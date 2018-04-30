package com.mycompany.bookservice.repository;

import com.mycompany.bookservice.model.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.mycompany.bookservice.helper.BookServiceTestHelper.getABook;
import static com.mycompany.bookservice.helper.BookServiceTestHelper.getDefaultBook;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataMongoTest
public class BookRepositoryTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private BookRepository bookRepository;

    @Test
    public void given_noBook_when_findAll_then_returnEmptyArray() {
        List<Book> books = bookRepository.findAll();

        assertThat(books).hasSize(0);
    }

    @Test
    public void given_oneBook_when_findAll_then_returnArrayWithOneBook() {
        Book book = getDefaultBook();
        mongoTemplate.save(book);

        List<Book> books = bookRepository.findAll();

        assertThat(books).hasSize(1);
    }

    @Test
    public void given_noBook_when_findOne_then_returnBook() {
        Book bookFound = bookRepository.findOne(UUID.randomUUID());

        assertThat(bookFound).isNull();
    }

    @Test
    public void given_oneBook_when_findOne_then_returnBook() {
        Book book = getDefaultBook();
        mongoTemplate.save(book);

        Book bookFound = bookRepository.findOne(book.getId());

        assertThat(bookFound).isEqualToComparingFieldByField(book);
    }

    @Test
    public void given_oneBook_when_findByAuthorNameLike_then_returnBook() {
        Book book = getDefaultBook();
        mongoTemplate.save(book);

        List<Book> books = bookRepository.findByAuthorNameLike("Franchin");

        assertThat(books).hasSize(1);
    }

    @Test
    public void given_oneBook_when_delete_then_bookIsDeleted() {
        Book book = getDefaultBook();
        mongoTemplate.save(book);

        Book bookFound = bookRepository.findOne(book.getId());
        assertThat(bookFound).isNotNull();

        bookRepository.delete(book.getId());

        bookFound = bookRepository.findOne(book.getId());
        assertThat(bookFound).isNull();
    }

    @Test
    public void given_oneBook_when_update_then_bookIsUpdated() {
        Book book = getDefaultBook();
        mongoTemplate.save(book);

        Book bookFound = bookRepository.findOne(book.getId());
        assertThat(bookFound).isEqualToComparingFieldByField(book);

        String newAuthorName = "Ivan Franchin Jr.";
        String newTitle = "Java 8";
        BigDecimal newPrice = new BigDecimal(12.99);

        book.setAuthorName(newAuthorName);
        book.setTitle(newTitle);
        book.setPrice(newPrice);

        bookRepository.save(book);

        bookFound = bookRepository.findOne(book.getId());
        assertThat(bookFound.getAuthorName()).isEqualTo(newAuthorName);
        assertThat(bookFound.getTitle()).isEqualTo(newTitle);
        assertThat(bookFound.getPrice()).isEqualTo(newPrice);
    }

}