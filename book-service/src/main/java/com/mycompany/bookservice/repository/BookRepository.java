package com.mycompany.bookservice.repository;

import com.mycompany.bookservice.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface BookRepository extends MongoRepository<Book, UUID> {

    List<Book> findByAuthorNameLike(String authorName);

}
