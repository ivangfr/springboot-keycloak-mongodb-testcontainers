package com.mycompany.bookservice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookDto {

    @ApiModelProperty(example = "Craig Walls")
    @NotNull
    private String authorName;

    @ApiModelProperty(position = 2, example = "Spring Boot")
    @NotNull
    private String title;

    @ApiModelProperty(position = 3, example = "10.5")
    @NotNull
    private BigDecimal price;

}
