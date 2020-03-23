package com.mycompany.bookservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.mycompany.bookservice.config.KeycloakConfig;
import com.mycompany.bookservice.dto.CreateBookDto;
import com.mycompany.bookservice.dto.UpdateBookDto;
import com.mycompany.bookservice.exception.BookNotFoundException;
import com.mycompany.bookservice.mapper.BookMapperImpl;
import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.service.BookService;
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

@ExtendWith(SpringExtension.class)
@WebMvcTest(BookController.class)
@Import({BookMapperImpl.class, KeycloakConfig.class}) // <-- if this class is missing, run: ./gradlew book-service:assemble
public class BookControllerTest {

    private static final String MANAGE_BOOKS = "manage_books";
    private static final String FAKE_ROLE = "fake_role";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenNoBookWhenGetAllBooksThenReturnStatusOkAndEmptyJsonArray() throws Exception {
        given(bookService.getAllBooks()).willReturn(Collections.emptyList());

        ResultActions resultActions = mockMvc.perform(get("/api/books"))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void givenOneBookWhenGetAllBooksThenReturnStatusOkAndJsonArrayWithOneBook() throws Exception {
        Book book = getDefaultBook();
        given(bookService.getAllBooks()).willReturn(Lists.newArrayList(book));

        ResultActions resultActions = mockMvc.perform(get("/api/books"))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(book.getId().toString())))
                .andExpect(jsonPath("$[0].authorName", is(book.getAuthorName())))
                .andExpect(jsonPath("$[0].title", is(book.getTitle())))
                .andExpect(jsonPath("$[0].price", is(book.getPrice().doubleValue())));
    }

    @Test
    void givenExistingBookIdWhenGetBookByIdThenReturnStatusOkAndBookJson() throws Exception {
        Book book = getDefaultBook();
        given(bookService.validateAndGetBookById(book.getId())).willReturn(book);

        ResultActions resultActions = mockMvc.perform(get("/api/books/" + book.getId()))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(book.getId().toString())))
                .andExpect(jsonPath("$.authorName", is(book.getAuthorName())))
                .andExpect(jsonPath("$.title", is(book.getTitle())))
                .andExpect(jsonPath("$.price", is(book.getPrice().doubleValue())));
    }

    @Test
    @WithMockUser(roles = MANAGE_BOOKS)
    void givenValidBookWhenCreateBookThenReturnStatusCreatedAndBookJson() throws Exception {
        CreateBookDto createBookDto = getDefaultCreateBookDto();
        Book book = getDefaultBook();

        given(bookService.saveBook(any(Book.class))).willReturn(book);

        ResultActions resultActions = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createBookDto)))
                .andDo(print());

        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(book.getId().toString())))
                .andExpect(jsonPath("$.authorName", is(book.getAuthorName())))
                .andExpect(jsonPath("$.title", is(book.getTitle())))
                .andExpect(jsonPath("$.price", is(book.getPrice().doubleValue())));
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

        ResultActions resultActions = mockMvc.perform(patch("/api/books/" + book.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBookDto)))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(book.getId().toString())))
                .andExpect(jsonPath("$.authorName", is(book.getAuthorName())))
                .andExpect(jsonPath("$.title", is(updateBookDto.getTitle())))
                .andExpect(jsonPath("$.price", is(updateBookDto.getPrice().doubleValue())));
    }

    @Test
    @WithMockUser(roles = MANAGE_BOOKS)
    void givenNonExistingBookIdWhenUpdateBookThenReturnStatusNotFound() throws Exception {
        UpdateBookDto updateBookDto = getDefaultUpdateBookDto();

        willThrow(BookNotFoundException.class).given(bookService).validateAndGetBookById(any(UUID.class));

        ResultActions resultActions = mockMvc.perform(patch("/api/books/" + UUID.randomUUID())
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

        ResultActions resultActions = mockMvc.perform(delete("/api/books/" + book.getId()))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(book.getId().toString())))
                .andExpect(jsonPath("$.authorName", is(book.getAuthorName())))
                .andExpect(jsonPath("$.title", is(book.getTitle())))
                .andExpect(jsonPath("$.price", is(book.getPrice().doubleValue())));
    }

    @Test
    @WithMockUser(roles = MANAGE_BOOKS)
    void givenNonExistingBookIdWhenDeleteBookThenReturnStatusNotFound() throws Exception {
        willThrow(BookNotFoundException.class).given(bookService).validateAndGetBookById(any(UUID.class));

        ResultActions resultActions = mockMvc.perform(delete("/api/books/" + UUID.randomUUID()))
                .andDo(print());

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = FAKE_ROLE)
    void givenAUserWithInvalidRolesWhenCreateBookThenReturnStatusForbidden() throws Exception {
        CreateBookDto createBookDto = getDefaultCreateBookDto();

        ResultActions resultActions = mockMvc.perform(post("/api/books")
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

        ResultActions resultActions = mockMvc.perform(patch("/api/books/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateBookDto)))
                .andDo(print());

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = FAKE_ROLE)
    void givenAUserWithInvalidRolesWhenDeleteBookReturnStatusForbidden() throws Exception {
        ResultActions resultActions = mockMvc.perform(delete("/api/books/" + UUID.randomUUID()))
                .andDo(print());

        resultActions.andExpect(status().isForbidden());
    }

}