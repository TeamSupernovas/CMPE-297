package edu.sjsu.stealdeal.ups.event;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ProductEventProducer {

	private final KafkaTemplate<String, ProductEvent> kafkaTemplate;
	
	@Value("${stealdeal.ups.kafka.producteventtopic}")
	private String topicName;

	@Autowired
	public ProductEventProducer(KafkaTemplate<String, ProductEvent> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendEvent(ProductEvent message) {
		CompletableFuture<SendResult<String, ProductEvent>> future = kafkaTemplate.send(topicName, message);
		log.info("Sending ProductEvent with productId={} to topic={}", message.getProductId(), topicName);
		future.whenComplete((result, ex) -> {
			if (ex == null) {
				log.info("SUCCESS: ProductEvent with productId={} successfully sent, Offset={}", message.getProductId(), result.getRecordMetadata().offset());
			} else {
				log.error("FAILURE: Unable to send ProductEvent with productId={}, Error={}",  message.getProductId(), ex.getMessage());
			}
		});
	}
}
