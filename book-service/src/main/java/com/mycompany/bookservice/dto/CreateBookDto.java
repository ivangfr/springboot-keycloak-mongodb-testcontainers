package com.mycompany.bookservice.dto;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(example = "Craig Walls")
    @NotBlank
    private String authorName;

    @ApiModelProperty(position = 2, example = "Spring Boot")
    @NotBlank
    private String title;

    @ApiModelProperty(position = 3, example = "10.5")
    @NotNull
    @Positive
    private BigDecimal price;

}
