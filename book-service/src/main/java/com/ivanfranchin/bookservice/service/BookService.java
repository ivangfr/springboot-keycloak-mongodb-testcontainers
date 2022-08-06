package com.ivanfranchin.bookservice.service;

import com.ivanfranchin.bookservice.model.Book;

import java.util.List;

public interface BookService {

    List<Book> getBooks();

    List<Book> getBooksByAuthorName(String authorName);

    Book saveBook(Book book);

    void deleteBook(Book book);

    Book validateAndGetBookById(String id);
}
