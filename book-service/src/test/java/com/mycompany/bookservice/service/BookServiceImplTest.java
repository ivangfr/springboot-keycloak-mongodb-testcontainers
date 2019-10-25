package com.mycompany.bookservice.service;

import com.google.common.collect.Lists;
import com.mycompany.bookservice.exception.BookNotFoundException;
import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mycompany.bookservice.helper.BookServiceTestHelper.getDefaultBook;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class BookServiceImplTest {

    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookService = new BookServiceImpl(bookRepository);
    }

    @Test
    void givenValidBookWhenSaveBookThenReturnBook() {
        Book book = getDefaultBook();
        given(bookRepository.save(book)).willReturn(book);

        Book bookSaved = bookService.saveBook(book);

        assertThat(bookSaved).isEqualToComparingFieldByField(book);
    }

    @Test
    void givenNoBookWhenGetAllBooksThenReturnEmptyList() {
        given(bookRepository.findAll()).willReturn(Collections.emptyList());

        List<Book> booksFound = bookService.getAllBooks();

        assertThat(booksFound).hasSize(0);
    }

    @Test
    void givenOneBookWhenGetAllBooksThenReturnListWithOneBook() {
        Book book = getDefaultBook();
        given(bookRepository.findAll()).willReturn(Lists.newArrayList(book));

        List<Book> booksFound = bookService.getAllBooks();

        assertThat(booksFound).hasSize(1);
        assertThat(booksFound.get(0)).isEqualToComparingFieldByField(book);
    }

    @Test
    void givenExistingBookAuthorNameWithOneBookWhenGetBooksByAuthorNameThenReturnListWithOneBook() {
        Book book = getDefaultBook();
        given(bookRepository.findByAuthorNameLike(book.getAuthorName())).willReturn(Lists.newArrayList(book));

        List<Book> booksFound = bookService.getBooksByAuthorName(book.getAuthorName());

        assertThat(booksFound).hasSize(1);
        assertThat(booksFound.get(0)).isEqualToComparingFieldByField(book);
    }

    @Test
    void givenNonExistingBookIdWhenValidateAndGetBookByIdThenThrowBookNotFoundException() {
        given(bookRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        UUID id = UUID.randomUUID();
        Throwable exception = assertThrows(BookNotFoundException.class, () -> bookService.validateAndGetBookById(id));
        assertThat(String.format("Book with id '%s' not found.", id)).isEqualTo(exception.getMessage());
    }

    @Test
    void givenExistingBookIdWhenValidateAndGetBookByIdThenReturnBook() throws BookNotFoundException {
        Book book = getDefaultBook();
        given(bookRepository.findById(book.getId())).willReturn(Optional.of(book));

        Book bookFound = bookService.validateAndGetBookById(book.getId());

        assertThat(bookFound).isEqualToComparingFieldByField(book);
    }

}