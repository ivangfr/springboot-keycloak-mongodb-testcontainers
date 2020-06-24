package com.mycompany.bookservice;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Collections;

@Testcontainers
public class ContainersExtension implements BeforeAllCallback, AfterAllCallback {

    @Container
    private static GenericContainer<?> mongoDBContainer;

    @Container
    private static GenericContainer<?> keycloakContainer;

    @Override
    public void beforeAll(ExtensionContext context) {

        // MongoDB
        // ---
        // As "bitnami/mongodb" docker image is used, we needed to configure mongoDBContainer as GenericContainer.
        // MongoDBContainer is used for "mongo" official docker image
        // ---
        mongoDBContainer = new GenericContainer<>("bitnami/mongodb:4.2.7");
        mongoDBContainer.setNetworkAliases(Collections.singletonList("mongodb"));
        mongoDBContainer.setPortBindings(Collections.singletonList("27017:27017"));
        mongoDBContainer.setWaitStrategy(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));
        mongoDBContainer.start();

        // Keycloak
        // ---
        keycloakContainer = new GenericContainer<>("jboss/keycloak:10.0.2")
                .withEnv("KEYCLOAK_USER", "admin")
                .withEnv("KEYCLOAK_PASSWORD", "admin")
                .withEnv("DB_VENDOR", "h2");
        keycloakContainer.setNetworkAliases(Collections.singletonList("keycloak"));
        keycloakContainer.setPortBindings(Collections.singletonList("8080:8080"));
        keycloakContainer.setWaitStrategy(Wait.forHttp("/auth").forPort(8080).withStartupTimeout(Duration.ofMinutes(2)));
        keycloakContainer.start();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        mongoDBContainer.stop();
        keycloakContainer.stop();
    }

}
