package com.ivanfranchin.bookservice.book;

import com.ivanfranchin.bookservice.book.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends MongoRepository<Book, String> {

    List<Book> findByAuthorNameLike(String authorName);
}
