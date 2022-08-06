package com.ivanfranchin.bookservice.dto;

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
class CreateBookRequestTest {

    @Autowired
    private JacksonTester<CreateBookRequest> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        CreateBookRequest createBookRequest = new CreateBookRequest("Ivan Franchin", "SpringBoot", BigDecimal.valueOf(29.99));

        JsonContent<CreateBookRequest> jsonContent = jacksonTester.write(createBookRequest);

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.authorName")
                .extractingJsonPathStringValue("@.authorName").isEqualTo(createBookRequest.getAuthorName());

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.title")
                .extractingJsonPathStringValue("@.title").isEqualTo(createBookRequest.getTitle());

        assertThat(jsonContent)
                .hasJsonPathNumberValue("@.price")
                .extractingJsonPathNumberValue("@.price").isEqualTo(createBookRequest.getPrice().doubleValue());
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"authorName\":\"Ivan Franchin\",\"title\":\"SpringBoot\",\"price\":29.99}";

        CreateBookRequest createBookRequest = jacksonTester.parseObject(content);

        assertThat(createBookRequest.getAuthorName()).isEqualTo("Ivan Franchin");
        assertThat(createBookRequest.getTitle()).isEqualTo("SpringBoot");
        assertThat(createBookRequest.getPrice().doubleValue()).isEqualTo(29.99);
    }
}