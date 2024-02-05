package com.technolearn.ms.kafka.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LibraryEvent(
                Integer libraryEventId,
                @NotNull @Valid Book book) {
}
