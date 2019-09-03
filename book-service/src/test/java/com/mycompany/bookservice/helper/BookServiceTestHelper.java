package com.mycompany.bookservice.helper;

import com.mycompany.bookservice.dto.BookDto;
import com.mycompany.bookservice.dto.CreateBookDto;
import com.mycompany.bookservice.dto.UpdateBookDto;
import com.mycompany.bookservice.model.Book;

import java.math.BigDecimal;
import java.util.UUID;

public class BookServiceTestHelper {

    private BookServiceTestHelper() {
    }

    /* Default Book Values */

    private static final UUID ID = UUID.fromString("d8bcc132-c704-4d21-b05f-9557d7fc3d91");
    private static final String AUTHOR_NAME = "Ivan Franchin";
    private static final String TITLE = "Springboot";
    private static final BigDecimal PRICE = new BigDecimal("29.99");

    /* Book */

    public static Book getDefaultBook() {
        return getABook(ID, AUTHOR_NAME, TITLE, PRICE);
    }

    public static Book getABook(UUID id, String authorName, String title, BigDecimal price) {
        return new Book(id, authorName, title, price);
    }

    /* BookDto */

    public static BookDto getDefaultBookDto() {
        return getABookDto(ID, AUTHOR_NAME, TITLE, PRICE);
    }

    public static BookDto getABookDto(UUID id, String authorName, String title, BigDecimal price) {
        return new BookDto(id, authorName, title, price);
    }

    /* CreateBookDto */

    public static CreateBookDto getDefaultCreateBookDto() {
        return getACreateBookDto(AUTHOR_NAME, TITLE, PRICE);
    }

    public static CreateBookDto getACreateBookDto(String authorName, String title, BigDecimal price) {
        return new CreateBookDto(authorName, title, price);
    }

    /* UpdateBookDto */

    public static UpdateBookDto getDefaultUpdateBookDto() {
        return getAnUpdateBookDto(AUTHOR_NAME, TITLE, PRICE);
    }

    public static UpdateBookDto getAnUpdateBookDto(String authorName, String title, BigDecimal price) {
        return new UpdateBookDto(authorName, title, price);
    }

}
