package edu.sjsu.stealdeal.wss.messaging;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import edu.sjsu.stealdeal.wss.event.ScrapedProductEvent;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ScrapedProductProducer {

	private final KafkaTemplate<String, ScrapedProductEvent> kafkaTemplate;
	
	@Value("${stealdeal.wss.kafka.topicname.priceevents}")
	private String topicName;

	@Autowired
	public ScrapedProductProducer(KafkaTemplate<String, ScrapedProductEvent> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendEvent(ScrapedProductEvent message) {
		CompletableFuture<SendResult<String, ScrapedProductEvent>> future = kafkaTemplate.send(topicName, message);
		log.info("Sending ScrapedProductEvent with ProductId={} to topic={}", message.getProductId(), topicName);
		future.whenComplete((result, ex) -> {
			if (ex == null) {
				log.info("SUCCESS: ScrapedProductEvent with ProductId={} sent to topic={} Offset={}", message.getProductId(),topicName,result.getRecordMetadata().offset());
			} else {
				log.error("FAILURE: Unable to send ScrapedProductEvent with ProductId={}, Error={}", message.getProductId(), ex.getMessage());
			}
		});
	}
}
