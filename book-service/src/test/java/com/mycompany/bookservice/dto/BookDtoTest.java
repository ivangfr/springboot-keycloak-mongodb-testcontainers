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
import java.util.UUID;

import static com.mycompany.bookservice.helper.BookServiceTestHelper.getABookDto;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@JsonTest
public class BookDtoTest {

    @Autowired
    private JacksonTester<BookDto> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        UUID id = UUID.randomUUID();
        BigDecimal price = new BigDecimal("29.99");
        BookDto bookDto = getABookDto(id, "Ivan Franchin", "Springboot", price);

        JsonContent<BookDto> jsonContent = jacksonTester.write(bookDto);

        assertThat(jsonContent).hasJsonPathStringValue("@.id");
        assertThat(jsonContent).extractingJsonPathStringValue("@.id").isEqualTo(bookDto.getId().toString());
        assertThat(jsonContent).hasJsonPathStringValue("@.authorName");
        assertThat(jsonContent).extractingJsonPathStringValue("@.authorName").isEqualTo(bookDto.getAuthorName());
        assertThat(jsonContent).hasJsonPathStringValue("@.title");
        assertThat(jsonContent).extractingJsonPathStringValue("@.title").isEqualTo(bookDto.getTitle());
        assertThat(jsonContent).hasJsonPathNumberValue("@.price");
        assertThat(jsonContent).extractingJsonPathNumberValue("@.price").isEqualTo(bookDto.getPrice().doubleValue());
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"id\":\"5aa5fad4-03ed-43e0-9e5f-8cfaf1ef616c\",\"authorName\":\"Ivan Franchin\",\"title\":\"Springboot\",\"price\":29.99}";

        BookDto bookDto = jacksonTester.parseObject(content);

        assertThat(bookDto.getId().toString()).isEqualTo("5aa5fad4-03ed-43e0-9e5f-8cfaf1ef616c");
        assertThat(bookDto.getAuthorName()).isEqualTo("Ivan Franchin");
        assertThat(bookDto.getTitle()).isEqualTo("Springboot");
        assertThat(bookDto.getPrice().doubleValue()).isEqualTo(29.99);
    }

}