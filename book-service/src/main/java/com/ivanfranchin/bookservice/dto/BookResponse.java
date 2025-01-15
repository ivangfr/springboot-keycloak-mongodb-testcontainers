package com.ivanfranchin.bookservice.dto;

import com.ivanfranchin.bookservice.model.Book;

import java.math.BigDecimal;

public record BookResponse(String id, String authorName, String title, BigDecimal price) {

    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getAuthorName(), book.getTitle(), book.getPrice());
    }
}
