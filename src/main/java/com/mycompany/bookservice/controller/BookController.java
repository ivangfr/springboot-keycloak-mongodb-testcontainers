package com.mycompany.bookservice.controller;

import com.mycompany.bookservice.dto.BookDto;
import com.mycompany.bookservice.dto.CreateBookDto;
import com.mycompany.bookservice.dto.UpdateBookDto;
import com.mycompany.bookservice.exception.BookNotFoundException;
import com.mycompany.bookservice.model.Book;
import com.mycompany.bookservice.service.BookService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    private final ModelMapper modelMapper;

    public BookController(BookService bookService, ModelMapper modelMapper) {
        this.bookService = bookService;
        this.modelMapper = modelMapper;
    }

    @ApiOperation(
            value = "Get list of book. It can be filtered by author name",
            response = BookDto.class,
            responseContainer = "List"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<BookDto> getBooks(@RequestParam(required = false) String authorName) {
        boolean filterByAuthorName = !StringUtils.isEmpty(authorName);
        if (filterByAuthorName) {
            log.info("Get all books filtering by authorName equals to {}", authorName);
        } else {
            log.info("Get all books");
        }

        List<Book> books = filterByAuthorName ?
                bookService.getBooksByAuthorName(authorName) : bookService.getAllBooks();
        List<BookDto> bookDtos = books.stream()
                .map(book -> modelMapper.map(book, BookDto.class))
                .collect(Collectors.toList());

        return bookDtos;
    }

    @ApiOperation(
            value = "Get book by id",
            response = BookDto.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public BookDto getBookById(@PathVariable UUID id) throws BookNotFoundException {
        Book book = bookService.validateAndGetBookById(id);
        return modelMapper.map(book, BookDto.class);
    }

    @ApiOperation(
            value = "Create a book",
            response = BookDto.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 302, message = "Found Redirect"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody CreateBookDto createBookDto,
                                              @ApiIgnore Principal principal) {
        log.info("Post request made by {} to create a book {}", principal.getName(), createBookDto);

        Book book = modelMapper.map(createBookDto, Book.class);
        book.setId(UUID.randomUUID());
        book = bookService.saveBook(book);

        return new ResponseEntity<>(modelMapper.map(book, BookDto.class), HttpStatus.CREATED);
    }

    @ApiOperation(
            value = "Update a book",
            response = BookDto.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 302, message = "Found Redirect"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/{id}")
    public BookDto updateBook(@PathVariable UUID id,
                              @Valid @RequestBody UpdateBookDto updateBookDto,
                              @ApiIgnore Principal principal) throws BookNotFoundException {
        log.info("Patch request made by {} to update book with id {}. New values {}", principal.getName(), id, updateBookDto);

        Book book = bookService.validateAndGetBookById(id);
        modelMapper.map(updateBookDto, book);
        book = bookService.saveBook(book);

        return modelMapper.map(book, BookDto.class);
    }

    @ApiOperation(
            value = "Delete a book",
            response = BookDto.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok"),
            @ApiResponse(code = 302, message = "Found Redirect"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}")
    public BookDto deleteBook(@PathVariable UUID id,
                              @ApiIgnore Principal principal) throws BookNotFoundException {
        log.info("Delete request made by {} to remove book with id {}", principal.getName(), id);

        Book book = bookService.validateAndGetBookById(id);
        bookService.deleteBook(book);

        return modelMapper.map(book, BookDto.class);
    }

    @ExceptionHandler(BookNotFoundException.class)
    public void handleNotFoundException(Exception e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

}
