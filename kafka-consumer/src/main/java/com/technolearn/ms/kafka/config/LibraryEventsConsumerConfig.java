package com.technolearn.ms.kafka.config;

import java.util.List;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;

import com.technolearn.ms.kafka.service.KafkaConsumptionFailureService;
import com.technolearn.ms.kafka.service.LibraryEventsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class LibraryEventsConsumerConfig {

    // Defining resuable string literals
    public static final String RETRY = "RETRY";
    public static final String SUCCESS = "SUCCESS";
    public static final String DEAD = "DEAD";

    private final LibraryEventsService libraryEventsService;

    private final KafkaProperties kafkaProperties;
 
    private final KafkaTemplate kafkaTemplate;

    private final KafkaConsumptionFailureService failureService;

    @Value("${topics.retry:library-events.RETRY}")
    private String retryTopic;

    @Value("${topics.dlt:library-events.DLT}")
    private String deadLetterTopic;

    public DeadLetterPublishingRecoverer publishingRecoverer() {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (r, e) -> {
            log.error("Exception in publishingRecoverer : {} ", e.getMessage(), e);
            if (e.getCause() instanceof RecoverableDataAccessException) {
                return new TopicPartition(retryTopic, r.partition());
            } else {
                return new TopicPartition(deadLetterTopic, r.partition());
            }
        });

        return recoverer;

    }

    ConsumerRecordRecoverer consumerRecordRecoverer = (record, exception) -> {
        log.error("Exception is : {} Failed Record : {} ", exception, record);
        if (exception.getCause() instanceof RecoverableDataAccessException) {
            log.info("Inside the recoverable logic");
            // Add any Recovery Code here.
            // failureService.saveFailedRecord((ConsumerRecord<Integer, String>) record,
            // exception, RETRY);

        } else {
            log.info("Inside the non recoverable logic and skipping the record : {}", record);

        }
    };

    /**
     * Error handler for concurrent consumer factory.
     */
    public DefaultErrorHandler errorHandler() {

        var exceptiopnToIgnorelist = List.of(
                IllegalArgumentException.class);

        ExponentialBackOffWithMaxRetries expBackOff = new ExponentialBackOffWithMaxRetries(2);
        expBackOff.setInitialInterval(1_000L);
        expBackOff.setMultiplier(2.0);
        expBackOff.setMaxInterval(2_000L);

        var fixedBackOff = new FixedBackOff(1000L, 2L);

        /**
         * Just the Custom Error Handler
         */
        // var defaultErrorHandler = new DefaultErrorHandler(fixedBackOff);

        /**
         * Error Handler with the BackOff, Exceptions to Ignore, RetryListener
         */

        var defaultErrorHandler = new DefaultErrorHandler(
                // consumerRecordRecoverer
                publishingRecoverer(),
                fixedBackOff
        // expBackOff
        );

        exceptiopnToIgnorelist.forEach(defaultErrorHandler::addNotRetryableExceptions);

        defaultErrorHandler.setRetryListeners(
                (record, ex, deliveryAttempt) -> log.info(
                        "Failed Record in Retry Listener  exception : {} , deliveryAttempt : {} ", ex.getMessage(),
                        deliveryAttempt));

        return defaultErrorHandler;
    }

    /**
     * To configer concurrent listeners
     * @param configurer
     * @param kafkaConsumerFactory
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory
                .getIfAvailable(
                        () -> new DefaultKafkaConsumerFactory<>(this.kafkaProperties.buildConsumerProperties())));
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }

}
