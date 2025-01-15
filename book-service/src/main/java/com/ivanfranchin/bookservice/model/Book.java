package com.ivanfranchin.bookservice.model;

import com.ivanfranchin.bookservice.dto.CreateBookRequest;
import com.ivanfranchin.bookservice.dto.UpdateBookRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "books")
public class Book {

    @Id
    private String id;
    private String authorName;
    private String title;
    private BigDecimal price;

    public Book(String authorName, String title, BigDecimal price) {
        this.authorName = authorName;
        this.title = title;
        this.price = price;
    }

    public static Book from(CreateBookRequest createBookRequest) {
        return new Book(
                createBookRequest.authorName(),
                createBookRequest.title(),
                createBookRequest.price()
        );
    }

    public static void updateFrom(UpdateBookRequest updateBookRequest, Book book) {
        if (updateBookRequest.authorName() != null) {
            book.setAuthorName(updateBookRequest.authorName());
        }
        if (updateBookRequest.title() != null) {
            book.setTitle(updateBookRequest.title());
        }
        if (updateBookRequest.price() != null) {
            book.setPrice(updateBookRequest.price());
        }
    }
}
