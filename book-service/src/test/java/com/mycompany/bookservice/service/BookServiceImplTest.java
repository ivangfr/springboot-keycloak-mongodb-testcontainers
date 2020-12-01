package com.mycompany.bookservice.service;

import com.mycompany.bookservice.exception.BookNotFoundException;
import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
@Import(BookServiceImpl.class)
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    @Test
    void givenValidBookWhenSaveBookThenReturnBook() {
        Book book = getDefaultBook();
        given(bookRepository.save(book)).willReturn(book);

        Book bookSaved = bookService.saveBook(book);
        assertThat(bookSaved).usingRecursiveComparison().isEqualTo(book);
    }

    @Test
    void givenNoBookWhenGetAllBooksThenReturnEmptyList() {
        given(bookRepository.findAll()).willReturn(Collections.emptyList());

        List<Book> booksFound = bookService.getAllBooks();
        assertThat(booksFound).isEmpty();
    }

    @Test
    void givenOneBookWhenGetAllBooksThenReturnListWithOneBook() {
        Book book = getDefaultBook();
        given(bookRepository.findAll()).willReturn(Collections.singletonList(book));

        List<Book> booksFound = bookService.getAllBooks();
        assertThat(booksFound).hasSize(1);
        assertThat(booksFound.get(0)).usingRecursiveComparison().isEqualTo(book);
    }

    @Test
    void givenExistingBookAuthorNameWithOneBookWhenGetBooksByAuthorNameThenReturnListWithOneBook() {
        Book book = getDefaultBook();
        given(bookRepository.findByAuthorNameLike(book.getAuthorName())).willReturn(Collections.singletonList(book));

        List<Book> booksFound = bookService.getBooksByAuthorName(book.getAuthorName());
        assertThat(booksFound).hasSize(1);
        assertThat(booksFound.get(0)).usingRecursiveComparison().isEqualTo(book);
    }

    @Test
    void givenNonExistingBookIdWhenValidateAndGetBookByIdThenThrowBookNotFoundException() {
        given(bookRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        UUID id = UUID.randomUUID();
        Throwable exception = assertThrows(BookNotFoundException.class, () -> bookService.validateAndGetBookById(id));
        assertThat(String.format("Book with id '%s' not found.", id)).isEqualTo(exception.getMessage());
    }

    @Test
    void givenExistingBookIdWhenValidateAndGetBookByIdThenReturnBook() {
        Book book = getDefaultBook();
        given(bookRepository.findById(book.getId())).willReturn(Optional.of(book));

        Book bookFound = bookService.validateAndGetBookById(book.getId());
        assertThat(bookFound).usingRecursiveComparison().isEqualTo(book);
    }

}