package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddCartItemRequest;
import com.example.ecommerce.dto.CartItemResponse;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartResponse getCart(String email) {
        return toResponse(getOrCreateCart(email));
    }

    @Transactional
    public CartResponse addItem(String email, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(email);
        Product product = findActiveProduct(request.productId());

        CartItem item = findItem(cart, product.getId());
        int currentQuantity = item == null ? 0 : item.getQuantity();
        int newQuantity = currentQuantity + request.quantity();
        checkStock(product, newQuantity);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(newQuantity);
            cart.getItems().add(item);
        } else {
            item.setQuantity(newQuantity);
        }
        return save(cart);
    }

    @Transactional
    public CartResponse updateItem(String email, Long productId, int quantity) {
        Cart cart = getOrCreateCart(email);
        CartItem item = findItem(cart, productId);
        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item is not in the cart");
        }
        checkStock(item.getProduct(), quantity);
        item.setQuantity(quantity);
        return save(cart);
    }

    @Transactional
    public CartResponse removeItem(String email, Long productId) {
        Cart cart = getOrCreateCart(email);
        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item is not in the cart");
        }
        return save(cart);
    }

    @Transactional
    public void clear(String email) {
        Cart cart = getOrCreateCart(email);
        cart.getItems().clear();
        save(cart);
    }

    private CartResponse save(Cart cart) {
        cart.setUpdatedAt(Instant.now());
        return toResponse(cartRepository.save(cart));
    }

    private Cart getOrCreateCart(String email) {
        return cartRepository.findByUserEmail(email).orElseGet(() -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    private Product findActiveProduct(Long productId) {
        return productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private CartItem findItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private void checkStock(Product product, int quantity) {
        if (quantity > product.getStockQuantity()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only " + product.getStockQuantity() + " in stock for " + product.getName());
        }
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(item -> {
            Product product = item.getProduct();
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            boolean available = product.isActive() && product.getStockQuantity() >= item.getQuantity();
            return new CartItemResponse(
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    product.getPrice(),
                    item.getQuantity(),
                    lineTotal,
                    product.getStockQuantity(),
                    available);
        }).toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = items.stream().mapToInt(CartItemResponse::quantity).sum();
        return new CartResponse(items, totalItems, totalAmount);
    }
}