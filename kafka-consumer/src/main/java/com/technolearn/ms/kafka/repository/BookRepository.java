package com.technolearn.ms.kafka.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.technolearn.ms.kafka.model.Book;

public interface BookRepository extends CrudRepository<Book, Integer>{
    
    List<Book> findByNameContainingIgnoreCaseOrAuthorContainingIgnoreCase(String query, String query2);

}
