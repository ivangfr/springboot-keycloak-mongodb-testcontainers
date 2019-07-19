package com.mycompany.bookservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {

    private UUID id;
    private String authorName;
    private String title;
    private BigDecimal price;

}
