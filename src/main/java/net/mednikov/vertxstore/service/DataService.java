package net.mednikov.vertxstore.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.mednikov.vertxstore.app.DataServiceModule;
import net.mednikov.vertxstore.entity.Product;
import net.mednikov.vertxstore.entity.ProductMapper;
import net.mednikov.vertxstore.repository.IProductRepository;
import net.mednikov.vertxstore.repository.RepositoryException;

import static net.mednikov.vertxstore.service.BusAddress.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;




public class DataService extends AbstractVerticle{

    @Inject IProductRepository productRepository;
    private EventBus eventBus;

    public DataService(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public DataService(DataServiceModule module){
        Injector injector = Guice.createInjector(module);
        injector.injectMembers(this);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        eventBus = vertx.eventBus();
        eventBus.consumer(DB_SAVE_PRODUCT, message->{
            try {
                JsonObject payload = JsonObject.mapFrom(message.body());
                JsonObject result = saveProduct(payload);
                message.reply(result);
            } catch (RepositoryException ex){
                message.fail(500, "Database unavailable");
            }
        });
        eventBus.consumer(DB_UPDATE_PRODUCT, message->{
            try {
                JsonObject payload = JsonObject.mapFrom(message.body());
                update(payload);
            } catch (RepositoryException ex){
                message.fail(500, "Database unavailable");
            }
        });
        eventBus.consumer(DB_REMOVE_PRODUCT, message->{
            try {
                JsonObject payload = JsonObject.mapFrom(message.body());
                remove(payload);
            } catch (RepositoryException ex){
                message.fail(500, "Database unavailable");
            }
        });
        eventBus.consumer(DB_GET_CATEGORY_PRODUCT, message->{
            try {
                JsonObject payload = JsonObject.mapFrom(message.body());
                JsonArray results = getProductByCategory(payload);
                message.reply(results);
            } catch (RepositoryException ex){
                message.fail(500, "Database unavailable");
            }
        });
        eventBus.consumer(DB_GET_PRICE_RANGE_PRODUCT, message->{
            try {
                JsonObject payload = JsonObject.mapFrom(message.body());
                JsonArray results = getProductByPriceRange(payload);
                message.reply(results);
            } catch (RepositoryException ex){
                message.fail(500, "Database unavailable");
            }
        });
        eventBus.consumer(DB_GET_ONE_PRODUCT, message->{
            try {
                JsonObject payload = JsonObject.mapFrom(message.body());
                getProductById(payload).ifPresentOrElse(message::reply, ()-> message.fail(404, "Not found"));

            } catch (RepositoryException ex){
                message.fail(500, "Database unavailable");
            }
        });
        startPromise.complete();
    }

    JsonObject saveProduct (JsonObject json) throws RepositoryException{
        Product product = ProductMapper.fromJson(json);
        Product result = productRepository.save(product);
        return ProductMapper.toJson(result);
    }

    void update (JsonObject json) throws RepositoryException{
        Product product = ProductMapper.fromJson(json);
        productRepository.update(product);
    }

    void remove(JsonObject json) throws RepositoryException{
        UUID productId = UUID.fromString(json.getString("productId"));
        productRepository.remove(productId);
    }

    JsonArray getProductByCategory(JsonObject json) throws RepositoryException{
        String category = json.getString("category");
      //  List<Product> results = productRepository.getProductsByCategory(category);
        // 1 list products
        // 2 list jsonobject
        // 3 convert product -> jsonobject
        List<JsonObject> results = productRepository
            .getProductsByCategory(category)
            .stream()
            .map(ProductMapper::toJson).collect(Collectors.toList());
        return new JsonArray(results);
    }

    JsonArray getProductByPriceRange(JsonObject json) throws RepositoryException{
        BigDecimal min = new BigDecimal(json.getString("min"));
        BigDecimal max = new BigDecimal(json.getString("max"));
     //   List<Product> results = productRepository.getProductsInPriceRange(min, max);
        List<JsonObject> results = productRepository
            .getProductsInPriceRange(min, max)
            .stream()
            .map(ProductMapper::toJson).collect(Collectors.toList());
        return new JsonArray(results);
    }

    Optional<JsonObject> getProductById(JsonObject json) throws RepositoryException{
        UUID productId = UUID.fromString(json.getString("productId"));
        Optional<Product> result = productRepository.getProductById(productId);
        if (result.isPresent()){
            return Optional.of(ProductMapper.toJson(result.get()));
        } else {
            return Optional.empty();
        }
    }
}