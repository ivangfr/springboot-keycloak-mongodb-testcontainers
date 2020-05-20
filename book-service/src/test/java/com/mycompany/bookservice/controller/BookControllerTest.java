package com.mycompany.bookservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.bookservice.dto.CreateBookDto;
import com.mycompany.bookservice.dto.UpdateBookDto;
import com.mycompany.bookservice.exception.BookNotFoundException;
import com.mycompany.bookservice.mapper.BookMapperImpl;
import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.service.BookService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static com.mycompany.bookservice.helper.BookServiceTestHelper.getDefaultBook;
import static com.mycompany.bookservice.helper.BookServiceTestHelper.getDefaultCreateBookDto;
import static com.mycompany.bookservice.helper.BookServiceTestHelper.getDefaultUpdateBookDto;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@ExtendWith(SpringExtension.class)
@WebMvcTest(BookController.class)
@Import(BookMapperImpl.class) // if BookMapperImpl.class is missing, run: ./gradlew book-service:assemble
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenNoBookWhenGetAllBooksThenReturnStatusOkAndEmptyJsonArray() throws Exception {
        given(bookService.getAllBooks()).willReturn(Collections.emptyList());

        ResultActions resultActions = mockMvc.perform(get(API_BOOKS_URL))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void givenOneBookWhenGetAllBooksThenReturnStatusOkAndJsonArrayWithOneBook() throws Exception {
        Book book = getDefaultBook();
        given(bookService.getAllBooks()).willReturn(Collections.singletonList(book));

        ResultActions resultActions = mockMvc.perform(get(API_BOOKS_URL))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_$, hasSize(1)))
                .andExpect(jsonPath(JSON_$_0_ID, is(book.getId().toString())))
                .andExpect(jsonPath(JSON_$_0_AUTHOR_NAME, is(book.getAuthorName())))
                .andExpect(jsonPath(JSON_$_0_TITLE, is(book.getTitle())))
                .andExpect(jsonPath(JSON_$_0_PRICE, is(book.getPrice().doubleValue())));
    }

    @Test
    void givenExistingBookIdWhenGetBookByIdThenReturnStatusOkAndBookJson() throws Exception {
        Book book = getDefaultBook();
        given(bookService.validateAndGetBookById(book.getId())).willReturn(book);

        ResultActions resultActions = mockMvc.perform(get(API_BOOKS_ID_URL, book.getId()))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_$_ID, is(book.getId().toString())))
                .andExpect(jsonPath(JSON_$_AUTHOR_NAME, is(book.getAuthorName())))
                .andExpect(jsonPath(JSON_$_TITLE, is(book.getTitle())))
                .andExpect(jsonPath(JSON_$_PRICE, is(book.getPrice().doubleValue())));
    }

    @Test
    @WithMockUser(roles = MANAGE_BOOKS)
    void givenValidBookWhenCreateBookThenReturnStatusCreatedAndBookJson() throws Exception {
        CreateBookDto createBookDto = getDefaultCreateBookDto();
        Book book = getDefaultBook();

        given(bookService.saveBook(any(Book.class))).willReturn(book);

        ResultActions resultActions = mockMvc.perform(post(API_BOOKS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBookDto)))
                .andDo(print());

        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_$_ID, is(book.getId().toString())))
                .andExpect(jsonPath(JSON_$_AUTHOR_NAME, is(book.getAuthorName())))
                .andExpect(jsonPath(JSON_$_TITLE, is(book.getTitle())))
                .andExpect(jsonPath(JSON_$_PRICE, is(book.getPrice().doubleValue())));
    }

    @Test
    @WithMockUser(roles = MANAGE_BOOKS)
    void givenExistingBookIdWhenUpdateBookThenReturnStatusOkAndBookJsonUpdated() throws Exception {
        Book book = getDefaultBook();

        UpdateBookDto updateBookDto = new UpdateBookDto();
        updateBookDto.setPrice(new BigDecimal("99.99"));
        updateBookDto.setTitle("Java 9");

        given(bookService.validateAndGetBookById(book.getId())).willReturn(book);
        given(bookService.saveBook(any(Book.class))).willReturn(book);

        ResultActions resultActions = mockMvc.perform(patch(API_BOOKS_ID_URL, book.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBookDto)))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_$_ID, is(book.getId().toString())))
                .andExpect(jsonPath(JSON_$_AUTHOR_NAME, is(book.getAuthorName())))
                .andExpect(jsonPath(JSON_$_TITLE, is(updateBookDto.getTitle())))
                .andExpect(jsonPath(JSON_$_PRICE, is(updateBookDto.getPrice().doubleValue())));
    }

    @Test
    @WithMockUser(roles = MANAGE_BOOKS)
    void givenNonExistingBookIdWhenUpdateBookThenReturnStatusNotFound() throws Exception {
        UpdateBookDto updateBookDto = getDefaultUpdateBookDto();

        willThrow(BookNotFoundException.class).given(bookService).validateAndGetBookById(any(UUID.class));

        ResultActions resultActions = mockMvc.perform(patch(API_BOOKS_ID_URL, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBookDto)))
                .andDo(print());

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = MANAGE_BOOKS)
    void givenExistingBookIdWhenDeleteBookThenReturnStatusOkAndBookJson() throws Exception {
        Book book = getDefaultBook();

        given(bookService.validateAndGetBookById(book.getId())).willReturn(book);
        willDoNothing().given(bookService).deleteBook(any(Book.class));

        ResultActions resultActions = mockMvc.perform(delete(API_BOOKS_ID_URL, book.getId()))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_$_ID, is(book.getId().toString())))
                .andExpect(jsonPath(JSON_$_AUTHOR_NAME, is(book.getAuthorName())))
                .andExpect(jsonPath(JSON_$_TITLE, is(book.getTitle())))
                .andExpect(jsonPath(JSON_$_PRICE, is(book.getPrice().doubleValue())));
    }

    @Test
    @WithMockUser(roles = MANAGE_BOOKS)
    void givenNonExistingBookIdWhenDeleteBookThenReturnStatusNotFound() throws Exception {
        willThrow(BookNotFoundException.class).given(bookService).validateAndGetBookById(any(UUID.class));

        ResultActions resultActions = mockMvc.perform(delete(API_BOOKS_ID_URL, UUID.randomUUID()))
                .andDo(print());

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = FAKE_ROLE)
    void givenAUserWithInvalidRolesWhenCreateBookThenReturnStatusForbidden() throws Exception {
        CreateBookDto createBookDto = getDefaultCreateBookDto();

        ResultActions resultActions = mockMvc.perform(post(API_BOOKS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBookDto)))
                .andDo(print());

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = FAKE_ROLE)
    void givenAUserWithInvalidRolesWhenUpdateBookThenReturnStatusForbidden() throws Exception {
        UpdateBookDto updateBookDto = new UpdateBookDto();
        updateBookDto.setPrice(new BigDecimal("99.99"));
        updateBookDto.setTitle("Java 9");

        ResultActions resultActions = mockMvc.perform(patch(API_BOOKS_ID_URL, UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBookDto)))
                .andDo(print());

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = FAKE_ROLE)
    void givenAUserWithInvalidRolesWhenDeleteBookReturnStatusForbidden() throws Exception {
        ResultActions resultActions = mockMvc.perform(delete(API_BOOKS_ID_URL, UUID.randomUUID()))
                .andDo(print());

        resultActions.andExpect(status().isForbidden());
    }

    private static final String MANAGE_BOOKS = "manage_books";
    private static final String FAKE_ROLE = "fake_role";

    private static final String API_BOOKS_URL = "/api/books";
    private static final String API_BOOKS_ID_URL = "/api/books/{id}";

    private static final String JSON_$ = "$";

    private static final String JSON_$_ID = "$.id";
    private static final String JSON_$_AUTHOR_NAME = "$.authorName";
    private static final String JSON_$_TITLE = "$.title";
    private static final String JSON_$_PRICE = "$.price";

    private static final String JSON_$_0_ID = "$[0].id";
    private static final String JSON_$_0_AUTHOR_NAME = "$[0].authorName";
    private static final String JSON_$_0_TITLE = "$[0].title";
    private static final String JSON_$_0_PRICE = "$[0].price";

}