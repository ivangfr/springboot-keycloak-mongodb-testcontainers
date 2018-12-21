package com.mycompany.bookservice.service;

import com.google.common.collect.Lists;
import com.mycompany.bookservice.exception.BookNotFoundException;
import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExpectedException;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mycompany.bookservice.helper.BookServiceTestHelper.getDefaultBook;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
public class BookServiceImplTest {

    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        bookService = new BookServiceImpl(bookRepository);
    }

    @Test
    void given_validBook_when_saveBook_then_returnBook() {
        Book book = getDefaultBook();
        given(bookRepository.save(book)).willReturn(book);

        Book bookSaved = bookService.saveBook(book);

        assertThat(bookSaved).isEqualToComparingFieldByField(book);
    }

    @Test
    void given_noBook_when_getAllBooks_then_returnEmptyList() {
        given(bookRepository.findAll()).willReturn(new ArrayList<>());

        List<Book> booksFound = bookService.getAllBooks();

        assertThat(booksFound).hasSize(0);
    }

    @Test
    void given_oneBook_when_getAllBooks_then_returnListWithOneBook() {
        Book book = getDefaultBook();
        given(bookRepository.findAll()).willReturn(Lists.newArrayList(book));

        List<Book> booksFound = bookService.getAllBooks();

        assertThat(booksFound).hasSize(1);
        assertThat(booksFound.get(0)).isEqualToComparingFieldByField(book);
    }

    @Test
    void given_existingBookId_when_getBookById_then_returnBook() {
        Book book = getDefaultBook();
        given(bookRepository.findById(book.getId())).willReturn(Optional.of(book));

        Optional<Book> bookFound = bookService.getBookById(book.getId());

        assertThat(bookFound.isPresent()).isTrue();
        assertThat(bookFound.get()).isEqualToComparingFieldByField(book);
    }

    @Test
    void given_nonExistingBookId_when_getBookById_then_returnNull() {
        Book book = getDefaultBook();
        given(bookRepository.findById(book.getId())).willReturn(Optional.of(book));

        Optional<Book> bookFound = bookService.getBookById(UUID.randomUUID());

        assertThat(bookFound.isPresent()).isFalse();
    }

    @Test
    void given_existingBookAuthorNameWithOneBook_when_getBooksByAuthorName_then_returnListWithOneBook() {
        Book book = getDefaultBook();
        given(bookRepository.findByAuthorNameLike(book.getAuthorName())).willReturn(Lists.newArrayList(book));

        List<Book> booksFound = bookService.getBooksByAuthorName(book.getAuthorName());

        assertThat(booksFound).hasSize(1);
        assertThat(booksFound.get(0)).isEqualToComparingFieldByField(book);
    }

    @Test
    void given_nonExistingBookId_when_validateAndGetBookById_then_throwBookNotFoundException() {
        given(bookRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        UUID id = UUID.randomUUID();
        Throwable exception = assertThrows(BookNotFoundException.class, () -> {
            bookService.validateAndGetBookById(id);
        });
        assertThat(String.format("Book with id '%s' not found.", id)).isEqualTo(exception.getMessage());
    }

    @Test
    void given_existingBookId_when_validateAndGetBookById_then_returnBook() throws BookNotFoundException {
        Book book = getDefaultBook();
        given(bookRepository.findById(book.getId())).willReturn(Optional.of(book));

        Book bookFound = bookService.validateAndGetBookById(book.getId());

        assertThat(bookFound).isEqualToComparingFieldByField(book);
    }

}