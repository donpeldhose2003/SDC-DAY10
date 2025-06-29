package org.example;

import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.example.handlers.*;

public class Main extends AbstractVerticle {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Main());
    }

    @Override
    public void start() {
        JsonObject mongoConfig = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "electiveDB");

        MongoClient mongo = MongoClient.createShared(vertx, mongoConfig);

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());


        new CourseHandler(mongo).setup(router);
        new RegistrationHandler(vertx, mongo).setup(router);
        System.out.println("Serving static files from: " + System.getProperty("user.dir") + "/src/main/resources/webroot");

        router.route("/*").handler(StaticHandler.create("webroot"));


        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888, res -> {
                    if (res.succeeded()) {
                        System.out.println("Server running at http://localhost:8888");
                    } else {
                        System.err.println("Server failed: " + res.cause());
                    }
                });
    }
}
