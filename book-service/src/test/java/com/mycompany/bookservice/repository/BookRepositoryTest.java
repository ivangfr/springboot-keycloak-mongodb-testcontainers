package com.mycompany.bookservice.repository;

import com.mycompany.bookservice.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mycompany.bookservice.helper.BookServiceTestHelper.getDefaultBook;
import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class BookRepositoryTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void givenNoBookWhenFindAllThenReturnEmptyArray() {
        List<Book> books = bookRepository.findAll();

        assertThat(books).isEmpty();
    }

    @Test
    void givenOneBookWhenFindAllThenReturnArrayWithOneBook() {
        Book book = getDefaultBook();
        mongoTemplate.save(book);

        List<Book> books = bookRepository.findAll();

        assertThat(books).hasSize(1);
    }

    @Test
    void givenNonExistingBookIdWhenFindByIdThenReturnBook() {
        Optional<Book> bookFound = bookRepository.findById(UUID.randomUUID());

        assertThat(bookFound).isNotPresent();
    }

    @Test
    void givenExistingBookIdWhenFindByIdThenReturnBook() {
        Book book = getDefaultBook();
        mongoTemplate.save(book);

        Optional<Book> bookFound = bookRepository.findById(book.getId());

        assertThat(bookFound).isPresent();
        assertThat(bookFound.get()).usingRecursiveComparison().isEqualTo(book);
    }

    @Test
    void givenExistingBookAuthorNameWithOneBookWhenFindByAuthorNameLikeThenReturnListWithOneBook() {
        Book book = getDefaultBook();
        mongoTemplate.save(book);

        List<Book> books = bookRepository.findByAuthorNameLike("Franchin");

        assertThat(books).hasSize(1);
    }

    @Test
    void givenExistingBookIdWhenDeleteThenBookIsDeleted() {
        Book book = getDefaultBook();
        mongoTemplate.save(book);

        Optional<Book> bookFound = bookRepository.findById(book.getId());
        assertThat(bookFound).isPresent();

        bookRepository.delete(book);

        bookFound = bookRepository.findById(book.getId());
        assertThat(bookFound).isNotPresent();
    }

    @Test
    void givenExistingBookIdWhenUpdateThenBookIsUpdated() {
        Book book = getDefaultBook();
        mongoTemplate.save(book);

        Optional<Book> bookFound = bookRepository.findById(book.getId());
        assertThat(bookFound).isPresent();
        assertThat(bookFound.get()).usingRecursiveComparison().isEqualTo(book);

        book.setAuthorName("Ivan Franchin 2");
        book.setTitle("Java 8");
        book.setPrice(new BigDecimal("12.99"));

        bookRepository.save(book);

        bookFound = bookRepository.findById(book.getId());
        assertThat(bookFound).isPresent();
        assertThat(bookFound.get().getAuthorName()).isEqualTo(book.getAuthorName());
        assertThat(bookFound.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(bookFound.get().getPrice()).isEqualTo(book.getPrice());
    }

}