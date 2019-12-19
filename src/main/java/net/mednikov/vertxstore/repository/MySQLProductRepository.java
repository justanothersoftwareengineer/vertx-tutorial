package net.mednikov.vertxstore.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mysql.cj.jdbc.MysqlDataSource;

import net.mednikov.vertxstore.entity.Product;
import net.mednikov.vertxstore.entity.ProductMapper;

public class MySQLProductRepository implements IProductRepository {

    private static final String SELECT_STATEMENT = "SELECT product_id, product_name, description, product_price, category FROM products WHERE ";

    private MysqlDataSource dataSource;

    public MySQLProductRepository(MysqlDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Product save(Product product) throws RepositoryException {
        try (Connection connection = dataSource.getConnection()){
            PreparedStatement statement = connection.prepareStatement("INSERT INTO products (product_id, product_name, description, category, product_price) VALUES (?,?,?,?,?);");
            UUID productId = UUID.randomUUID();
            statement.setString(1, productId.toString());
            statement.setString(2, product.getProductName());
            statement.setString(3, product.getDescription());
            statement.setString(4, product.getCategory());
            statement.setBigDecimal(5, product.getProductPrice());
            statement.execute();
            return new Product(productId, 
                product.getProductName(), 
                product.getDescription(), 
                product.getCategory(), 
                product.getProductPrice());
        } catch(SQLException ex){
            ex.printStackTrace();
            throw new RepositoryException();
        }
    }

    @Override
    public void remove(UUID productId) throws RepositoryException {
        try (Connection connection = dataSource.getConnection()){
            PreparedStatement statement = connection.prepareStatement("DELETE FROM products WHERE product_id=?;");
            statement.setString(1, productId.toString());
            statement.executeUpdate();
        } catch(SQLException ex){
            ex.printStackTrace();
            throw new RepositoryException();
        }
    }

    @Override
    public void update(Product product) throws RepositoryException {
        try (Connection connection = dataSource.getConnection()){
            PreparedStatement statement = connection.prepareStatement("UPDATE products SET product_name=?, description=?, category=?, product_price=? WHERE product_id=?");
            statement.setString(1, product.getProductName());
            statement.setString(2, product.getDescription());
            statement.setString(3, product.getCategory());
            statement.setBigDecimal(4, product.getProductPrice());
            statement.setString(5, product.getProductId().toString());
            statement.executeUpdate();
        } catch(SQLException ex){
            ex.printStackTrace();
            throw new RepositoryException();
        }
    }

    @Override
    public List<Product> getProductsByCategory(String cat) throws RepositoryException {
        try (Connection connection = dataSource.getConnection()){
            List<Product> results = new ArrayList<>();
            String sql = SELECT_STATEMENT.concat("category=?;");
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, cat);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                Product product = ProductMapper.fromResultSet(resultSet);
                results.add(product);
            }
            return results;
        } catch(SQLException ex){
            ex.printStackTrace();
            throw new RepositoryException();
        }
    }

    @Override
    public List<Product> getProductsInPriceRange(BigDecimal min, BigDecimal max) throws RepositoryException {
        try (Connection connection = dataSource.getConnection()){
            List<Product> results = new ArrayList<>();
            String sql = SELECT_STATEMENT.concat(" product_price BETWEEN ? AND ?;");
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setBigDecimal(1, min);
            statement.setBigDecimal(2, max);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                Product product = ProductMapper.fromResultSet(resultSet);
                results.add(product);
            }
            return results;
        } catch(SQLException ex){
            ex.printStackTrace();
            throw new RepositoryException();
        }
    }

    @Override
    public Optional<Product> getProductById(UUID id) throws RepositoryException {
        try (Connection connection = dataSource.getConnection()){
            String sql = SELECT_STATEMENT.concat(" product_id=?");
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, id.toString());
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                Product product = ProductMapper.fromResultSet(resultSet);
                return Optional.of(product);
            } else {
                // don't have data
                return Optional.empty();
            }
        } catch(SQLException ex){
            ex.printStackTrace();
            throw new RepositoryException();
        }
    }
}