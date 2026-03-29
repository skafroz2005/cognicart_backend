package com.cognicart.cognicart_app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.cognicart.cognicart_app.model.Size;
import com.cognicart.cognicart_app.request.AttributeExtractionRequest;
import com.cognicart.cognicart_app.response.AiHealthCheckResponse;
import com.cognicart.cognicart_app.response.AttributeExtractionResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AttributeExtractionServiceImpl implements AttributeExtractionService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${ai.engine.api.url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String aiApiUrl;

    @Value("${ai.engine.gemini.key:${ai.engine.api.key:}}")
    private String aiApiKey;

    @Value("${ai.engine.model:gemini-2.5-pro}")
    private String aiModel;

    @Value("${ai.engine.fallback-models:}")
    private String fallbackModels;

    public AttributeExtractionServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public AttributeExtractionResponse extractAttributes(AttributeExtractionRequest request) {
        if (!StringUtils.hasText(aiApiKey)) {
            AttributeExtractionResponse fallback = buildFallbackResponse(request);
            fallback.setNote("AI key not configured. Returned fallback extraction.");
            fallback.setConfidence(0.5);
            return fallback;
        }

        String prompt = buildPrompt(request);
        List<String> candidateModels = buildCandidateModels();
        String lastError = "unknown error";

        for (String model : candidateModels) {
            try {
                String content = callAiModel(prompt, model);
                AttributeExtractionResponse parsed = parseModelContent(content, request);
                parsed.setNote("Extracted using external AI model: " + model);
                parsed.setConfidence(Math.max(parsed.getConfidence(), 0.8));
                return parsed;
            } catch (Exception ex) {
                lastError = sanitizeErrorMessage(ex.getMessage());
            }
        }

        AttributeExtractionResponse fallback = buildFallbackResponse(request);
        fallback.setNote("AI response parse/call failed across models. Last error: " + lastError + ". Returned fallback extraction.");
        fallback.setConfidence(0.55);
        return fallback;
    }

    @Override
    public AiHealthCheckResponse checkHealth() {
        if (!StringUtils.hasText(aiApiKey)) {
            return new AiHealthCheckResponse(false, "Google Gemini", "DOWN", aiModel,
                    "AI key is not configured. Set GEMINI_API_KEY or ai.engine.gemini.key.");
        }

        String healthPrompt = "Return only JSON: {\"health\":\"ok\"}";
        String lastError = "unknown error";

        for (String model : buildCandidateModels()) {
            try {
                String response = callAiModel(healthPrompt, model);
                if (StringUtils.hasText(response)) {
                    return new AiHealthCheckResponse(true, "Google Gemini", "UP", model,
                            "Gemini API is reachable and responding.");
                }
            } catch (Exception ex) {
                lastError = sanitizeErrorMessage(ex.getMessage());
            }
        }

        String status = isRateLimitedMessage(lastError) ? "DEGRADED" : "DOWN";
        return new AiHealthCheckResponse(true, "Google Gemini", status, aiModel,
                "Gemini API check failed. Last error: " + lastError);
    }

    private List<String> buildCandidateModels() {
        List<String> models = new ArrayList<>();
        if (StringUtils.hasText(aiModel)) {
            models.add(aiModel.trim());
        }
        if (StringUtils.hasText(fallbackModels)) {
            String[] splitModels = fallbackModels.split(",");
            for (String model : splitModels) {
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

    private String buildPrompt(AttributeExtractionRequest req) {
        return "Extract ecommerce product attributes and return ONLY valid JSON object with these keys: "
                + "title, description, brand, color, tags(array), price(number), discountedPrice(number), discountPercent(number), "
                + "quantity(number), imageUrl, images(array), topLevelCategory, secondLevelCategory, thirdLevelCategory, "
                + "size(array of objects with keys name and quantity), confidence(number 0 to 1). "
                + "Keep all text DB-safe: title<=255 chars, description<=255 chars, brand<=255 chars, color<=255 chars, imageUrl<=255 chars, each tag<=255 chars, each image URL<=255 chars. "
                + "Keep description concise and product-focused; do not generate long paragraphs. "
                + "Return JSON only. Do not include markdown, explanations, or extra text. "
                + "If missing, infer safe defaults. Input: "
                + "title=" + safe(req.getTitle())
                + ", description=" + safe(req.getDescription())
                + ", imageUrl=" + safe(req.getImageUrl())
                + ", topLevelCategory=" + safe(req.getTopLevelCategory())
                + ", secondLevelCategory=" + safe(req.getSecondLevelCategory())
                + ", thirdLevelCategory=" + safe(req.getThirdLevelCategory())
                + ", price=" + (req.getPrice() == null ? 0 : req.getPrice())
                + ", quantity=" + (req.getQuantity() == null ? 0 : req.getQuantity());
    }

    private String callAiModel(String prompt, String model) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", aiApiKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", List.of(
                Map.of("parts", List.of(Map.of("text", prompt)))
        ));
        payload.put("generationConfig", Map.of(
                "temperature", 0.2,
                "responseMimeType", "application/json"
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        String normalizedBaseUrl = aiApiUrl.endsWith("/") ? aiApiUrl.substring(0, aiApiUrl.length() - 1) : aiApiUrl;
        String endpoint = normalizedBaseUrl + "/" + model + ":generateContent";
        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode contentNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        return contentNode.isMissingNode() ? "" : contentNode.asText("");
    }

    private String sanitizeErrorMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return "unknown error";
        }
        String cleaned = message.replaceAll("\\s+", " ").trim();

        String providerMessage = extractProviderMessage(cleaned);
        if (StringUtils.hasText(providerMessage)) {
            return providerMessage;
        }

        if (cleaned.length() > 800) {
            return cleaned.substring(0, 1).toUpperCase(Locale.ROOT) + cleaned.substring(1, 800) + "...";
        }
        return cleaned;
    }

    private String extractProviderMessage(String cleaned) {
        Pattern messagePattern = Pattern.compile("\\\"message\\\"\\s*:\\s*\\\"(.*?)\\\"");
        Matcher matcher = messagePattern.matcher(cleaned);
        if (matcher.find()) {
            return matcher.group(1).replace("\\\\n", " ").replace("\\\\\"", "\"").trim();
        }
        return "";
    }

    private boolean isRateLimitedMessage(String message) {
        String text = String.valueOf(message).toLowerCase(Locale.ROOT);
        return text.contains("429")
                || text.contains("rate")
                || text.contains("quota")
                || text.contains("resource_exhausted");
    }

    private AttributeExtractionResponse parseModelContent(String content, AttributeExtractionRequest req) throws Exception {
        String jsonText = extractJsonObject(content);
        if (!StringUtils.hasText(jsonText)) {
            return buildFallbackResponse(req);
        }

        Map<String, Object> modelMap = objectMapper.readValue(jsonText, new TypeReference<Map<String, Object>>() {});
        AttributeExtractionResponse result = buildFallbackResponse(req);

        result.setTitle(readString(modelMap, "title", result.getTitle()));
        result.setDescription(readString(modelMap, "description", result.getDescription()));
        result.setBrand(readString(modelMap, "brand", result.getBrand()));
        result.setColor(readString(modelMap, "color", result.getColor()));
        result.setPrice(readInt(modelMap, "price", result.getPrice()));
        result.setDiscountedPrice(readInt(modelMap, "discountedPrice", result.getDiscountedPrice()));
        result.setDiscountPercent(readInt(modelMap, "discountPercent", result.getDiscountPercent()));
        result.setQuantity(readInt(modelMap, "quantity", result.getQuantity()));
        result.setImageUrl(readString(modelMap, "imageUrl", result.getImageUrl()));
        result.setTopLevelCategory(readString(modelMap, "topLevelCategory", result.getTopLevelCategory()));
        result.setSecondLevelCategory(readString(modelMap, "secondLevelCategory", result.getSecondLevelCategory()));
        result.setThirdLevelCategory(readString(modelMap, "thirdLevelCategory", result.getThirdLevelCategory()));
        result.setConfidence(readDouble(modelMap, "confidence", 0.82));

        List<String> parsedTags = readStringList(modelMap.get("tags"));
        if (!parsedTags.isEmpty()) {
            result.setTags(parsedTags);
        }

        List<String> parsedImages = readStringList(modelMap.get("images"));
        if (!parsedImages.isEmpty()) {
            result.setImages(parsedImages);
        }

        Set<Size> parsedSizes = readSizes(modelMap.get("size"), result.getQuantity());
        if (!parsedSizes.isEmpty()) {
            result.setSize(parsedSizes);
        }

        if (result.getDiscountedPrice() == 0) {
            result.setDiscountedPrice(result.getPrice());
        }

        return result;
    }

    private AttributeExtractionResponse buildFallbackResponse(AttributeExtractionRequest req) {
        AttributeExtractionResponse res = new AttributeExtractionResponse();
        res.setTitle(safe(req.getTitle()));
        res.setDescription(safe(req.getDescription()));
        res.setImageUrl(safe(req.getImageUrl()));
        res.setTopLevelCategory(defaultIfBlank(req.getTopLevelCategory(), "men"));
        res.setSecondLevelCategory(defaultIfBlank(req.getSecondLevelCategory(), "clothing"));
        res.setThirdLevelCategory(defaultIfBlank(req.getThirdLevelCategory(), inferThirdCategory(req)));
        res.setPrice(req.getPrice() == null ? 0 : req.getPrice());
        res.setDiscountedPrice(res.getPrice());
        res.setDiscountPercent(0);
        res.setQuantity(req.getQuantity() == null ? 1 : req.getQuantity());
        res.setBrand(inferBrand(req.getTitle()));
        res.setColor(inferColor(req.getTitle(), req.getDescription()));

        List<String> tags = inferTags(req.getTitle(), req.getDescription());
        res.setTags(tags);

        if (StringUtils.hasText(req.getImageUrl())) {
            List<String> images = new ArrayList<>();
            images.add(req.getImageUrl().trim());
            res.setImages(images);
        }

        Set<Size> sizes = new LinkedHashSet<>();
        for (String s : List.of("S", "M", "L")) {
            Size size = new Size();
            size.setName(s);
            size.setQuantity(Math.max(1, res.getQuantity() / 3));
            sizes.add(size);
        }
        res.setSize(sizes);

        res.setConfidence(0.6);
        return res;
    }

    private String inferThirdCategory(AttributeExtractionRequest req) {
        String text = (safe(req.getTitle()) + " " + safe(req.getDescription())).toLowerCase(Locale.ROOT);
        if (text.contains("shirt")) {
            return "shirt";
        }
        if (text.contains("kurta")) {
            return "mens_kurta";
        }
        if (text.contains("dress")) {
            return "women_dress";
        }
        return "top";
    }

    private String inferBrand(String title) {
        if (!StringUtils.hasText(title)) {
            return "Generic";
        }
        String[] parts = title.trim().split("\\s+");
        return parts.length > 0 ? capitalize(parts[0]) : "Generic";
    }

    private String inferColor(String title, String description) {
        String text = (safe(title) + " " + safe(description)).toLowerCase(Locale.ROOT);
        List<String> colors = List.of("black", "white", "blue", "green", "red", "yellow", "pink", "grey", "gray", "brown", "orange", "purple");
        for (String color : colors) {
            if (text.contains(color)) {
                return capitalize(color.equals("gray") ? "grey" : color);
            }
        }
        return "Black";
    }

    private List<String> inferTags(String title, String description) {
        Set<String> tags = new LinkedHashSet<>();
        String text = (safe(title) + " " + safe(description)).toLowerCase(Locale.ROOT);
        for (String token : text.split("[^a-z0-9_]+")) {
            if (token.length() >= 4) {
                tags.add(token);
            }
            if (tags.size() >= 8) {
                break;
            }
        }
        if (tags.isEmpty()) {
            tags.add("fashion");
            tags.add("ecommerce");
        }
        return new ArrayList<>(tags);
    }

    private Set<Size> readSizes(Object value, int defaultQuantity) {
        Set<Size> result = new LinkedHashSet<>();
        if (!(value instanceof List<?> list)) {
            return result;
        }

        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                Object nameObj = map.get("name");
                if (nameObj == null) {
                    continue;
                }
                Size size = new Size();
                size.setName(String.valueOf(nameObj));
                Object qtyObj = map.get("quantity");
                int qty = qtyObj == null ? Math.max(1, defaultQuantity / 3) : parseInt(qtyObj, Math.max(1, defaultQuantity / 3));
                size.setQuantity(qty);
                result.add(size);
            }
        }
        return result;
    }

    private List<String> readStringList(Object value) {
        List<String> result = new ArrayList<>();
        if (!(value instanceof List<?> list)) {
            return result;
        }

        for (Object item : list) {
            if (item != null && StringUtils.hasText(String.valueOf(item))) {
                result.add(String.valueOf(item).trim());
            }
        }
        return result;
    }

    private String extractJsonObject(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        Pattern pattern = Pattern.compile("\\{.*}\\s*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input.trim());
        return matcher.find() ? matcher.group() : input.trim();
    }

    private String readString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        String str = String.valueOf(value).trim();
        return str.isEmpty() ? defaultValue : str;
    }

    private int readInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : parseInt(value, defaultValue);
    }

    private int parseInt(Object value, int defaultValue) {
        try {
            if (value instanceof Number number) {
                return number.intValue();
            }
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private double readDouble(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            if (value instanceof Number number) {
                return number.doubleValue();
            }
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String capitalize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
