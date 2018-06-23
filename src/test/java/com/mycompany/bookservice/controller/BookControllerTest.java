package com.mycompany.bookservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.mycompany.bookservice.config.ModelMapperConfig;
import com.mycompany.bookservice.dto.CreateBookDto;
import com.mycompany.bookservice.dto.UpdateBookDto;
import com.mycompany.bookservice.exception.BookNotFoundException;
import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.UUID;

import static com.mycompany.bookservice.helper.BookServiceTestHelper.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BookController.class)
@Import(ModelMapperConfig.class)
@AutoConfigureMockMvc(secure = false)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Mock
    private Principal principal;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void given_noBook_when_getAllBooks_then_returnStatusOkAndEmptyJsonArray() throws Exception {
        given(bookService.getAllBooks()).willReturn(new ArrayList<>());

        ResultActions resultActions = mockMvc.perform(get("/api/books")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void given_oneBook_when_getAllBooks_then_returnStatusOkAndJsonArrayWithOneBook() throws Exception {
        Book book = getDefaultBook();
        given(bookService.getAllBooks()).willReturn(Lists.newArrayList(book));

        ResultActions resultActions = mockMvc.perform(get("/api/books")
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(book.getId().toString())))
                .andExpect(jsonPath("$[0].authorName", is(book.getAuthorName())))
                .andExpect(jsonPath("$[0].title", is(book.getTitle())))
                .andExpect(jsonPath("$[0].price", is(book.getPrice().doubleValue())));
    }

    @Test
    void given_existingBookId_when_getBookById_then_returnStatusOkAndBookJson() throws Exception {
        Book book = getDefaultBook();
        given(bookService.validateAndGetBookById(book.getId())).willReturn(book);

        ResultActions resultActions = mockMvc.perform(get("/api/books/" + book.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(book.getId().toString())))
                .andExpect(jsonPath("$.authorName", is(book.getAuthorName())))
                .andExpect(jsonPath("$.title", is(book.getTitle())))
                .andExpect(jsonPath("$.price", is(book.getPrice().doubleValue())));
    }

    @Test
    void given_validBook_when_createBook_then_returnStatusCreatedAndBookJson() throws Exception {
        CreateBookDto createBookDto = getDefaultCreateBookDto();
        Book book = getDefaultBook();

        given(principal.getName()).willReturn("ivan.franchin");
        given(bookService.saveBook(any(Book.class))).willReturn(book);

        ResultActions resultActions = mockMvc.perform(post("/api/books")
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(createBookDto)))
                .andDo(print());

        resultActions.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(book.getId().toString())))
                .andExpect(jsonPath("$.authorName", is(book.getAuthorName())))
                .andExpect(jsonPath("$.title", is(book.getTitle())))
                .andExpect(jsonPath("$.price", is(book.getPrice().doubleValue())));
    }

    @Test
    void given_existingBookId_when_updateBook_then_returnStatusOkAndBookJsonUpdated() throws Exception {
        Book book = getDefaultBook();

        UpdateBookDto updateBookDto = new UpdateBookDto();
        updateBookDto.setPrice(new BigDecimal("99.99"));
        updateBookDto.setTitle("Java 9");

        given(principal.getName()).willReturn("ivan.franchin");
        given(bookService.validateAndGetBookById(book.getId())).willReturn(book);
        given(bookService.saveBook(any(Book.class))).willReturn(book);

        ResultActions resultActions = mockMvc.perform(patch("/api/books/" + book.getId())
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(updateBookDto)))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(book.getId().toString())))
                .andExpect(jsonPath("$.authorName", is(book.getAuthorName())))
                .andExpect(jsonPath("$.title", is(updateBookDto.getTitle())))
                .andExpect(jsonPath("$.price", is(updateBookDto.getPrice().doubleValue())));
    }

    @Test
    void given_nonExistingBookId_when_updateBook_then_returnStatusNotFound() throws Exception {
        UpdateBookDto updateBookDto = getDefaultUpdateBookDto();

        given(principal.getName()).willReturn("ivan.franchin");
        willThrow(BookNotFoundException.class).given(bookService).validateAndGetBookById(any(UUID.class));

        ResultActions resultActions = mockMvc.perform(patch("/api/books/" + UUID.randomUUID())
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(updateBookDto)))
                .andDo(print());

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    void given_existingBookId_when_deleteBook_then_returnStatusOkAndBookJson() throws Exception {
        Book book = getDefaultBook();

        given(principal.getName()).willReturn("ivan.franchin");
        given(bookService.validateAndGetBookById(book.getId())).willReturn(book);
        willDoNothing().given(bookService).deleteBook(any(Book.class));

        ResultActions resultActions = mockMvc.perform(delete("/api/books/" + book.getId())
                .principal(principal)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(book.getId().toString())))
                .andExpect(jsonPath("$.authorName", is(book.getAuthorName())))
                .andExpect(jsonPath("$.title", is(book.getTitle())))
                .andExpect(jsonPath("$.price", is(book.getPrice().doubleValue())));
    }

    @Test
    void given_nonExistingBookId_when_deleteBook_then_returnStatusNotFound() throws Exception {
        given(principal.getName()).willReturn("ivan.franchin");
        willThrow(BookNotFoundException.class).given(bookService).validateAndGetBookById(any(UUID.class));

        ResultActions resultActions = mockMvc.perform(delete("/api/books/" + UUID.randomUUID())
                .principal(principal)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print());

        resultActions.andExpect(status().isNotFound());
    }

}