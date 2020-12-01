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

import static com.mycompany.bookservice.helper.BookServiceTestHelper.getACreateBookDto;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@JsonTest
class CreateBookDtoTest {

    @Autowired
    private JacksonTester<CreateBookDto> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        BigDecimal price = new BigDecimal("29.99");
        CreateBookDto createBookDto = getACreateBookDto("Ivan Franchin", "Springboot", price);

        JsonContent<CreateBookDto> jsonContent = jacksonTester.write(createBookDto);

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.authorName")
                .extractingJsonPathStringValue("@.authorName").isEqualTo(createBookDto.getAuthorName());

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.title")
                .extractingJsonPathStringValue("@.title").isEqualTo(createBookDto.getTitle());

        assertThat(jsonContent)
                .hasJsonPathNumberValue("@.price")
                .extractingJsonPathNumberValue("@.price").isEqualTo(createBookDto.getPrice().doubleValue());
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"authorName\":\"Ivan Franchin\",\"title\":\"Springboot\",\"price\":29.99}";

        CreateBookDto createBookDto = jacksonTester.parseObject(content);

        assertThat(createBookDto.getAuthorName()).isEqualTo("Ivan Franchin");
        assertThat(createBookDto.getTitle()).isEqualTo("Springboot");
        assertThat(createBookDto.getPrice().doubleValue()).isEqualTo(29.99);
    }

}