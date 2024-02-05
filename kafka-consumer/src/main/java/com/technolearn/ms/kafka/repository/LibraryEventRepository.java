package com.technolearn.ms.kafka.repository;

import org.springframework.data.repository.CrudRepository;

import com.technolearn.ms.kafka.model.LibraryEvent;

public interface LibraryEventRepository extends CrudRepository<LibraryEvent, Integer>{
}
