package com.mycompany.bookservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class Book {

    @Id
    private UUID id;

    private String title;

    @Indexed
    private String authorName;

    private BigDecimal price;

}
