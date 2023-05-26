package com.ivanfranchin.bookservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequest {

    @Schema(example = "Ivan Franchin")
    @NotBlank
    private String authorName;

    @Schema(example = "SpringBoot")
    @NotBlank
    private String title;

    @Schema(example = "10.5")
    @NotNull
    @Positive
    private BigDecimal price;
}
