package com.technolearn.ms.kafka.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity(name = "books")
public class Book {

    @Id
    private Integer id;
    private String name;
    private String author;

    @OneToOne
    @JoinColumn(name = "eventId")
    private LibraryEvent libraryEvent;
}
