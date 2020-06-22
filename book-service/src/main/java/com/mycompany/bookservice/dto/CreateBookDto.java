package com.mycompany.bookservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookDto {

    @Schema(example = "Craig Walls")
    @NotBlank
    private String authorName;

    @Schema(example = "Spring Boot")
    @NotBlank
    private String title;

    @Schema(example = "10.5")
    @NotNull
    @Positive
    private BigDecimal price;

}
