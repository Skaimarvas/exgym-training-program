package com.exgym.training.cucumber.integration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.testcontainers.containers.MongoDBContainer;

final class ExternalWorkloadServiceManager {

    private static final int WORKLOAD_PORT = 18082;
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(3);
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7.0");

    private static Process workloadProcess;
    private static boolean started;

    private ExternalWorkloadServiceManager() {
    }

    static synchronized void ensureStarted() {
        if (started && workloadProcess != null && workloadProcess.isAlive()) {
            return;
        }

        if (!MONGO_DB_CONTAINER.isRunning()) {
            MONGO_DB_CONTAINER.start();
        }

        Path trainingRepo = Path.of(System.getProperty("user.dir"));
        Path workloadRepo = trainingRepo.resolve("../exgymworkload").normalize();
        Path logDirectory = trainingRepo.resolve("target/microservice-integration");
        Path logFile = logDirectory.resolve("workload-service.log");

        try {
            Files.createDirectories(logDirectory);
            ProcessBuilder processBuilder = new ProcessBuilder(buildCommand(workloadRepo, MONGO_DB_CONTAINER.getReplicaSetUrl()));
            processBuilder.directory(workloadRepo.toFile());
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(logFile.toFile());
            workloadProcess = processBuilder.start();
            waitForServiceReady();
            started = true;
        } catch (Exception exception) {
            stop();
            throw new IllegalStateException("Failed to start workload service for integration tests", exception);
        }
    }

    static synchronized void stop() {
        if (workloadProcess != null) {
            workloadProcess.destroy();
            try {
                workloadProcess.waitFor();
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        workloadProcess = null;
        started = false;
        if (MONGO_DB_CONTAINER.isRunning()) {
            MONGO_DB_CONTAINER.stop();
        }
    }

    private static List<String> buildCommand(Path workloadRepo, String mongoUri) {
        String script = workloadRepo.resolve("mvnw").toString();
        String arguments = String.join(" ",
                "--server.port=" + WORKLOAD_PORT,
                "--spring.data.mongodb.uri=" + mongoUri,
                "--eureka.client.enabled=false",
                "--eureka.client.register-with-eureka=false",
                "--eureka.client.fetch-registry=false",
                "--spring.activemq.broker-url=vm://embedded?broker.persistent=false");

        return List.of(script, "spring-boot:run", "-Dspring-boot.run.arguments=" + arguments);
    }

    private static void waitForServiceReady() throws IOException, InterruptedException {
        Instant deadline = Instant.now().plus(STARTUP_TIMEOUT);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + WORKLOAD_PORT + "/swagger-ui.html"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        while (Instant.now().isBefore(deadline)) {
            if (workloadProcess != null && !workloadProcess.isAlive()) {
                throw new IllegalStateException("Workload service process exited before becoming ready");
            }

            try {
                HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
                if (response.statusCode() >= 200 && response.statusCode() < 400) {
                    return;
                }
            } catch (IOException ignored) {
                // Retry until timeout.
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw interruptedException;
            }
        }

        throw new IllegalStateException("Timed out waiting for workload service to become ready");
    }
}