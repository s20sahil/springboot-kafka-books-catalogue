package com.technolearn.ms.kafka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookDto(
                @NotNull Integer id,
                @NotBlank String name,
                @NotBlank String author,
                Integer eventId) {
}