package com.ivanfranchin.bookservice.repository;

import com.ivanfranchin.bookservice.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class BookRepositoryTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    void testFindAllWhenThereIsNone() {
        List<Book> books = bookRepository.findAll();

        assertThat(books).isEmpty();
    }

    @Test
    void testFindAllWhenThereIsOne() {
        mongoTemplate.save(getDefaultBook());

        List<Book> books = bookRepository.findAll();

        assertThat(books).hasSize(1);
    }

    @Test
    void testFindByIdWhenNonExistent() {
        Optional<Book> bookFound = bookRepository.findById("123");

        assertThat(bookFound).isNotPresent();
    }

    @Test
    void testFindByIdWhenExistent() {
        Book book = mongoTemplate.save(getDefaultBook());

        Optional<Book> bookFound = bookRepository.findById(book.getId());

        assertThat(bookFound).isPresent();
        assertThat(bookFound.get()).isEqualTo(book);
    }

    @Test
    void testFindByAuthorNameLikeWhenThereIsOne() {
        mongoTemplate.save(getDefaultBook());

        List<Book> books = bookRepository.findByAuthorNameLike("Franchin");

        assertThat(books).hasSize(1);
    }

    @Test
    void testDeleteWhenExistent() {
        Book book = mongoTemplate.save(getDefaultBook());

        Optional<Book> bookOptional = bookRepository.findById(book.getId());
        assertThat(bookOptional).isPresent();

        bookRepository.delete(book);

        bookOptional = bookRepository.findById(book.getId());
        assertThat(bookOptional).isNotPresent();
    }

    @Test
    void testSaveWhenUpdatingExistentRecord() {
        Book book = mongoTemplate.save(getDefaultBook());

        Optional<Book> bookOptional = bookRepository.findById(book.getId());
        assertThat(bookOptional).isPresent();
        assertThat(bookOptional.get()).isEqualTo(book);

        book.setAuthorName("Ivan Franchin 2");
        book.setTitle("Java 8");
        book.setPrice(BigDecimal.valueOf(12.99));

        bookRepository.save(book);

        bookOptional = bookRepository.findById(book.getId());
        assertThat(bookOptional).isPresent();
        assertThat(bookOptional.get().getAuthorName()).isEqualTo(book.getAuthorName());
        assertThat(bookOptional.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(bookOptional.get().getPrice()).isEqualTo(book.getPrice());
    }

    private Book getDefaultBook() {
        return new Book("Ivan Franchin", "SpringBoot", BigDecimal.valueOf(29.99));
    }
}