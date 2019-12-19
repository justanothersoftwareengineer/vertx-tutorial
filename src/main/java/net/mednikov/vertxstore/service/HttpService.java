package net.mednikov.vertxstore.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import static net.mednikov.vertxstore.service.BusAddress.*;

import java.math.BigDecimal;

public class HttpService extends AbstractVerticle{

    private EventBus eventBus;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        eventBus = vertx.eventBus();
        int port = config().getInteger("port");
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/api/*").handler(BodyHandler.create());
        router.post("/api/products").handler(this::saveProduct);
        router.put("/api/products").handler(this::updateProduct);
        router.delete("/api/products/:productId").handler(this::removeProduct);
        router.get("/api/products/query/category/:category").handler(this::getProductsByCategory);
        router.get("/api/products/query/price/:min/:max").handler(this::getProductsByPriceRange);
        router.get("/api/products/one/:productId").handler(this::getProductById);
        server.requestHandler(router);
        server.listen(port, res->{
           if (res.succeeded()){
               startPromise.complete();
           } else {
               startPromise.fail(res.cause());
           }
       });
    }

    private void saveProduct(RoutingContext context){
        JsonObject payload = context.getBodyAsJson();
        eventBus.request(DB_SAVE_PRODUCT, payload, reply->{
            if (reply.succeeded()){
                JsonObject result = JsonObject.mapFrom(reply.result().body());
                context.response().setStatusCode(200).end(result.encode());
            } else {
                context.response().setStatusCode(500).end();
            }
        });
    }

    private void updateProduct(RoutingContext context){
        JsonObject payload = context.getBodyAsJson();
        eventBus.publish(DB_UPDATE_PRODUCT, payload);
        context.response().setStatusCode(200).end();
    }

    private void removeProduct(RoutingContext context){
        String productId = context.pathParam("productId");
        JsonObject payload = new JsonObject();
        payload.put("productId", productId);
        eventBus.publish(DB_REMOVE_PRODUCT, payload);
        context.response().setStatusCode(200).end();
    }

    private void getProductsByCategory(RoutingContext context){
        String category = context.pathParam("category");
        JsonObject payload = new JsonObject();
        payload.put("category", category);
        eventBus.request(DB_GET_CATEGORY_PRODUCT, payload, reply->{
            if (reply.succeeded()){
                JsonArray results = new JsonArray(reply.result().body().toString());
                context.response().setStatusCode(200).end(results.encode());
            } else {
                context.response().setStatusCode(500).end();
            }
        });
    }

    private void getProductsByPriceRange(RoutingContext context){
        BigDecimal min = new BigDecimal(context.pathParam("min"));
        BigDecimal max = new BigDecimal(context.pathParam("max"));
        JsonObject payload = new JsonObject();
        payload.put("min", min.toString());
        payload.put("max", max.toString());
        eventBus.request(DB_GET_PRICE_RANGE_PRODUCT, payload, reply->{
            if (reply.succeeded()){
                JsonArray results = new JsonArray(reply.result().body().toString());
                context.response().setStatusCode(200).end(results.encode());
            } else {
                context.response().setStatusCode(500).end();
            }
        });
    }

    private void getProductById(RoutingContext context){
        String productId = context.pathParam("productId");
        JsonObject payload = new JsonObject();
        payload.put("productId", productId);
        eventBus.request(DB_GET_ONE_PRODUCT, payload, reply->{
            if (reply.succeeded()){
                JsonObject result = JsonObject.mapFrom(reply.result().body());
                context.response().setStatusCode(200).end(result.encode());
            } else {
                context.response().setStatusCode(404).end();
            }
        });
    }
}