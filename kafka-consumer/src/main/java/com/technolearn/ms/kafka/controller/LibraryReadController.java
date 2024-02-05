package com.technolearn.ms.kafka.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technolearn.ms.kafka.dto.BookDto;
import com.technolearn.ms.kafka.service.LibraryReadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/library")
public class LibraryReadController {

    private final LibraryReadService libraryReadService;
    
    @GetMapping("/search")
    public ResponseEntity<List<BookDto>> searchBooks(@RequestParam String q) {
        return ResponseEntity.ok(libraryReadService.search(q));
    }

}
