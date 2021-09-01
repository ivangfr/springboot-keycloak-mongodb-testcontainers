package com.mycompany.bookservice.controller;

import com.mycompany.bookservice.dto.BookDto;
import com.mycompany.bookservice.dto.CreateBookDto;
import com.mycompany.bookservice.dto.UpdateBookDto;
import com.mycompany.bookservice.mapper.BookMapper;
import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import static com.mycompany.bookservice.config.SwaggerConfig.BEARER_KEY_SECURITY_SCHEME;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final BookMapper bookMapper;

    @Operation(summary = "Get list of book. It can be filtered by author name")
    @GetMapping
    public List<BookDto> getBooks(@RequestParam(required = false) String authorName) {
        boolean filterByAuthorName = StringUtils.hasText(authorName);
        if (filterByAuthorName) {
            log.info("Get books filtering by authorName equals to {}", authorName);
        } else {
            log.info("Get books");
        }
        List<Book> books = filterByAuthorName ? bookService.getBooksByAuthorName(authorName) : bookService.getBooks();
        return books.stream().map(bookMapper::toBookDto).collect(Collectors.toList());
    }

    @Operation(summary = "Get book by id")
    @GetMapping("/{id}")
    public BookDto getBookById(@PathVariable String id) {
        log.info("Get books with id equals to {}", id);
        Book book = bookService.validateAndGetBookById(id);
        return bookMapper.toBookDto(book);
    }

    @Operation(
            summary = "Create a book",
            security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public BookDto createBook(@Valid @RequestBody CreateBookDto createBookDto, Principal principal) {
        log.info("Post request made by {} to create a book {}", principal.getName(), createBookDto);
        Book book = bookMapper.toBook(createBookDto);
        book = bookService.saveBook(book);
        return bookMapper.toBookDto(book);
    }

    @Operation(
            summary = "Update a book",
            security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @PatchMapping("/{id}")
    public BookDto updateBook(@PathVariable String id, @Valid @RequestBody UpdateBookDto updateBookDto, Principal principal) {
        log.info("Patch request made by {} to update book with id {}. New values {}", principal.getName(), id, updateBookDto);
        Book book = bookService.validateAndGetBookById(id);
        bookMapper.updateUserFromDto(updateBookDto, book);
        book = bookService.saveBook(book);
        return bookMapper.toBookDto(book);
    }

    @Operation(
            summary = "Delete a book",
            security = {@SecurityRequirement(name = BEARER_KEY_SECURITY_SCHEME)})
    @DeleteMapping("/{id}")
    public BookDto deleteBook(@PathVariable String id, Principal principal) {
        log.info("Delete request made by {} to remove book with id {}", principal.getName(), id);
        Book book = bookService.validateAndGetBookById(id);
        bookService.deleteBook(book);
        return bookMapper.toBookDto(book);
    }

}
