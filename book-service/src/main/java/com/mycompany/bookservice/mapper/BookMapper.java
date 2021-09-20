package com.mycompany.bookservice.mapper;

import com.mycompany.bookservice.dto.BookResponse;
import com.mycompany.bookservice.dto.CreateBookRequest;
import com.mycompany.bookservice.dto.UpdateBookRequest;
import com.mycompany.bookservice.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BookMapper {

    Book toBook(CreateBookRequest createBookRequest);

    BookResponse toBookResponse(Book book);

    void updateUserFromRequest(UpdateBookRequest updateBookRequest, @MappingTarget Book book);
}
