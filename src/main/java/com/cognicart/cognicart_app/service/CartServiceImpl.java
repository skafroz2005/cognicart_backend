package com.cognicart.cognicart_app.service;

import com.cognicart.cognicart_app.repository.CartItemRepository;
import org.springframework.stereotype.Service;

import com.cognicart.cognicart_app.exception.ProductException;
import com.cognicart.cognicart_app.model.Cart;
import com.cognicart.cognicart_app.model.CartItem;
import com.cognicart.cognicart_app.model.Product;
import com.cognicart.cognicart_app.model.User;
import com.cognicart.cognicart_app.repository.CartRepository;
import com.cognicart.cognicart_app.request.AddItemRequest;

@Service
public class CartServiceImpl implements CartService {

    private CartRepository cartRepository;
    private CartItemService cartItemService;
    private ProductService productService;
    private CartItemRepository cartItemRepository;

    public CartServiceImpl(CartRepository cartRepository, CartItemService cartItemService, ProductService productService, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemService = cartItemService;
        this.productService = productService;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    @Override
    public String addCartItem(Long userId, AddItemRequest req) throws ProductException {
        Cart cart = cartRepository.findByUserId(userId);
        Product product = productService.findProductById(req.getProductId());

        CartItem isPresent = cartItemService.isCartItemExist(cart, product, req.getSize(), userId);

        if(isPresent == null) {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(cart);
            cartItem.setQuantity(req.getQuantity());
            cartItem.setUserId(userId);

            int price = req.getQuantity() * product.getDiscountedPrice();
            cartItem.setPrice(price);
            cartItem.setSize(req.getSize());

            CartItem createdCartItem = cartItemService.createCartItem(cartItem);
            cart.getCartItems().add(createdCartItem);
        }
        return "Item Add To Cart";
    }

    @Override
    public Cart findUserCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);

        int totalPrice = 0;
        int totalDiscountedPrice = 0;
        int totalItem = 0;

        for(CartItem cartItem : cart.getCartItems()) {
            totalPrice += cartItem.getPrice();
            totalDiscountedPrice += cartItem.getDiscountedPrice();
            totalItem += cartItem.getQuantity();
        }

        cart.setTotalDiscountedPrice(totalDiscountedPrice);
        cart.setTotalItem(totalItem);
        cart.setTotalPrice(totalPrice);
        cart.setDiscount(totalPrice - totalDiscountedPrice);

        return cartRepository.save(cart);
    }


    @Override
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);

        // 1. Delete all the items from the database so they don't linger
        cartItemRepository.deleteAll(cart.getCartItems());

        // 2. Clear the list and reset all the pricing math back to zero
        cart.getCartItems().clear();
        cart.setTotalDiscountedPrice(0);
        cart.setTotalItem(0);
        cart.setTotalPrice(0);
        cart.setDiscount(0);

        // 3. Save the empty cart back to the database
        cartRepository.save(cart);
    }
}