package net.mednikov.vertxstore.app;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import net.mednikov.vertxstore.service.DataService;
import net.mednikov.vertxstore.service.HttpService;

public class Application {

    public static void main(String[] args) {
        // create vertx instance vertx god object
        Vertx vertx = Vertx.vertx();

        // obtain configuration
        ConfigRetriever retriever = ConfigRetriever.create(vertx, getConfigOptions());
        retriever.getConfig(configResult -> {
            if (configResult.succeeded()) {
                JsonObject config = configResult.result();
                String dbUrl = config.getString("DB_URL");
                int port = config.getInteger("PORT");
                DataServiceModule module = new DataServiceModule(dbUrl);
                vertx.deployVerticle(new DataService(module), result1 -> {
                    if (result1.succeeded()) {
                        DeploymentOptions options = new DeploymentOptions();
                        options.setConfig(new JsonObject().put("port", port));
                        vertx.deployVerticle(new HttpService(), options, result2 -> {
                            if (result2.succeeded()) {
                                System.out.println("Application was successfully deployed");
                            } else {
                                result2.cause().printStackTrace();
                                vertx.close();
                            }
                        });
                    } else {
                        result1.cause().printStackTrace();
                        vertx.close();
                    }
                });
            } else {
                configResult.cause().printStackTrace();
                vertx.close();
            }
        });
    }

    private static ConfigRetrieverOptions getConfigOptions(){
        ConfigRetrieverOptions options = new ConfigRetrieverOptions();
        ConfigStoreOptions storeOptions = new ConfigStoreOptions();
        storeOptions.setType("file");
        storeOptions.setFormat("properties");
        storeOptions.setConfig(new JsonObject().put("path", "testconfig.properties"));
        options.addStore(storeOptions);
        return options;
    }
}