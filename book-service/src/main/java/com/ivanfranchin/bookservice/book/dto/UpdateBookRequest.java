package com.ivanfranchin.bookservice.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record UpdateBookRequest(
        @Schema(example = "Ivan G. Franchin") String authorName,
        @Schema(example = "Java 16") String title, @Schema(example = "20.5") BigDecimal price) {
}
