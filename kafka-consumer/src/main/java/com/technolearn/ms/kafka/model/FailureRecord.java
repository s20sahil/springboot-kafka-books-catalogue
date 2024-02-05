package com.technolearn.ms.kafka.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity(name = "failure_records")
public class FailureRecord {

    @Id
    @GeneratedValue
    private Integer bookId;

    private String topic;
    private Integer keyValue;
    private String errorRecord;
    private Integer partition;
    private Long offsetValue;
    private String exception;
    private String status;

}