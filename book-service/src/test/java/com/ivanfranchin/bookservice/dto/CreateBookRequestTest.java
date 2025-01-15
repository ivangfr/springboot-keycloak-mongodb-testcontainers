package com.ivanfranchin.bookservice.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CreateBookRequestTest {

    @Autowired
    private JacksonTester<CreateBookRequest> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        CreateBookRequest createBookRequest = new CreateBookRequest("Ivan Franchin", "SpringBoot", BigDecimal.valueOf(29.99));

        JsonContent<CreateBookRequest> jsonContent = jacksonTester.write(createBookRequest);

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.authorName")
                .extractingJsonPathStringValue("@.authorName").isEqualTo(createBookRequest.authorName());

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.title")
                .extractingJsonPathStringValue("@.title").isEqualTo(createBookRequest.title());

        assertThat(jsonContent)
                .hasJsonPathNumberValue("@.price")
                .extractingJsonPathNumberValue("@.price").isEqualTo(createBookRequest.price().doubleValue());
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"authorName\":\"Ivan Franchin\",\"title\":\"SpringBoot\",\"price\":29.99}";

        CreateBookRequest createBookRequest = jacksonTester.parseObject(content);

        assertThat(createBookRequest.authorName()).isEqualTo("Ivan Franchin");
        assertThat(createBookRequest.title()).isEqualTo("SpringBoot");
        assertThat(createBookRequest.price().doubleValue()).isEqualTo(29.99);
    }
}