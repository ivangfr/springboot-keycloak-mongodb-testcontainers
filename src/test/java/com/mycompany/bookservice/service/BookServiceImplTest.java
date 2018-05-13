package com.mycompany.bookservice.service;

import com.google.common.collect.Lists;
import com.mycompany.bookservice.exception.BookNotFoundException;
import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.repository.BookRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mycompany.bookservice.helper.BookServiceTestHelper.getDefaultBook;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
public class BookServiceImplTest {

    private BookService bookService;
    private BookRepository bookRepository;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        bookRepository = mock(BookRepository.class);
        bookService = new BookServiceImpl(bookRepository);
    }

    @Test
    public void given_oneBook_when_saveBook_then_returnBook() {
        Book book = getDefaultBook();
        given(bookRepository.save(book)).willReturn(book);

        Book bookSaved = bookService.saveBook(book);

        assertThat(bookSaved).isEqualToComparingFieldByField(book);
    }

    @Test
    public void given_noBook_when_getAllBooks_then_returnEmptyList() {
        given(bookRepository.findAll()).willReturn(new ArrayList<>());

        List<Book> booksFound = bookService.getAllBooks();

        assertThat(booksFound).hasSize(0);
    }

    @Test
    public void given_oneBook_when_getAllBooks_then_returnListWithOneBook() {
        Book book = getDefaultBook();
        given(bookRepository.findAll()).willReturn(Lists.newArrayList(book));

        List<Book> booksFound = bookService.getAllBooks();

        assertThat(booksFound).hasSize(1);
        assertThat(booksFound.get(0)).isEqualToComparingFieldByField(book);
    }

    @Test
    public void given_oneBook_when_getBookById_then_returnBook() {
        Book book = getDefaultBook();
        given(bookRepository.findById(book.getId())).willReturn(Optional.of(book));

        Optional<Book> bookFound = bookService.getBookById(book.getId());

        assertThat(bookFound.isPresent()).isTrue();
        assertThat(bookFound.get()).isEqualToComparingFieldByField(book);
    }

    @Test
    public void given_oneBook_when_getBookByIdUsingNonExistingId_then_returnNull() {
        Book book = getDefaultBook();
        given(bookRepository.findById(book.getId())).willReturn(Optional.of(book));

        Optional<Book> bookFound = bookService.getBookById(UUID.randomUUID());

        assertThat(bookFound.isPresent()).isFalse();
    }

    @Test
    public void given_oneBook_when_getBooksByAuthorName_then_returnListWithOneBook() {
        Book book = getDefaultBook();
        given(bookRepository.findByAuthorNameLike(book.getAuthorName())).willReturn(Lists.newArrayList(book));

        List<Book> booksFound = bookService.getBooksByAuthorName(book.getAuthorName());

        assertThat(booksFound).hasSize(1);
        assertThat(booksFound.get(0)).isEqualToComparingFieldByField(book);
    }

    @Test
    public void given_nonExistingBookId_when_validateAndGetBookById_then_throwException() throws BookNotFoundException {
        given(bookRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(null));

        UUID id = UUID.randomUUID();
        expectedException.expect(BookNotFoundException.class);
        expectedException.expectMessage(String.format("Book with id '%s' not found.", id));

        bookService.validateAndGetBookById(id);
    }

    @Test
    public void given_oneBook_when_validateAndGetBookById_then_returnBook() throws BookNotFoundException {
        Book book = getDefaultBook();
        given(bookRepository.findById(book.getId())).willReturn(Optional.of(book));

        Book bookFound = bookService.validateAndGetBookById(book.getId());

        assertThat(bookFound).isEqualToComparingFieldByField(book);
    }

}