package com.ivanfranchin.bookservice.book.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateBookRequest(
        @Schema(example = "Ivan Franchin") @NotBlank String authorName,
        @Schema(example = "SpringBoot") @NotBlank String title,
        @Schema(example = "10.5") @NotNull @Positive BigDecimal price) {
}
