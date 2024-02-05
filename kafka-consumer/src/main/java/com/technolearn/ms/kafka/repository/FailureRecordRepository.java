package com.technolearn.ms.kafka.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.technolearn.ms.kafka.model.FailureRecord;

public interface FailureRecordRepository extends CrudRepository<FailureRecord, Integer> {
    
    List<FailureRecord> findAllByStatus(String status);
    
}
