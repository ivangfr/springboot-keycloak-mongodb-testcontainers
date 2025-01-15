package com.ivanfranchin.bookservice.book;

import com.ivanfranchin.bookservice.book.exception.BookNotFoundException;
import com.ivanfranchin.bookservice.book.model.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    public List<Book> getBooksByAuthorName(String authorName) {
        return bookRepository.findByAuthorNameLike(authorName);
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }

    public void deleteBook(Book book) {
        bookRepository.delete(book);
    }

    public Book validateAndGetBookById(String id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }
}
