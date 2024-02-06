package com.technolearn.ms.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technolearn.ms.kafka.consumer.LibraryEventsConsumer;
import com.technolearn.ms.kafka.dto.BookDto;
import com.technolearn.ms.kafka.model.LibraryEvent;
import com.technolearn.ms.kafka.repository.LibraryEventRepository;
import com.technolearn.ms.kafka.service.LibraryEventsService;

@ComponentScan(basePackages = { "com.technolearn.ms.kafka" })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EmbeddedKafka(topics = { "library-events", "library-events.RETRY", "library-events.DLT" }, partitions = 3)
@TestPropertySource(properties = {
		"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"retryListener.startup=false"
})
public class KafkaConsumerIntegrationTests {

	@Autowired
    TestRestTemplate restTemplate;

	@Container
	static PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:latest");

	// TestContainer package will dynamically update the mongoDB URI upon container
	// start
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dymDynamicPropertyRegistry) {
		dymDynamicPropertyRegistry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
		dymDynamicPropertyRegistry.add("spring.datasource.username", postgresContainer::getUsername);
		dymDynamicPropertyRegistry.add("spring.datasource.password", postgresContainer::getPassword);
	}

	@Value("${topics.retry}")
	private String retryTopic;

	@Value("${topics.dlt}")
	private String deadLetterTopic;

	@Autowired
	EmbeddedKafkaBroker embeddedKafkaBroker;

	@Autowired
	KafkaTemplate<Integer, String> kafkaTemplate;

	@Autowired
	KafkaListenerEndpointRegistry endpointRegistry;

	@SpyBean
	LibraryEventsConsumer libraryEventsConsumerSpy;

	@SpyBean
	LibraryEventsService libraryEventsServiceSpy;

	@Autowired
	LibraryEventRepository libraryEventsRepository;

	/*
	 * @Autowired
	 * FailureRecordRepository failureRecordRepository;
	 */

	@Autowired
	ObjectMapper objectMapper;

	private Consumer<Integer, String> consumer;

	@BeforeEach
	void setUp() {
		
		for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
			System.out.println("Group Id : " + messageListenerContainer.getGroupId());
			if (Objects.equals(messageListenerContainer.getGroupId(), "library-events-listener-group")) {
				System.out.println("Waiting for assignment");
				ContainerTestUtils.waitForAssignment(messageListenerContainer,
						embeddedKafkaBroker.getPartitionsPerTopic());
			}
		}

	}

	@AfterEach
	void tearDown() {

		libraryEventsRepository.deleteAll();
		// failureRecordRepository.deleteAll();

	}

	@Test
	public void shouldPublishnewLibraryEvent() throws Exception {
		String jsonString = "{\"libraryEventId\": 3, \"book\": { \"id\": 1002, \"name\": \"Guns of Navarone\", \"author\": \"Allistair McLean\" } }";

		kafkaTemplate.sendDefault(jsonString).get();

		// when
		CountDownLatch latch = new CountDownLatch(1);
		latch.await(3, TimeUnit.SECONDS);

		// then
		verify(libraryEventsConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
		verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

		List<LibraryEvent> libraryEventList = (List<LibraryEvent>) libraryEventsRepository.findAll();
		assert libraryEventList.size() == 1;
		libraryEventList.forEach(libraryEvent -> {
			assert libraryEvent.getEventId() != null;
			assertEquals(1002, libraryEvent.getBook().getId());
		});
	}

	@Test
	public void shouldReturnSearchResultsCorrectly() throws Exception {
		String jsonString1 = "{\"libraryEventId\": 1, \"book\": { \"id\": 1000, \"name\": \"Guns of Navarone\", \"author\": \"Allistair McLean\" } }";
		String jsonString2 = "{\"libraryEventId\": 2, \"book\": { \"id\": 2000, \"name\": \"Animal Farm\", \"author\": \"George Orwell\" } }";
		kafkaTemplate.sendDefault(jsonString1).get();
		kafkaTemplate.sendDefault(jsonString2).get();

		// when
		CountDownLatch latch = new CountDownLatch(1);
		latch.await(3, TimeUnit.SECONDS);

		// then
		verify(libraryEventsConsumerSpy, times(2)).onMessage(isA(ConsumerRecord.class));
		verify(libraryEventsServiceSpy, times(2)).processLibraryEvent(isA(ConsumerRecord.class));

		List<LibraryEvent> libraryEventList = (List<LibraryEvent>) libraryEventsRepository.findAll();
		assert libraryEventList.size() == 2;
		libraryEventList.forEach(libraryEvent -> {
			assert libraryEvent.getEventId() != null;
		});
		assertEquals("Guns of Navarone", libraryEventList.stream().filter(le -> 1000 == le.getBook().getId())
				.findFirst().get().getBook().getName());
		assertEquals("Animal Farm", libraryEventList.stream().filter(le -> 2000 == le.getBook().getId()).findFirst()
				.get().getBook().getName());

		//Now that insertion is verified, test for searchEndpoint which will return valid results 
		ResponseEntity<List<BookDto>> responseEntity = restTemplate.exchange("/api/library/search?q=arm", HttpMethod.GET,
                null, new ParameterizedTypeReference<List<BookDto>>() {});

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(1, responseEntity.getBody().size());
		BookDto searchResult = responseEntity.getBody().stream().findFirst().get();
		assertEquals("George Orwell", searchResult.author());
		assertEquals("Animal Farm", searchResult.name());

		//Now that insertion is verified, test for searchEndpoint which will return NO results 
		responseEntity = restTemplate.exchange("/api/library/search?q=noSuchBookOrAuthor", HttpMethod.GET,
                null, new ParameterizedTypeReference<List<BookDto>>() {});

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(0, responseEntity.getBody().size());
	}

}
