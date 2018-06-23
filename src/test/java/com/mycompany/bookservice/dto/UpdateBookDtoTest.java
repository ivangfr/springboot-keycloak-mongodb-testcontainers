package com.mycompany.bookservice.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.math.BigDecimal;

import static com.mycompany.bookservice.helper.BookServiceTestHelper.getAnUpdateBookDto;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@JsonTest
public class UpdateBookDtoTest {

    @Autowired
    private JacksonTester<UpdateBookDto> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        BigDecimal price = new BigDecimal("29.99");
        UpdateBookDto updateBookDto = getAnUpdateBookDto("Ivan Franchin", "Springboot", price);

        JsonContent<UpdateBookDto> jsonContent = jacksonTester.write(updateBookDto);

        assertThat(jsonContent).hasJsonPathStringValue("@.authorName");
        assertThat(jsonContent).extractingJsonPathStringValue("@.authorName").isEqualTo(updateBookDto.getAuthorName());
        assertThat(jsonContent).hasJsonPathStringValue("@.title");
        assertThat(jsonContent).extractingJsonPathStringValue("@.title").isEqualTo(updateBookDto.getTitle());
        assertThat(jsonContent).hasJsonPathNumberValue("@.price");
        assertThat(jsonContent).extractingJsonPathNumberValue("@.price").isEqualTo(updateBookDto.getPrice().doubleValue());
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"authorName\":\"Ivan Franchin\",\"title\":\"Springboot\",\"price\":29.99}";

        UpdateBookDto updateBookDto = jacksonTester.parseObject(content);

        assertThat(updateBookDto.getAuthorName()).isEqualTo("Ivan Franchin");
        assertThat(updateBookDto.getTitle()).isEqualTo("Springboot");
        assertThat(updateBookDto.getPrice().doubleValue()).isEqualTo(29.99);
    }

}