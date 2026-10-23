package com.example.demo.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.annotation.EnableAsync;

import com.example.demo.event.bookingEvent;

@Configuration
@EnableKafka
@EnableAsync
public class KafkaConfig {

    @Bean
    ProducerFactory<String, bookingEvent> bookingEventProducerFactory(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5_000);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    KafkaTemplate<String, bookingEvent> kafkaTemplate(
            ProducerFactory<String, bookingEvent> bookingEventProducerFactory) {
        return new KafkaTemplate<>(bookingEventProducerFactory);
    }

    @Bean
    ConsumerFactory<String, bookingEvent> bookingEventConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id:tatkal-booking-group}") String groupId) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        JsonDeserializer<bookingEvent> valueDeserializer = new JsonDeserializer<>(bookingEvent.class);
        valueDeserializer.addTrustedPackages("com.example.demo.event");
        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, bookingEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, bookingEvent> bookingEventConsumerFactory,
            @Value("${app.kafka.listener-enabled:true}") boolean listenerEnabled) {
        ConcurrentKafkaListenerContainerFactory<String, bookingEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bookingEventConsumerFactory);
        factory.setAutoStartup(listenerEnabled);
        return factory;
    }

    @Bean
    NewTopic bookingEventsTopic(@Value("${app.kafka.booking-topic:booking-events}") String topicName) {
        return TopicBuilder.name(topicName).partitions(1).replicas(1).build();
    }
}
