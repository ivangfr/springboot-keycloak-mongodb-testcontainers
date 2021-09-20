package com.mycompany.bookservice.dto;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class BookResponse {

    String id;
    String authorName;
    String title;
    BigDecimal price;
}
