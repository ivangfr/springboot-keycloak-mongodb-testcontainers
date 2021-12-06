package com.mycompany.bookservice;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Testcontainers
public abstract class AbstractTestcontainers {

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.4");
    private static final GenericContainer<?> keycloakContainer = new GenericContainer<>("jboss/keycloak:15.0.2");
    protected static Keycloak keycloakBookService;

    @DynamicPropertySource
    private static void dynamicProperties(DynamicPropertyRegistry registry) {
        mongoDBContainer.start();

        keycloakContainer.withExposedPorts(8080)
                .withEnv("KEYCLOAK_USER", "admin")
                .withEnv("KEYCLOAK_PASSWORD", "admin")
                .withEnv("DB_VENDOR", "h2")
                .waitingFor(Wait.forHttp("/auth").forPort(8080).withStartupTimeout(Duration.ofMinutes(2)))
                .start();

        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));

        String keycloakServerUrl = String.format("http://%s:%s/auth", keycloakContainer.getHost(), keycloakContainer.getMappedPort(8080));
        registry.add("keycloak.auth-server-url", () -> keycloakServerUrl);

        if (keycloakBookService == null) {
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
