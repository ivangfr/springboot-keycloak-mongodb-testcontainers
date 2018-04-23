package com.mycompany.bookservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookDto {

    private String title;
    private String authorName;
    private BigDecimal price;

}
