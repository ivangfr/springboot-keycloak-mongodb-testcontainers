package com.mycompany.bookservice.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookDto {

    @ApiModelProperty(example = "James Gosling")
    private String authorName;

    @ApiModelProperty(example = "Java 8")
    private String title;

    @ApiModelProperty(example = "20.5")
    private BigDecimal price;

}
