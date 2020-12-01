package com.mycompany.bookservice;

import com.mycompany.bookservice.dto.BookDto;
import com.mycompany.bookservice.dto.CreateBookDto;
import com.mycompany.bookservice.dto.UpdateBookDto;
import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.repository.BookRepository;
import lombok.Data;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static com.mycompany.bookservice.helper.BookServiceTestHelper.getDefaultBook;
import static com.mycompany.bookservice.helper.BookServiceTestHelper.getDefaultCreateBookDto;
import static com.mycompany.bookservice.helper.BookServiceTestHelper.getDefaultUpdateBookDto;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class RandomPortTestRestTemplateTests extends AbstractTestcontainers {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void givenNoBooksWhenGetAllBooksThenReturnStatusOkAndEmptyArray() {
        ResponseEntity<BookDto[]> responseEntity = testRestTemplate.getForEntity(API_BOOKS_URL, BookDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEmpty();
    }

    @Test
    void givenOneBookWhenGetAllBooksThenReturnStatusOkAndArrayWithOneBook() {
        Book book = getDefaultBook();
        bookRepository.save(book);

        ResponseEntity<BookDto[]> responseEntity = testRestTemplate.getForEntity(API_BOOKS_URL, BookDto[].class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).hasSize(1);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()[0].getId()).isEqualTo(book.getId());
        assertThat(responseEntity.getBody()[0].getAuthorName()).isEqualTo(book.getAuthorName());
        assertThat(responseEntity.getBody()[0].getTitle()).isEqualTo(book.getTitle());
        assertThat(responseEntity.getBody()[0].getPrice()).isEqualTo(book.getPrice());
    }

    @Test
    void givenValidBookWhenCreateBookWithoutAuthenticationThenReturnStatus302() {
        CreateBookDto createBookDto = getDefaultCreateBookDto();
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(API_BOOKS_URL, createBookDto, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    void givenValidBookWhenCreateBookInformingInvalidTokenThenReturnStatusUnauthorized() {
        CreateBookDto createBookDto = getDefaultCreateBookDto();

        HttpHeaders headers = authBearerHeaders("abcdef");
        ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(API_BOOKS_URL, new HttpEntity<>(createBookDto, headers), String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getBody()).isNull();
    }

    @Test
    void givenValidBookWhenCreateBookAuthenticatedThenReturnStatusCreatedAndBookJson() {
        CreateBookDto createBookDto = getDefaultCreateBookDto();

        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();

        HttpHeaders headers = authBearerHeaders(accessToken);
        ResponseEntity<BookDto> responseEntity = testRestTemplate.postForEntity(API_BOOKS_URL, new HttpEntity<>(createBookDto, headers), BookDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isNotNull();
        assertThat(responseEntity.getBody().getAuthorName()).isEqualTo(createBookDto.getAuthorName());
        assertThat(responseEntity.getBody().getTitle()).isEqualTo(createBookDto.getTitle());
        assertThat(responseEntity.getBody().getPrice()).isEqualTo(createBookDto.getPrice());
    }

    @Test
    void givenNonExistingBookIdWhenUpdateBookThenReturnStatusNotFound() {
        UUID id = UUID.randomUUID();
        UpdateBookDto updateBookDto = getDefaultUpdateBookDto();

        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();
        HttpHeaders headers = authBearerHeaders(accessToken);

        String url = String.format(API_BOOKS_ID_URL, id);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(url, HttpMethod.PATCH,
                new HttpEntity<>(updateBookDto, headers), MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_NOT_FOUND);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Book with id '" + id + "' not found.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo(url);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void givenExistingBookWhenUpdateBookThenReturnStatusOkAndBookJson() {
        Book book = getDefaultBook();
        bookRepository.save(book);

        UpdateBookDto updateBookDto = new UpdateBookDto();
        updateBookDto.setAuthorName("Ivan Franchin 2");
        updateBookDto.setTitle("Java 9");

        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();
        HttpHeaders headers = authBearerHeaders(accessToken);

        String url = String.format(API_BOOKS_ID_URL, book.getId());
        ResponseEntity<BookDto> responseEntity = testRestTemplate.exchange(url, HttpMethod.PATCH,
                new HttpEntity<>(updateBookDto, headers), BookDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isNotNull();
        assertThat(responseEntity.getBody().getAuthorName()).isEqualTo(updateBookDto.getAuthorName());
        assertThat(responseEntity.getBody().getTitle()).isEqualTo(updateBookDto.getTitle());
        assertThat(responseEntity.getBody().getPrice()).isEqualTo(book.getPrice());
    }

    @Test
    void givenNonExistingBookIdWhenDeleteBookThenReturnStatusNotFound() {
        UUID id = UUID.randomUUID();
        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();

        HttpHeaders headers = authBearerHeaders(accessToken);

        String url = String.format(API_BOOKS_ID_URL, id);
        ResponseEntity<MessageError> responseEntity = testRestTemplate.exchange(url, HttpMethod.DELETE,
                new HttpEntity<>(headers), MessageError.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isNotEmpty();
        assertThat(responseEntity.getBody().getStatus()).isEqualTo(404);
        assertThat(responseEntity.getBody().getError()).isEqualTo(ERROR_NOT_FOUND);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Book with id '" + id + "' not found.");
        assertThat(responseEntity.getBody().getPath()).isEqualTo(url);
        assertThat(responseEntity.getBody().getErrors()).isNull();
    }

    @Test
    void givenExistingBookIdWhenDeleteBookThenReturnStatusOkAndBookJson() {
        Book book = getDefaultBook();
        bookRepository.save(book);

        String accessToken = keycloakBookService.tokenManager().grantToken().getToken();
        HttpHeaders headers = authBearerHeaders(accessToken);

        String url = String.format(API_BOOKS_ID_URL, book.getId());
        ResponseEntity<BookDto> responseEntity = testRestTemplate.exchange(url, HttpMethod.DELETE,
                new HttpEntity<>(headers), BookDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getId()).isNotNull();
        assertThat(responseEntity.getBody().getAuthorName()).isEqualTo(book.getAuthorName());
        assertThat(responseEntity.getBody().getTitle()).isEqualTo(book.getTitle());
        assertThat(responseEntity.getBody().getPrice()).isEqualTo(book.getPrice());
    }

    private HttpHeaders authBearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    @Data
    private static class MessageError {
        private String timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private String errorCode;
        private List<ErrorDetail> errors;

        @Data
        public static class ErrorDetail {
            private List<String> codes;
            private String defaultMessage;
            private String objectName;
            private String field;
            private String rejectedValue;
            private boolean bindingFailure;
            private String code;
        }
    }

    private static final String API_BOOKS_URL = "/api/books";
    private static final String API_BOOKS_ID_URL = "/api/books/%s";
    private static final String ERROR_NOT_FOUND = "Not Found";

}
