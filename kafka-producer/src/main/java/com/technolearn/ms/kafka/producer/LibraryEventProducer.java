package com.technolearn.ms.kafka.producer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.technolearn.ms.kafka.dto.LibraryEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor // Inject beans using constructor injection
public class LibraryEventProducer {

    private final KafkaTemplate<Integer, String> kafkaTemplate;

    private final ObjectMapper objectmapper;

    @Value("${spring.kafka.topic}")
    private String kafkaTopic;

    /**
     * A method to publish the library event to the Kafka broker asynchronously
     * 
     * @param libEvent
     * @return
     * @throws JsonProcessingException
     */
    public CompletableFuture<SendResult<Integer, String>> sendLibraryEvent(LibraryEvent libEvent)
            throws JsonProcessingException {

        // Create ke-value as per the defined types in the Kafka config properties
        Integer key = libEvent.libraryEventId();
        String value = objectmapper.writeValueAsString(libEvent);

        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value, kafkaTopic);

        var completableFuture = kafkaTemplate.send(producerRecord);

        return completableFuture
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        handleFailure(key, value, throwable);
                    } else {
                        handleSuccess(key, value, result);
                    }
                });

    }

    /**
     * Handle successful message propagation
     * 
     * @param key
     * @param value
     * @param throwable
     */
    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Event sent successFully for the key : {} and the value is {} ,\nTopic is {}, partition is {}", key,
                value, result.getRecordMetadata().topic(), result.getRecordMetadata().partition());
    }

    /**
     * Handle message propagation failure
     * 
     * @param key
     * @param value
     * @param throwable
     */
    private void handleFailure(Integer key, String value, Throwable throwable) {
        log.error("Error sending event {} with the key : {} to the broker ! \nException occured : {}", value, key,
                throwable);
    }

    /**
     * Creating Producer record provides more finetuned control over setting event
     * headers.
     * Thus it is prefererd method where any additional metadata such as baggage
     * fields
     * need to be set
     * 
     * @param key
     * @param value
     * @param kafkaTopic
     * @return
     */
    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value, String kafkaTopic) {
        List<Header> recordHeaders = List.of(
                new RecordHeader("event-source", "service-call".getBytes()),
                new RecordHeader("server-timestamp", String.valueOf(System.currentTimeMillis()).getBytes()));

        return new ProducerRecord<>(kafkaTopic, null, key, value, recordHeaders);
    }

}
