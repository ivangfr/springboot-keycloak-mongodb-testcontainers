package com.ivanfranchin.bookservice;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Testcontainers
public abstract class AbstractTestcontainers {

    @Container
    @ServiceConnection
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.6");

    private static final GenericContainer<?> keycloakContainer = new GenericContainer<>("quay.io/keycloak/keycloak:23.0.7");

    protected static Keycloak keycloakBookService;

    @DynamicPropertySource
    private static void dynamicProperties(DynamicPropertyRegistry registry) {
        keycloakContainer.withExposedPorts(8080)
                .withEnv("KEYCLOAK_ADMIN", "admin")
                .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
                .withEnv("KC_DB", "dev-mem")
                .withCommand("start-dev")
                .waitingFor(Wait.forHttp("/admin").forPort(8080).withStartupTimeout(Duration.ofMinutes(2)))
                .start();

        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));

        String keycloakHost = keycloakContainer.getHost();
        Integer keycloakPort = keycloakContainer.getMappedPort(8080);

        String issuerUri = String.format("http://%s:%s/realms/company-services", keycloakHost, keycloakPort);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuerUri);

        if (keycloakBookService == null) {
            String keycloakServerUrl = String.format("http://%s:%s", keycloakHost, keycloakPort);
            setupKeycloak(keycloakServerUrl);
        }
    }

    private static void setupKeycloak(String keycloakServerUrl) {
        Keycloak keycloakAdmin = KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm("master")
                .username("admin")
                .password("admin")
                .clientId("admin-cli")
                .build();

        // Realm
        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(COMPANY_SERVICE_REALM_NAME);
        realmRepresentation.setEnabled(true);

        // Client
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setId(BOOK_SERVICE_CLIENT_ID);
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        clientRepresentation.setSecret(BOOK_SERVICE_CLIENT_SECRET);
        realmRepresentation.setClients(Collections.singletonList(clientRepresentation));

        // Client roles
        Map<String, List<String>> clientRoles = new HashMap<>();
        clientRoles.put(BOOK_SERVICE_CLIENT_ID, BOOK_SERVICE_ROLES);

        // Credentials
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(USER_PASSWORD);

        // User
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(USER_USERNAME);
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
        userRepresentation.setClientRoles(clientRoles);
        realmRepresentation.setUsers(Collections.singletonList(userRepresentation));

        keycloakAdmin.realms().create(realmRepresentation);

        keycloakBookService = KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm(COMPANY_SERVICE_REALM_NAME)
                .username(USER_USERNAME)
                .password(USER_PASSWORD)
                .clientId(BOOK_SERVICE_CLIENT_ID)
                .clientSecret(BOOK_SERVICE_CLIENT_SECRET)
                .build();
    }

    private static final String COMPANY_SERVICE_REALM_NAME = "company-services";
    private static final String BOOK_SERVICE_CLIENT_ID = "book-service";
    private static final String BOOK_SERVICE_CLIENT_SECRET = "abc123";
    private static final List<String> BOOK_SERVICE_ROLES = Collections.singletonList("manage_books");
    private static final String USER_USERNAME = "ivan.franchin";
    private static final String USER_PASSWORD = "123";
}
