package net.mednikov.vertxstore.entity;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import io.vertx.core.json.JsonObject;

public class ProductMapper {

    public static Product fromJson(JsonObject json) throws IllegalArgumentException {

        UUID productId = null;
        String productName;
        String description;
        String category;
        BigDecimal productPrice;

        if (json.containsKey("productId")) {
            productId = UUID.fromString(json.getString("productId"));
        }

        if (json.containsKey("productName")) {
            productName = json.getString("productName");
        } else {
            throw new IllegalArgumentException();
        }

        if (json.containsKey("description")) {
            description = json.getString("description");
        } else {
            throw new IllegalArgumentException();
        }

        if (json.containsKey("category")) {
            category = json.getString("category");
        } else {
            throw new IllegalArgumentException();
        }

        if (json.containsKey("productPrice")) {
            productPrice = new BigDecimal(json.getString("productPrice"));
        } else {
            throw new IllegalArgumentException();
        }

        return new Product(productId, productName, description, category, productPrice);
    }

    public static Product fromResultSet(ResultSet resultSet) throws SQLException {
        UUID productId = UUID.fromString(resultSet.getString("product_id"));
        String productName = resultSet.getString("product_name");
        String description = resultSet.getString("description");
        BigDecimal productPrice = resultSet.getBigDecimal("product_price");
        String category = resultSet.getString("category");
        return new Product(productId, productName, description, category, productPrice);
    }

    public static JsonObject toJson(Product product){
        JsonObject obj = new JsonObject();
        obj.put("productId", product.getProductId().toString());
        obj.put("productPrice", product.getProductPrice().toString());
        obj.put("productName", product.getProductName());
        obj.put("category", product.getCategory());
        obj.put("description", product.getDescription());
        return obj;
    }
}