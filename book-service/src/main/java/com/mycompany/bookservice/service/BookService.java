package com.mycompany.bookservice.service;

import com.mycompany.bookservice.model.Book;

import java.util.List;
import java.util.UUID;

public interface BookService {

    List<Book> getAllBooks();

    List<Book> getBooksByAuthorName(String authorName);

    Book saveBook(Book book);

    void deleteBook(Book book);

    Book validateAndGetBookById(UUID id);

}
