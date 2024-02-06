package com.technolearn.ms.kafka.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.technolearn.ms.kafka.dto.BookDto;
import com.technolearn.ms.kafka.model.Book;
import com.technolearn.ms.kafka.repository.BookRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LibraryReadService {

    private final BookRepository bookRespository;

    /**
     * Delegating call to named query method
     * 
     * @param query
     * @return
     */
    public List<BookDto> search(String query) {
        return bookRespository.findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query).stream().map(
                book -> new BookDto(book.getId(), book.getName(), book.getAuthor(),
                        book.getLibraryEvent().getEventId()))
                .toList();
    }

}
