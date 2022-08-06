package com.ivanfranchin.bookservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookRequest {

    @Schema(example = "Ivan G. Franchin")
    private String authorName;

    @Schema(example = "Java 16")
    private String title;

    @Schema(example = "20.5")
    private BigDecimal price;
}
