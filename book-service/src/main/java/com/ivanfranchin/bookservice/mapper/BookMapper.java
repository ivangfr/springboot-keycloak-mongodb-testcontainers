package com.ivanfranchin.bookservice.mapper;

import com.ivanfranchin.bookservice.dto.BookResponse;
import com.ivanfranchin.bookservice.model.Book;
import com.ivanfranchin.bookservice.dto.CreateBookRequest;
import com.ivanfranchin.bookservice.dto.UpdateBookRequest;
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
