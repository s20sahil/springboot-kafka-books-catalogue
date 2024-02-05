package com.technolearn.ms.kafka.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.technolearn.ms.kafka.dto.LibraryEvent;
import com.technolearn.ms.kafka.service.LibraryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class LibraryEventsController {

    private final LibraryService libraryService;

   
    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> addNewBook(@Valid @RequestBody LibraryEvent libraryEvent) throws Exception{
        log.info("Publishing library event {} to the Kafka topic", libraryEvent);
        libraryService.sendLibraryEvent(libraryEvent);

        return ResponseEntity.ok("Book event created successfully!");
    }

}
