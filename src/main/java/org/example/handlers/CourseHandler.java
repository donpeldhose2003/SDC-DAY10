package org.example.handlers;

import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.JsonObject;

public class CourseHandler {
    private final MongoClient mongo;

    public CourseHandler(MongoClient mongo) {
        this.mongo = mongo;
    }

    public void setup(Router router) {
        router.post("/course").handler(this::addCourse);
        router.get("/courses").handler(this::listCourses);
    }

    private void addCourse(RoutingContext ctx) {
        JsonObject course = ctx.getBodyAsJson();
        mongo.insert("courses", course, res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(201).end("Course added.");
            } else {
                ctx.response().setStatusCode(500).end("Failed to add course.");
            }
        });
    }

    private void listCourses(RoutingContext ctx) {
        mongo.find("courses", new JsonObject(), res -> {
            if (res.succeeded()) {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(res.result().toString());
            } else {
                ctx.response().setStatusCode(500).end("Failed to list courses.");
            }
        });
    }
}
