package edu.sjsu.stealdeal.wss.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import edu.sjsu.stealdeal.wss.event.ScrapedProductEvent;

@Configuration
public class KafkaProducerConfig {
	
	@Value("${stealdeal.wss.kafka.server}")
    private String kafkaServer;
   
    @Value("${stealdeal.wss.kafka.username}")
    private String kafkaUsername;

    @Value("${stealdeal.wss.kafka.password}")
    private String kafkaPassword;

    @Bean
    public ProducerFactory<String, ScrapedProductEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put("security.protocol", "SASL_SSL");
        configProps.put("sasl.mechanism", "PLAIN");
        String jaasTemplate = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";";
        String jaasCfg = String.format(jaasTemplate, kafkaUsername, kafkaPassword);
        configProps.put("sasl.jaas.config", jaasCfg);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false); 

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ScrapedProductEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}