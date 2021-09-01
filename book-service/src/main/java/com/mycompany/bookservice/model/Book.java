package com.mycompany.bookservice.model;

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
}
