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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@JsonTest
class BookDtoTest {

    @Autowired
    private JacksonTester<BookDto> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        BookDto bookDto = new BookDto("123", "Ivan Franchin", "SpringBoot", BigDecimal.valueOf(29.99));

        JsonContent<BookDto> jsonContent = jacksonTester.write(bookDto);

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.id")
                .extractingJsonPathStringValue("@.id").isEqualTo(bookDto.getId());

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.authorName")
                .extractingJsonPathStringValue("@.authorName").isEqualTo(bookDto.getAuthorName());

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.title")
                .extractingJsonPathStringValue("@.title").isEqualTo(bookDto.getTitle());

        assertThat(jsonContent)
                .hasJsonPathNumberValue("@.price")
                .extractingJsonPathNumberValue("@.price").isEqualTo(bookDto.getPrice().doubleValue());
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"id\":\"123\",\"authorName\":\"Ivan Franchin\",\"title\":\"SpringBoot\",\"price\":29.99}";

        BookDto bookDto = jacksonTester.parseObject(content);

        assertThat(bookDto.getId()).hasToString("123");
        assertThat(bookDto.getAuthorName()).isEqualTo("Ivan Franchin");
        assertThat(bookDto.getTitle()).isEqualTo("SpringBoot");
        assertThat(bookDto.getPrice().doubleValue()).isEqualTo(29.99);
    }
}