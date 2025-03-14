package com.ivanfranchin.bookservice.service;

import com.ivanfranchin.bookservice.book.BookService;
import com.ivanfranchin.bookservice.book.exception.BookNotFoundException;
import com.ivanfranchin.bookservice.book.model.Book;
import com.ivanfranchin.bookservice.book.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@Import(BookService.class)
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @MockitoBean
    private BookRepository bookRepository;

    @Test
    void testSaveBook() {
        Book book = getDefaultBook();
        given(bookRepository.save(any(Book.class))).willReturn(book);

        Book bookSaved = bookService.saveBook(book);
        assertThat(bookSaved).isEqualTo(book);
    }

    @Test
    void testGetBooksWhenThereIsNone() {
        given(bookRepository.findAll()).willReturn(Collections.emptyList());

        List<Book> booksFound = bookService.getBooks();
        assertThat(booksFound).isEmpty();
    }

    @Test
    void testGetBooksWhenThereIsOne() {
        Book book = getDefaultBook();
        given(bookRepository.findAll()).willReturn(Collections.singletonList(book));

        List<Book> booksFound = bookService.getBooks();
        assertThat(booksFound).hasSize(1);
        assertThat(booksFound.getFirst()).isEqualTo(book);
    }

    @Test
    void testGetBooksByAuthorNameWhenAuthorHasOneBook() {
        Book book = getDefaultBook();
        given(bookRepository.findByAuthorNameLike(book.getAuthorName())).willReturn(Collections.singletonList(book));

        List<Book> booksFound = bookService.getBooksByAuthorName(book.getAuthorName());
        assertThat(booksFound).hasSize(1);
        assertThat(booksFound.getFirst()).isEqualTo(book);
    }

    @Test
    void testValidateAndGetBookWhenNonExistent() {
        given(bookRepository.findById(anyString())).willReturn(Optional.empty());

        Throwable exception = assertThrows(BookNotFoundException.class, () -> bookService.validateAndGetBookById("123"));
        assertThat("Book with id '123' not found.").isEqualTo(exception.getMessage());
    }

    @Test
    void testValidateAndGetBookWhenExistent() {
        Book book = getDefaultBook();
        given(bookRepository.findById(anyString())).willReturn(Optional.of(book));

        Book bookFound = bookService.validateAndGetBookById(book.getId());
        assertThat(bookFound).isEqualTo(book);
    }

    private Book getDefaultBook() {
        Book book = new Book("Ivan Franchin", "SpringBoot", BigDecimal.valueOf(29.99));
        book.setId("123");
        return book;
    }
}