package com.mycompany.bookservice.mapper;

import com.mycompany.bookservice.dto.BookDto;
import com.mycompany.bookservice.dto.CreateBookDto;
import com.mycompany.bookservice.dto.UpdateBookDto;
import com.mycompany.bookservice.model.Book;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2020-03-22T23:28:41+0000",
    comments = "version: 1.3.1.Final, compiler: javac, environment: Java 1.8.0_102 (Oracle Corporation)"
)
@Component
public class BookMapperImpl implements BookMapper {

    @Override
    public Book toBook(CreateBookDto createBookDto) {
        if ( createBookDto == null ) {
            return null;
        }

        Book book = new Book();

        book.setAuthorName( createBookDto.getAuthorName() );
        book.setTitle( createBookDto.getTitle() );
        book.setPrice( createBookDto.getPrice() );

        return book;
    }

    @Override
    public BookDto toBookDto(Book book) {
        if ( book == null ) {
            return null;
        }

        BookDto bookDto = new BookDto();

        bookDto.setId( book.getId() );
        bookDto.setAuthorName( book.getAuthorName() );
        bookDto.setTitle( book.getTitle() );
        bookDto.setPrice( book.getPrice() );

        return bookDto;
    }

    @Override
    public void updateUserFromDto(UpdateBookDto updateBookDto, Book book) {
        if ( updateBookDto == null ) {
            return;
        }

        if ( updateBookDto.getAuthorName() != null ) {
            book.setAuthorName( updateBookDto.getAuthorName() );
        }
        if ( updateBookDto.getTitle() != null ) {
            book.setTitle( updateBookDto.getTitle() );
        }
        if ( updateBookDto.getPrice() != null ) {
            book.setPrice( updateBookDto.getPrice() );
        }
    }
}
