package com.mycompany.bookservice.dto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;

import static com.mycompany.bookservice.helper.BookServiceTestHelper.getACreateBookDto;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@JsonTest
public class CreateBookDtoTest {

    @Autowired
    private JacksonTester<CreateBookDto> jacksonTester;

    @Test
    public void testSerialize() throws IOException {
        BigDecimal price = new BigDecimal("29.99");
        CreateBookDto createBookDto = getACreateBookDto("Ivan Franchin", "Springboot", price);

        JsonContent<CreateBookDto> jsonContent = jacksonTester.write(createBookDto);

        assertThat(jsonContent).hasJsonPathStringValue("@.authorName");
        assertThat(jsonContent).extractingJsonPathStringValue("@.authorName").isEqualTo(createBookDto.getAuthorName());
        assertThat(jsonContent).hasJsonPathStringValue("@.title");
        assertThat(jsonContent).extractingJsonPathStringValue("@.title").isEqualTo(createBookDto.getTitle());
        assertThat(jsonContent).hasJsonPathNumberValue("@.price");
        assertThat(jsonContent).extractingJsonPathNumberValue("@.price").isEqualTo(createBookDto.getPrice().doubleValue());
    }

    @Test
    public void testDeserialize() throws IOException {
        String content = "{\"authorName\":\"Ivan Franchin\",\"title\":\"Springboot\",\"price\":29.99}";

        CreateBookDto createBookDto = jacksonTester.parseObject(content);

        assertThat(createBookDto.getAuthorName()).isEqualTo("Ivan Franchin");
        assertThat(createBookDto.getTitle()).isEqualTo("Springboot");
        assertThat(createBookDto.getPrice().doubleValue()).isEqualTo(29.99);
    }

}