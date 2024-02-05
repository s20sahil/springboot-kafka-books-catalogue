package com.technolearn.ms.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technolearn.ms.kafka.dto.Book;
import com.technolearn.ms.kafka.dto.LibraryEvent;

import lombok.RequiredArgsConstructor;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = { "library-events" }, partitions = 3)
@TestPropertySource(properties = { "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}" }) // The properties to
                                                                                              // beinjected comes with
                                                                                              // the embedded Kafka
                                                                                              // broker
@RequiredArgsConstructor
class KafkaProducerIntegrationTests {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    ObjectMapper objectMapper;

    private Consumer<Integer, String> consumer;

    /**
     * Initialize kafkaConsumer before each test method execution
     */
    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(
                KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer())
                .createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void postLibraryEventShouldRetrunOkStatus() throws Exception {

        // Valid event body
        LibraryEvent libraryEvent = new LibraryEvent(null, new Book(1, "Animal Farm", "George Orwell"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

        // when
        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/books", HttpMethod.POST,
                request, String.class);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Book event created successfully!", responseEntity.getBody());

        ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer);
        assert consumerRecords.count() == 1;
        consumerRecords.forEach(record -> {
            var libraryEventActual = deserializeLibraryEvent(record.value());
            assertEquals(libraryEvent, libraryEventActual);

        });

    }

    @Test
    void postLibraryEventShouldThrowBadRequestException() throws Exception {

        // Missing libraryEvent.book.id for validation error
        LibraryEvent libraryEvent = new LibraryEvent(null, new Book(null, "Animal Farm", "George Orwell"));

        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

        // when
        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/books", HttpMethod.POST,
                request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotEquals("Book event created successfully!", responseEntity.getBody());
        ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer);
        // Expected value is one because the previous test's message is being replayed
        assertEquals(1, consumerRecords.count());

    }

    private LibraryEvent deserializeLibraryEvent(String valueString) {
        try {
            return objectMapper.readValue(valueString, LibraryEvent.class);
        } catch (Exception e) {
            Assert.fail();
            return null;
        }
    }

}
