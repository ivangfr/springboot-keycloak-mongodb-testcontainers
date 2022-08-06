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
class UpdateBookRequestTest {

    @Autowired
    private JacksonTester<UpdateBookRequest> jacksonTester;

    @Test
    void testSerialize() throws IOException {
        UpdateBookRequest updateBookRequest = new UpdateBookRequest("Ivan Franchin", "SpringBoot", BigDecimal.valueOf(29.99));

        JsonContent<UpdateBookRequest> jsonContent = jacksonTester.write(updateBookRequest);

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.authorName")
                .extractingJsonPathStringValue("@.authorName").isEqualTo(updateBookRequest.getAuthorName());

        assertThat(jsonContent)
                .hasJsonPathStringValue("@.title")
                .extractingJsonPathStringValue("@.title").isEqualTo(updateBookRequest.getTitle());

        assertThat(jsonContent)
                .hasJsonPathNumberValue("@.price")
                .extractingJsonPathNumberValue("@.price").isEqualTo(updateBookRequest.getPrice().doubleValue());
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"authorName\":\"Ivan Franchin\",\"title\":\"SpringBoot\",\"price\":29.99}";

        UpdateBookRequest updateBookRequest = jacksonTester.parseObject(content);

        assertThat(updateBookRequest.getAuthorName()).isEqualTo("Ivan Franchin");
        assertThat(updateBookRequest.getTitle()).isEqualTo("SpringBoot");
        assertThat(updateBookRequest.getPrice().doubleValue()).isEqualTo(29.99);
    }
}