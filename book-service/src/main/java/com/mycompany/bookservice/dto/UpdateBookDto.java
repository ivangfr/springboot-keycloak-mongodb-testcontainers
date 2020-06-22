package com.mycompany.bookservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookDto {

    @Schema(example = "James Gosling")
    private String authorName;

    @Schema(example = "Java 8")
    private String title;

    @Schema(example = "20.5")
    private BigDecimal price;

}
