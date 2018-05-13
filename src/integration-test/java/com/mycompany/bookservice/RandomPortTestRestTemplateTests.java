package com.mycompany.bookservice;

import com.google.common.collect.Lists;
import com.mycompany.bookservice.dto.BookDto;
import com.mycompany.bookservice.dto.CreateBookDto;
import com.mycompany.bookservice.dto.UpdateBookDto;
import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.repository.BookRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.mycompany.bookservice.helper.BookServiceTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class RandomPortTestRestTemplateTests {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private Keycloak keycloakAdmin;
    private Keycloak keycloakBookService;

    @Before
    public void setUp() {
        String serverUrl = "http://localhost:8181/auth";
        keycloakAdmin = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .username("admin")
                .password("admin")
                .clientId("admin-cli")
                .build();

        // Realm
        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm("company-services");
        realmRepresentation.setEnabled(true);

        // Client
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setId("book-service");
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        clientRepresentation.setSecret("abc123");
        realmRepresentation.setClients(Lists.newArrayList(clientRepresentation));

        // Client roles
        Map<String, List<String>> clientRoles = new HashMap<>();
        clientRoles.put("book-service", Lists.newArrayList("manage_books"));

        // Credentials
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue("123");

        // User
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("ivan.franchin");
        userRepresentation.setEnabled(true);
        userRepresentation.setCredentials(Lists.newArrayList(credentialRepresentation));
        userRepresentation.setClientRoles(clientRoles);
        realmRepresentation.setUsers(Lists.newArrayList(userRepresentation));

        keycloakAdmin.realms().create(realmRepresentation);

        keycloakBookService = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("company-services")
                .username("ivan.franchin")
                .password("123")
                .clientId("book-service")
                .clientSecret("abc123")
                .build();
    }

    @After
    public void tearDown() {
        keycloakAdmin.realm("company-services").remove();
    }

    /*
     * GET /api/books
     * ============== */

    @Test
    public void given_noBooks_when_getAllBooks_then_returnEmptyArray() {
        ResponseEntity<BookDto[]> responseEntity = testRestTemplate.getForEntity("/api/books", BookDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasSize(0);
    }

    @Test
    public void given_oneBook_when_getAllBooks_then_returnArrayWithBook() {
        Book book = getDefaultBook();
        bookRepository.save(book);

        ResponseEntity<BookDto[]> responseEntity = testRestTemplate.getForEntity("/api/books", BookDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasSize(1);
        assertThat(responseEntity.getBody()[0].getId()).isEqualTo(book.getId());
        assertThat(responseEntity.getBody()[0].getAuthorName()).isEqualTo(book.getAuthorName());
        assertThat(responseEntity.getBody()[0].getTitle()).isEqualTo(book.getTitle());
        assertThat(responseEntity.getBody()[0].getPrice()).isEqualTo(book.getPrice());
    }

    /*
     * POST /api/books
     * =============== */

    @Test
    public void given_noBooks_when_createBookWithoutAuthentication_then_returnStatus302() {
        CreateBookDto createBookDto = getDefaultCreateBookDto();
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(
                "/api/books", createBookDto, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    public void given_noBooks_when_createBookInformingInvalidToken_then_returnBookJson() {
        String accessToken = "abcdef";

        CreateBookDto createBookDto = getDefaultCreateBookDto();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(
                "/api/books", new HttpEntity<>(createBookDto, headers), String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    public void given_noBooks_when_createBookAuthenticated_then_returnBookJson() {
        CreateBookDto createBookDto = getDefaultCreateBookDto();

        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<BookDto> responseEntity = testRestTemplate.postForEntity(
                "/api/books", new HttpEntity<>(createBookDto, headers), BookDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody().getId()).isNotNull();
        assertThat(responseEntity.getBody().getAuthorName()).isEqualTo(createBookDto.getAuthorName());
        assertThat(responseEntity.getBody().getTitle()).isEqualTo(createBookDto.getTitle());
        assertThat(responseEntity.getBody().getPrice()).isEqualTo(createBookDto.getPrice());
    }

    /*
     * PATCH /api/books/{id}
     * ===================== */

    @Test
    public void given_noBook_when_updateBook_then_returnNotFound() {
        UUID id = UUID.randomUUID();
        UpdateBookDto updateBookDto = getDefaultUpdateBookDto();

        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(
                "/api/books/" + id, HttpMethod.PATCH,
                new HttpEntity<>(updateBookDto, headers), MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Not Found");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Book with id '" + id + "' not found.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo("/api/books/" + id);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    public void given_oneBook_when_updateBook_then_returnBookJson() {
        Book book = getDefaultBook();
        bookRepository.save(book);

        UpdateBookDto updateBookDto = new UpdateBookDto();
        updateBookDto.setAuthorName("Ivan Franchin Jr.");
        updateBookDto.setTitle("Java 9");

        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<BookDto> responseEntity = testRestTemplate.exchange(
                "/api/books/" + book.getId(), HttpMethod.PATCH,
                new HttpEntity<>(updateBookDto, headers), BookDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getId()).isNotNull();
        assertThat(responseEntity.getBody().getAuthorName()).isEqualTo(updateBookDto.getAuthorName());
        assertThat(responseEntity.getBody().getTitle()).isEqualTo(updateBookDto.getTitle());
        assertThat(responseEntity.getBody().getPrice()).isEqualTo(book.getPrice());
    }

    /*
     * DELETE /api/books/{id}
     * ====================== */

    @Test
    public void given_noBook_when_deleteBook_then_returnNotFound() {
        UUID id = UUID.randomUUID();
        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(
                "/api/books/" + id, HttpMethod.DELETE,
                new HttpEntity<>(headers), MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo("Not Found");
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Book with id '" + id + "' not found.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo("/api/books/" + id);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    public void given_oneBook_when_deleteBook_then_returnBookJson() {
        Book book = getDefaultBook();
        bookRepository.save(book);

        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<BookDto> responseEntity = testRestTemplate.exchange(
                "/api/books/" + book.getId(), HttpMethod.DELETE,
                new HttpEntity<>(headers), BookDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getId()).isNotNull();
        assertThat(responseEntity.getBody().getAuthorName()).isEqualTo(book.getAuthorName());
        assertThat(responseEntity.getBody().getTitle()).isEqualTo(book.getTitle());
        assertThat(responseEntity.getBody().getPrice()).isEqualTo(book.getPrice());
    }

}
