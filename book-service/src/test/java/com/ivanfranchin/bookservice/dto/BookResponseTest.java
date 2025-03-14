package com.ivanfranchin.bookservice.dto;

import com.ivanfranchin.bookservice.book.dto.BookResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

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
                .extractingJsonPathStringValue("@.id").isEqualTo(bookResponse.id());

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.authorName")
                .extractingJsonPathStringValue("@.authorName").isEqualTo(bookResponse.authorName());

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.title")
                .extractingJsonPathStringValue("@.title").isEqualTo(bookResponse.title());

        assertThat(jsonContent)
                .hasJsonPathNumberValue("@.price")
                .extractingJsonPathNumberValue("@.price").isEqualTo(bookResponse.price().doubleValue());
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"id\":\"123\",\"authorName\":\"Ivan Franchin\",\"title\":\"SpringBoot\",\"price\":29.99}";

        BookResponse bookResponse = jacksonTester.parseObject(content);

        assertThat(bookResponse.id()).hasToString("123");
        assertThat(bookResponse.authorName()).isEqualTo("Ivan Franchin");
        assertThat(bookResponse.title()).isEqualTo("SpringBoot");
        assertThat(bookResponse.price().doubleValue()).isEqualTo(29.99);
    }
}