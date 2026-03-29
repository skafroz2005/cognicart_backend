package com.cognicart.cognicart_app.service;

import com.cognicart.cognicart_app.model.AiChatMessage;
import com.cognicart.cognicart_app.model.AiChatSession;
import com.cognicart.cognicart_app.model.Product;
import com.cognicart.cognicart_app.model.User;
import com.cognicart.cognicart_app.repository.AiChatMessageRepository;
import com.cognicart.cognicart_app.repository.AiChatSessionRepository;
import com.cognicart.cognicart_app.request.AiChatSendRequest;
import com.cognicart.cognicart_app.request.AiSessionCreateRequest;
import com.cognicart.cognicart_app.response.AiChatMessageResponse;
import com.cognicart.cognicart_app.response.AiChatSendResponse;
import com.cognicart.cognicart_app.response.AiChatSessionResponse;
import com.cognicart.cognicart_app.response.AiSuggestedActionResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiModeServiceImpl implements AiModeService {

    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;
    private final UserService userService;
    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${ai.engine.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String aiApiUrl;

    @Value("${ai.engine.gemini.key:${ai.engine.api.key:}}")
    private String aiApiKey;

    @Value("${ai.engine.model:gemini-3-flash-preview}")
    private String aiModel;

    @Value("${ai.mode.fallback-models:gemini-2.5-flash,gemini-2.0-flash}")
    private String fallbackModels;

    public AiModeServiceImpl(
            AiChatSessionRepository sessionRepository,
            AiChatMessageRepository messageRepository,
            UserService userService,
            ProductService productService,
            ObjectMapper objectMapper
    ) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.productService = productService;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<AiChatSessionResponse> getSessions(String jwt) {
        User user = getUser(jwt);
        List<AiChatSession> sessions = sessionRepository.findByUserOrderByUpdatedAtDesc(user);
        List<AiChatSessionResponse> response = new ArrayList<>();
        for (AiChatSession session : sessions) {
            response.add(mapSession(session));
        }
        return response;
    }

    @Override
    public AiChatSessionResponse createSession(String jwt, AiSessionCreateRequest request) {
        User user = getUser(jwt);

        AiChatSession session = new AiChatSession();
        session.setUser(user);
        session.setTitle(trimTo255(defaultIfBlank(request == null ? null : request.getTitle(), "New Shopping Chat")));
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        AiChatSession saved = sessionRepository.save(session);

        saveMessage(saved, "assistant", "Premium stylist AI mode is active. Ask for product discovery, comparisons, add-to-cart, or checkout help.");

        return mapSession(saved);
    }

    @Override
    public List<AiChatMessageResponse> getMessages(String jwt, Long sessionId) {
        AiChatSession session = getOwnedSession(jwt, sessionId);
        List<AiChatMessage> messages = messageRepository.findBySessionOrderByCreatedAtAsc(session);
        List<AiChatMessageResponse> response = new ArrayList<>();
        for (AiChatMessage message : messages) {
            response.add(mapMessage(message));
        }
        return response;
    }

    @Override
    public AiChatSendResponse sendMessage(String jwt, AiChatSendRequest request) {
        if (request == null || !StringUtils.hasText(request.getMessage())) {
            throw new RuntimeException("Message is required.");
        }

        AiChatSession session = getOwnedSession(jwt, request.getSessionId());
        String userText = request.getMessage().trim();

        AiChatMessage userMessage = saveMessage(session, "user", userText);

        List<AiChatMessage> recent = messageRepository.findTop10BySessionOrderByCreatedAtDesc(session);
        List<AiChatMessage> chronologicalRecent = new ArrayList<>();
        for (int i = recent.size() - 1; i >= 0; i--) {
            chronologicalRecent.add(recent.get(i));
        }

        String searchQuery = extractSearchQuery(userText);
        List<Product> retrieved = getCandidateProducts(searchQuery);
        boolean usedAi = false;
        String aiError = "";
        String resolvedModel = aiModel;

        GeminiDecision decision = buildFallbackDecision(userText, retrieved);
        if (StringUtils.hasText(aiApiKey)) {
            String prompt = buildGeminiPrompt(userText, chronologicalRecent, retrieved);
            String lastError = "";

            for (String model : buildModelCandidates()) {
                try {
                    String geminiText = callGemini(prompt, model);
                    GeminiDecision parsed = parseGeminiDecision(geminiText);
                    if (StringUtils.hasText(parsed.getSearchQuery())) {
                        List<Product> refined = getCandidateProducts(parsed.getSearchQuery());
                        if (!refined.isEmpty()) {
                            retrieved = refined;
                        }
                    }
                    decision = mergeDecision(decision, parsed, retrieved);
                    usedAi = true;
                    resolvedModel = model;
                    lastError = "";
                    break;
                } catch (Exception ex) {
                    lastError = sanitizeError(ex.getMessage());
                }
            }

            if (StringUtils.hasText(lastError)) {
                aiError = lastError;
            }
        }

        AiChatMessage assistant = saveMessage(session, "assistant", decision.getAssistantMessage());

        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);

        AiChatSendResponse response = new AiChatSendResponse();
        response.setSessionId(session.getId());
        response.setUserMessage(mapMessage(userMessage));
        response.setAssistantMessage(mapMessage(assistant));
        response.setProducts(retrieved);

        if (decision.getActionType() != null && !"none".equalsIgnoreCase(decision.getActionType())) {
            AiSuggestedActionResponse action = new AiSuggestedActionResponse();
            action.setType(decision.getActionType());
            action.setProductId(decision.getActionProductId());
            action.setRequiresConfirmation(decision.isRequiresConfirmation());
            response.setAction(action);
        }

        response.setUsedAi(usedAi);
        response.setProvider("Google Gemini");
        response.setModel(resolvedModel);
        response.setAiError(aiError);

        return response;
    }

    private List<Product> getCandidateProducts(String query) {
        List<Product> byQuery = trimProducts(productService.searchProduct(query), 12);
        if (!byQuery.isEmpty()) {
            return byQuery;
        }

        Page<Product> generic = productService.getAllProduct(
                null,
                null,
                null,
                null,
                null,
                0,
                100000,
                0,
                "price_low",
                null,
                0,
                12
        );

        return trimProducts(generic.getContent(), 12);
    }

    private User getUser(String jwt) {
        try {
            return userService.findUserProfileByJwt(jwt);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to resolve user from token.");
        }
    }

    private AiChatSession getOwnedSession(String jwt, Long sessionId) {
        if (sessionId == null) {
            throw new RuntimeException("Session id is required.");
        }
        User user = getUser(jwt);
        Optional<AiChatSession> session = sessionRepository.findByIdAndUser(sessionId, user);
        if (session.isEmpty()) {
            throw new RuntimeException("Chat session not found.");
        }
        return session.get();
    }

    private AiChatMessage saveMessage(AiChatSession session, String role, String content) {
        AiChatMessage message = new AiChatMessage();
        message.setSession(session);
        message.setRole(role);
        message.setContent(content == null ? "" : content.trim());
        message.setCreatedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    private AiChatSessionResponse mapSession(AiChatSession session) {
        AiChatSessionResponse response = new AiChatSessionResponse();
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        return response;
    }

    private AiChatMessageResponse mapMessage(AiChatMessage message) {
        AiChatMessageResponse response = new AiChatMessageResponse();
        response.setId(message.getId());
        response.setRole(message.getRole());
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private String trimTo255(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        return trimmed.length() > 255 ? trimmed.substring(0, 255) : trimmed;
    }

    private List<Product> trimProducts(List<Product> products, int max) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }
        int end = Math.min(products.size(), max);
        return new ArrayList<>(products.subList(0, end));
    }

    private String extractSearchQuery(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String normalized = text.trim();
        if (normalized.toLowerCase(Locale.ROOT).startsWith("/search ")) {
            return normalized.substring(8).trim();
        }
        return normalized;
    }

    private String buildGeminiPrompt(String userMessage, List<AiChatMessage> recentMessages, List<Product> retrieved) {
        StringBuilder history = new StringBuilder();
        for (AiChatMessage message : recentMessages) {
            history.append(message.getRole()).append(": ").append(message.getContent()).append("\n");
        }

        List<Map<String, Object>> productContext = new ArrayList<>();
        for (Product product : retrieved) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", product.getId());
            row.put("title", trimTo255(product.getTitle()));
            row.put("brand", trimTo255(product.getBrand()));
            row.put("color", trimTo255(product.getColor()));
            row.put("price", product.getPrice());
            row.put("discountedPrice", product.getDiscountedPrice());
            row.put("quantity", product.getQuantity());
            row.put("description", trimTo255(product.getDescription()));
            productContext.add(row);
        }

        String productsJson;
        try {
            productsJson = objectMapper.writeValueAsString(productContext);
        } catch (Exception ex) {
            productsJson = "[]";
        }

        return "You are Cognicart premium stylist shopping assistant. "
                + "Return ONLY JSON with keys: assistantMessage, searchQuery, action. "
                + "action is object with keys: type(one of none,add_to_cart,go_to_checkout), productId(number or null), requiresConfirmation(boolean). "
                + "If user asks checkout, set requiresConfirmation=true. "
                + "If user asks add to cart and products exist, select best productId. "
                + "Keep assistantMessage concise and premium-stylist tone. "
                + "Conversation history: " + history
                + "Retrieved products: " + productsJson
                + "Current user message: " + userMessage;
    }

    private List<String> buildModelCandidates() {
        List<String> models = new ArrayList<>();
        if (StringUtils.hasText(aiModel)) {
            models.add(aiModel.trim());
        }
        if (StringUtils.hasText(fallbackModels)) {
            String[] split = fallbackModels.split(",");
            for (String model : split) {
                if (!StringUtils.hasText(model)) {
                    continue;
                }
                String trimmed = model.trim();
                if (!models.contains(trimmed)) {
                    models.add(trimmed);
                }
            }
        }
        return models;
    }

    private String callGemini(String prompt, String model) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", aiApiKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        payload.put("generationConfig", Map.of("temperature", 0.2, "responseMimeType", "application/json"));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        String base = aiApiUrl.endsWith("/") ? aiApiUrl.substring(0, aiApiUrl.length() - 1) : aiApiUrl;
        String endpoint = base + "/" + model + ":generateContent";

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        return textNode.isMissingNode() ? "" : textNode.asText("");
    }

    private GeminiDecision parseGeminiDecision(String modelText) {
        GeminiDecision decision = new GeminiDecision();
        if (!StringUtils.hasText(modelText)) {
            return decision;
        }

        try {
            String json = extractJsonObject(modelText);
            Map<String, Object> data = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            decision.setAssistantMessage(readString(data, "assistantMessage", ""));
            decision.setSearchQuery(readString(data, "searchQuery", ""));

            Object actionObj = data.get("action");
            if (actionObj instanceof Map<?, ?> actionMap) {
                Object type = actionMap.get("type");
                if (type != null) {
                    decision.setActionType(String.valueOf(type).trim().toLowerCase(Locale.ROOT));
                }
                Object productId = actionMap.get("productId");
                if (productId instanceof Number number) {
                    decision.setActionProductId(number.longValue());
                } else if (productId != null && StringUtils.hasText(String.valueOf(productId))) {
                    try {
                        decision.setActionProductId(Long.parseLong(String.valueOf(productId).trim()));
                    } catch (Exception ignored) {
                        // Ignore invalid product id format.
                    }
                }
                Object requires = actionMap.get("requiresConfirmation");
                if (requires instanceof Boolean bool) {
                    decision.setRequiresConfirmation(bool);
                }
            }
        } catch (Exception ignored) {
            // Parser fallback handled by caller.
        }
        return decision;
    }

    private GeminiDecision mergeDecision(GeminiDecision fallback, GeminiDecision parsed, List<Product> retrieved) {
        GeminiDecision merged = new GeminiDecision();
        merged.setAssistantMessage(StringUtils.hasText(parsed.getAssistantMessage())
                ? parsed.getAssistantMessage()
                : fallback.getAssistantMessage());
        merged.setSearchQuery(StringUtils.hasText(parsed.getSearchQuery())
                ? parsed.getSearchQuery()
                : fallback.getSearchQuery());

        String parsedType = parsed.getActionType();
        if (!StringUtils.hasText(parsedType) || "none".equals(parsedType)) {
            merged.setActionType(fallback.getActionType());
            merged.setActionProductId(fallback.getActionProductId());
            merged.setRequiresConfirmation(fallback.isRequiresConfirmation());
        } else {
            merged.setActionType(parsedType);
            merged.setActionProductId(parsed.getActionProductId());
            merged.setRequiresConfirmation(parsed.isRequiresConfirmation());
        }

        if ("add_to_cart".equals(merged.getActionType()) && merged.getActionProductId() == null && !retrieved.isEmpty()) {
            merged.setActionProductId(retrieved.get(0).getId());
        }

        if (!StringUtils.hasText(merged.getAssistantMessage())) {
            merged.setAssistantMessage(fallback.getAssistantMessage());
        }

        return merged;
    }

    private GeminiDecision buildFallbackDecision(String userText, List<Product> retrieved) {
        GeminiDecision decision = new GeminiDecision();
        decision.setSearchQuery(extractSearchQuery(userText));

        String lowered = userText == null ? "" : userText.toLowerCase(Locale.ROOT);
        if (lowered.contains("checkout")) {
            decision.setActionType("go_to_checkout");
            decision.setRequiresConfirmation(true);
            decision.setAssistantMessage("I can take you to checkout. Please confirm this action.");
            return decision;
        }

        if (lowered.contains("add") && lowered.contains("cart") && !retrieved.isEmpty()) {
            decision.setActionType("add_to_cart");
            decision.setActionProductId(retrieved.get(0).getId());
            decision.setRequiresConfirmation(false);
            decision.setAssistantMessage("Great choice. I can add the top product to your cart instantly.");
            return decision;
        }

        if (retrieved.isEmpty()) {
            decision.setActionType("none");
            decision.setAssistantMessage("I could not find matching products yet. Try color, category, budget, or occasion.");
            return decision;
        }

        String top = retrieved.get(0).getTitle() == null ? "top picks" : retrieved.get(0).getTitle();
        decision.setActionType("none");
        decision.setAssistantMessage("I found relevant options. My stylist lead recommendation is: " + top + ". You can ask to add it to cart.");
        return decision;
    }

    private String extractJsonObject(String input) {
        Pattern pattern = Pattern.compile("\\{.*}\\s*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input.trim());
        return matcher.find() ? matcher.group() : input.trim();
    }

    private String sanitizeError(String error) {
        if (!StringUtils.hasText(error)) {
            return "";
        }
        String cleaned = error.replaceAll("\\s+", " ").trim();
        if (cleaned.length() > 380) {
            return cleaned.substring(0, 380) + "...";
        }
        return cleaned;
    }

    private String readString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        String str = String.valueOf(value).trim();
        return str.isEmpty() ? defaultValue : str;
    }

    private static class GeminiDecision {
        private String assistantMessage;
        private String searchQuery;
        private String actionType;
        private Long actionProductId;
        private boolean requiresConfirmation;

        public String getAssistantMessage() {
            return assistantMessage;
        }

        public void setAssistantMessage(String assistantMessage) {
            this.assistantMessage = assistantMessage;
        }

        public String getSearchQuery() {
            return searchQuery;
        }

        public void setSearchQuery(String searchQuery) {
            this.searchQuery = searchQuery;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        public Long getActionProductId() {
            return actionProductId;
        }

        public void setActionProductId(Long actionProductId) {
            this.actionProductId = actionProductId;
        }

        public boolean isRequiresConfirmation() {
            return requiresConfirmation;
        }

        public void setRequiresConfirmation(boolean requiresConfirmation) {
            this.requiresConfirmation = requiresConfirmation;
        }
    }
}
