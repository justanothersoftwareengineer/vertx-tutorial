package net.mednikov.vertxstore.entity;

import java.math.BigDecimal;
import java.util.UUID;

public class Product{

    private final UUID productId;
    private final String productName;
    private final String description;
    private final String category;
    private final BigDecimal productPrice;

    public Product(UUID productId, String productName, String description, String category, BigDecimal productPrice) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.category = category;
        this.productPrice = productPrice;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    

}