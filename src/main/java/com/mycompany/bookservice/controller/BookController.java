package com.mycompany.bookservice.controller;

import com.mycompany.bookservice.dto.BookDto;
import com.mycompany.bookservice.dto.CreateBookDto;
import com.mycompany.bookservice.dto.UpdateBookDto;
import com.mycompany.bookservice.exception.BookNotFoundException;
import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.service.BookService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    private final ModelMapper modelMapper;

    public BookController(BookService bookService, ModelMapper modelMapper) {
        this.bookService = bookService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<BookDto>> getBooks(@RequestParam(required = false) String authorName) {
        boolean filterByAuthorName = !StringUtils.isEmpty(authorName);
        if (filterByAuthorName) {
            logger.info("Get all books filtering by authorName equals to {}", authorName);
        } else {
            logger.info("Get all books");
        }

        List<Book> books = filterByAuthorName ?
                bookService.getBooksByAuthorName(authorName) : bookService.getAllBooks();
        List<BookDto> bookDtos = books.stream()
                .map(book -> modelMapper.map(book, BookDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable UUID id) throws BookNotFoundException {
        Book book = bookService.validateAndGetBookById(id);
        return ResponseEntity.ok(modelMapper.map(book, BookDto.class));
    }

    @PostMapping
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody CreateBookDto createBookDto, Principal principal) {
        logger.info("Post request made by {} to create a book {}", principal.getName(), createBookDto);

        Book book = modelMapper.map(createBookDto, Book.class);
        book.setId(UUID.randomUUID());
        book = bookService.saveBook(book);

        return new ResponseEntity<>(modelMapper.map(book, BookDto.class), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(@PathVariable UUID id, @Valid @RequestBody UpdateBookDto updateBookDto, Principal principal) throws BookNotFoundException {
        logger.info("Patch request made by {} to update book with id {}. New values {}", principal.getName(), id, updateBookDto);

        Book book = bookService.validateAndGetBookById(id);
        modelMapper.map(updateBookDto, book);
        book = bookService.saveBook(book);

        return ResponseEntity.ok(modelMapper.map(book, BookDto.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BookDto> deleteBook(@PathVariable UUID id, Principal principal) throws BookNotFoundException {
        logger.info("Delete request made by {} to remove book with id {}", principal.getName(), id);

        Book book = bookService.validateAndGetBookById(id);
        bookService.deleteBook(book);

        return ResponseEntity.ok(modelMapper.map(book, BookDto.class));
    }

    @ExceptionHandler(BookNotFoundException.class)
    public void handleNotFoundException(Exception e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

}
