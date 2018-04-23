package com.mycompany.bookservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookDto {

    @NotNull
    private String title;

    @NotNull
    private String authorName;

    @NotNull
    private BigDecimal price;

}
