package org.example.handlers;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.example.utils.EmailUtil;
import org.example.utils.PasswordUtil;

public class RegistrationHandler {
    private final Vertx vertx;
    private final MongoClient mongo;

    public RegistrationHandler(Vertx vertx, MongoClient mongo) {
        this.vertx = vertx;
        this.mongo = mongo;
    }



    public void setup(Router router) {
        router.post("/register-student").handler(this::registerStudent);
        router.post("/login").handler(this::login);
        router.post("/register-course").handler(this::registerCourse);
    }

    private void registerStudent(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String name = body.getString("name");
        String email = body.getString("email");

        // ✅ Generate and hash the password
        String plainPassword = PasswordUtil.generateRandomPassword(8);
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        JsonObject student = new JsonObject()
                .put("name", name)
                .put("email", email)
                .put("password", hashedPassword);

        mongo.insert("students", student, res -> {
            if (res.succeeded()) {
                vertx.executeBlocking(promise -> {
                    try {
                        EmailUtil.sendEmail(
                                email,
                                "Your Elective System Password",
                                "Hi " + name + ",\n\nYour password is: " + plainPassword
                        );
                        promise.complete();
                    } catch (Exception e) {
                        promise.fail(e);
                    }
                }, emailAsyncResult -> {
                    if (emailAsyncResult.succeeded()) {
                        ctx.response().end("Registered and password emailed.");
                    } else {
                        ctx.response().setStatusCode(500).end("Email sending failed.");
                    }
                });
            } else {
                ctx.response().setStatusCode(500).end("Registration failed.");
            }
        });

    }

    private void login(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String email = body.getString("email");
        String inputPassword = body.getString("password");

        mongo.findOne("students", new JsonObject().put("email", email), null, res -> {
            if (res.succeeded() && res.result() != null) {
                String storedHash = res.result().getString("password");
                if (PasswordUtil.checkPassword(inputPassword, storedHash)) {
                    ctx.response().end("Login successful");
                } else {
                    ctx.response().setStatusCode(401).end("Invalid password");
                }
            } else {
                ctx.response().setStatusCode(404).end("Student not found");
            }
        });
    }

    private void registerCourse(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String email = body.getString("email");
        String courseId = body.getString("courseId");

        mongo.findOne("courses", new JsonObject().put("_id", courseId), null, courseRes -> {
            if (courseRes.succeeded() && courseRes.result() != null) {
                JsonObject course = courseRes.result();
                int seats = course.getInteger("capacity");
                if (seats > 0) {
                    // Update student
                    mongo.updateCollection("students",
                            new JsonObject().put("email", email),
                            new JsonObject().put("$set", new JsonObject().put("courseId", courseId)),
                            updateRes -> {
                                if (updateRes.succeeded()) {
                                    // Decrease seat
                                    mongo.updateCollection("courses",
                                            new JsonObject().put("_id", courseId),
                                            new JsonObject().put("$inc", new JsonObject().put("capacity", -1)),
                                            courseUpdate -> {
                                                if (courseUpdate.succeeded()) {
                                                    ctx.response().end("Course registered.");
                                                } else {
                                                    ctx.response().setStatusCode(500).end("Seat update failed.");
                                                }
                                            });
                                } else {
                                    ctx.response().setStatusCode(500).end("Student update failed.");
                                }
                            });
                } else {
                    ctx.response().setStatusCode(400).end("No seats available.");
                }
            } else {
                ctx.response().setStatusCode(404).end("Course not found.");
            }
        });
    }
}
