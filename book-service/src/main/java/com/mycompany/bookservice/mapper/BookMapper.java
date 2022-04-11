package com.mycompany.bookservice.mapper;

import com.mycompany.bookservice.dto.BookResponse;
import com.mycompany.bookservice.dto.CreateBookRequest;
import com.mycompany.bookservice.dto.UpdateBookRequest;
import com.mycompany.bookservice.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BookMapper {

    @Mapping(target = "id", ignore = true)
    Book toBook(CreateBookRequest createBookRequest);

    BookResponse toBookResponse(Book book);

    @Mapping(target = "id", ignore = true)
    void updateUserFromRequest(UpdateBookRequest updateBookRequest, @MappingTarget Book book);
}
