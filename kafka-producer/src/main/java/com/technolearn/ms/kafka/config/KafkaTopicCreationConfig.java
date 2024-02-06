package com.technolearn.ms.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("local")
public class KafkaTopicCreationConfig {

    @Value("${spring.kafka.topic}")
    private String kafkaTopic;

    /**
     * The TopicBuilder class makes use of the spring.kafka.admin.* 
     * properties from application.properties file. Which is why the property 
     * spring.kafka.admin.properties.bootstrap.servers seem to have redendant value
     * the 
     * @return
     */
    @Bean
    public NewTopic libraryEventsNewTopic() {
        return TopicBuilder.name(kafkaTopic)
        .partitions(3)
        .replicas(3)
        .build();
    }
    
}
