package com.ivanfranchin.bookservice.service;

import com.ivanfranchin.bookservice.exception.BookNotFoundException;
import com.ivanfranchin.bookservice.model.Book;
import com.ivanfranchin.bookservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    @Override
    public List<Book> getBooksByAuthorName(String authorName) {
        return bookRepository.findByAuthorNameLike(authorName);
    }

    @Override
    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public void deleteBook(Book book) {
        bookRepository.delete(book);
    }

    @Override
    public Book validateAndGetBookById(String id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }
}
