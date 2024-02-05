package com.technolearn.ms.kafka.service;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.technolearn.ms.kafka.dto.LibraryEvent;
import com.technolearn.ms.kafka.producer.LibraryEventProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryEventProducer libraryEventProducer;

    
    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        
        libraryEventProducer.sendLibraryEvent(libraryEvent);        

    }


}
