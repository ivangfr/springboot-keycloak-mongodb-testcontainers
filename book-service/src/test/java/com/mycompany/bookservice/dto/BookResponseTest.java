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
class BookResponseTest {

    @Autowired
    private JacksonTester<BookResponse> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        BookResponse bookResponse = new BookResponse("123", "Ivan Franchin", "SpringBoot", BigDecimal.valueOf(29.99));

        JsonContent<BookResponse> jsonContent = jacksonTester.write(bookResponse);

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.id")
                .extractingJsonPathStringValue("@.id").isEqualTo(bookResponse.getId());

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.authorName")
                .extractingJsonPathStringValue("@.authorName").isEqualTo(bookResponse.getAuthorName());

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.title")
                .extractingJsonPathStringValue("@.title").isEqualTo(bookResponse.getTitle());

        assertThat(jsonContent)
                .hasJsonPathNumberValue("@.price")
                .extractingJsonPathNumberValue("@.price").isEqualTo(bookResponse.getPrice().doubleValue());
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"id\":\"123\",\"authorName\":\"Ivan Franchin\",\"title\":\"SpringBoot\",\"price\":29.99}";

        BookResponse bookResponse = jacksonTester.parseObject(content);

        assertThat(bookResponse.getId()).hasToString("123");
        assertThat(bookResponse.getAuthorName()).isEqualTo("Ivan Franchin");
        assertThat(bookResponse.getTitle()).isEqualTo("SpringBoot");
        assertThat(bookResponse.getPrice().doubleValue()).isEqualTo(29.99);
    }
}