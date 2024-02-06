package com.technolearn.ms.kafka.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.technolearn.ms.kafka.model.LibraryEvent;
import com.technolearn.ms.kafka.repository.LibraryEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsService {

    private final ObjectMapper objectMapper;

    
    private final KafkaTemplate<Integer,String> kafkaTemplate;

    private final LibraryEventRepository libraryEventsRepository;

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonMappingException, JsonProcessingException {

        //Deserialize libraryEvent from the listener
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("libraryEvent : {} ", libraryEvent);

        libraryEvent = libraryEventsRepository.save(libraryEvent);

        log.info("libraryEvent saved information : {} ", libraryEvent);

    }
    
}
