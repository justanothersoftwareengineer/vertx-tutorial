package net.mednikov.vertxstore.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.mednikov.vertxstore.entity.Product;

public interface IProductRepository{

    Product save (Product product) throws RepositoryException;

    void remove (UUID productId) throws RepositoryException;

    void update (Product product) throws RepositoryException;

    List<Product> getProductsByCategory(String category) throws RepositoryException;

    List<Product> getProductsInPriceRange(BigDecimal min, BigDecimal max) throws RepositoryException;

    Optional<Product> getProductById (UUID productId) throws RepositoryException;
}