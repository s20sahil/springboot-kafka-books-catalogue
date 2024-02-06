package com.technolearn.ms.kafka.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity(name = "library_events")
public class LibraryEvent {
    
    @Id
    @GeneratedValue
    private Integer eventId;

    @OneToOne(mappedBy = "libraryEvent", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @ToString.Exclude
    private Book book;

    // Helper method to set the book and maintain bidirectional relationship
    public void setBook(Book book) {
        this.book = book;
        if (book != null) {
            book.setLibraryEvent(this);
        }
    }

}
