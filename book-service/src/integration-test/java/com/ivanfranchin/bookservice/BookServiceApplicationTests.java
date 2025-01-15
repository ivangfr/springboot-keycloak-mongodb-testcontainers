package com.ivanfranchin.bookservice;

import com.ivanfranchin.bookservice.dto.BookResponse;
import com.ivanfranchin.bookservice.dto.CreateBookRequest;
import com.ivanfranchin.bookservice.dto.UpdateBookRequest;
import com.ivanfranchin.bookservice.model.Book;
import com.ivanfranchin.bookservice.repository.BookRepository;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BookServiceApplicationTests extends AbstractTestcontainers {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    void testGetBooksWhenThereIsNone() {
        ResponseEntity<BookResponse[]> responseEntity = testRestTemplate.getForEntity(API_BOOKS_URL, BookResponse[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEmpty();
    }

    @Test
    void testGetBooksWhenThereIsOne() {
        Book book = bookRepository.save(getDefaultBook());

        ResponseEntity<BookResponse[]> responseEntity = testRestTemplate.getForEntity(API_BOOKS_URL, BookResponse[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasSize(1);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()[0].id()).isEqualTo(book.getId());
        assertThat(responseEntity.getBody()[0].authorName()).isEqualTo(book.getAuthorName());
        assertThat(responseEntity.getBody()[0].title()).isEqualTo(book.getTitle());
        assertThat(responseEntity.getBody()[0].price()).isEqualTo(book.getPrice());
    }

    @Test
    void testCreateBookWithoutAuthentication() {
        CreateBookRequest createBookRequest = getDefaultCreateBookRequest();
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(API_BOOKS_URL, createBookRequest, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    void testCreateBookInformingInvalidToken() {
        CreateBookRequest createBookRequest = getDefaultCreateBookRequest();

        HttpHeaders headers = authBearerHeaders("abcdef");
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(API_BOOKS_URL, new HttpEntity<>(createBookRequest, headers), String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    void testCreateBookInformingValidToken() {
        CreateBookRequest createBookRequest = getDefaultCreateBookRequest();

        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();

        HttpHeaders headers = authBearerHeaders(accessToken);
        ResponseEntity<BookResponse> responseEntity = testRestTemplate.postForEntity(API_BOOKS_URL, new HttpEntity<>(createBookRequest, headers), BookResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().id()).isNotNull();
        assertThat(responseEntity.getBody().authorName()).isEqualTo(createBookRequest.authorName());
        assertThat(responseEntity.getBody().title()).isEqualTo(createBookRequest.title());
        assertThat(responseEntity.getBody().price()).isEqualTo(createBookRequest.price());

        Optional<Book> bookOptional = bookRepository.findById(responseEntity.getBody().id());
        assertThat(bookOptional.isPresent()).isTrue();
        bookOptional.ifPresent(bookCreated -> {
            assertThat(bookCreated.getAuthorName()).isEqualTo(createBookRequest.authorName());
            assertThat(bookCreated.getTitle()).isEqualTo(createBookRequest.title());
            assertThat(bookCreated.getPrice()).isEqualTo(createBookRequest.price());
        });
    }

    @Test
    void testUpdateBookWhenNonExistent() {
        UpdateBookRequest updateBookRequest = new UpdateBookRequest(null, "SpringBoot 2", null);

        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();
        HttpHeaders headers = authBearerHeaders(accessToken);

        String url = String.format(API_BOOKS_ID_URL, "123");
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(url, HttpMethod.PATCH,
                new HttpEntity<>(updateBookRequest, headers), MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_NOT_FOUND);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Book with id '123' not found.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo(url);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void testUpdateBookWhenExistent() {
        Book book = bookRepository.save(getDefaultBook());
        UpdateBookRequest updateBookRequest = new UpdateBookRequest("Ivan Franchin 2", "Java 9", null);

        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();
        HttpHeaders headers = authBearerHeaders(accessToken);

        String url = String.format(API_BOOKS_ID_URL, book.getId());
        ResponseEntity<BookResponse> responseEntity = testRestTemplate.exchange(url, HttpMethod.PATCH,
                new HttpEntity<>(updateBookRequest, headers), BookResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().id()).isNotNull();
        assertThat(responseEntity.getBody().authorName()).isEqualTo(updateBookRequest.authorName());
        assertThat(responseEntity.getBody().title()).isEqualTo(updateBookRequest.title());
        assertThat(responseEntity.getBody().price()).isEqualTo(book.getPrice());

        Optional<Book> bookOptional = bookRepository.findById(responseEntity.getBody().id());
        assertThat(bookOptional.isPresent()).isTrue();
        bookOptional.ifPresent(bookUpdated -> {
            assertThat(bookUpdated.getAuthorName()).isEqualTo(updateBookRequest.authorName());
            assertThat(bookUpdated.getTitle()).isEqualTo(updateBookRequest.title());
            assertThat(bookUpdated.getPrice()).isEqualTo(book.getPrice());
        });
    }

    @Test
    void testDeleteBookWhenNonExistent() {
        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();

        HttpHeaders headers = authBearerHeaders(accessToken);

        String url = String.format(API_BOOKS_ID_URL, "123");
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(url, HttpMethod.DELETE,
                new HttpEntity<>(headers), MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_NOT_FOUND);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Book with id '123' not found.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo(url);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void testDeleteBookWhenExistent() {
        Book book = bookRepository.save(getDefaultBook());

        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();
        HttpHeaders headers = authBearerHeaders(accessToken);

        String url = String.format(API_BOOKS_ID_URL, book.getId());
        ResponseEntity<BookResponse> responseEntity = testRestTemplate.exchange(url, HttpMethod.DELETE,
                new HttpEntity<>(headers), BookResponse.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().id()).isNotNull();
        assertThat(responseEntity.getBody().authorName()).isEqualTo(book.getAuthorName());
        assertThat(responseEntity.getBody().title()).isEqualTo(book.getTitle());
        assertThat(responseEntity.getBody().price()).isEqualTo(book.getPrice());

        Optional<Book> bookOptional = bookRepository.findById(responseEntity.getBody().id());
        assertThat(bookOptional.isPresent()).isFalse();
    }

    private HttpHeaders authBearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    private Book getDefaultBook() {
        return new Book("Ivan Franchin", "SpringBoot", BigDecimal.valueOf(29.99));
    }

    private CreateBookRequest getDefaultCreateBookRequest() {
        return new CreateBookRequest("Ivan Franchin", "SpringBoot", BigDecimal.valueOf(10.99));
    }

    @Value
    private static class MessageError {
        String timestamp;
        int status;
        String error;
        String message;
        String path;
        String errorCode;
        List<ErrorDetail> errors;

        @Value
        public static class ErrorDetail {
            List<String> codes;
            String defaultMessage;
            String objectName;
            String field;
            String rejectedValue;
            boolean bindingFailure;
            String code;
        }
    }

    private static final String API_BOOKS_URL = "/api/books";
    private static final String API_BOOKS_ID_URL = "/api/books/%s";
    private static final String ERROR_NOT_FOUND = "Not Found";
}
