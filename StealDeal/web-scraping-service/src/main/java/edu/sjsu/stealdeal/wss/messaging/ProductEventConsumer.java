package edu.sjsu.stealdeal.wss.messaging;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import edu.sjsu.stealdeal.wss.event.ProductEvent;
import edu.sjsu.stealdeal.wss.service.WebScrapingService;
import edu.sjsu.stealdeal.wss.util.EcommerceStoresContants;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ProductEventConsumer {

	@Autowired
	WebScrapingService webScrapingService;

	@KafkaListener(topics = "${stealdeal.wss.kafka.topicname.productevents}", containerFactory = "productEventKafkaListenerContainerFactory")
	public void processProductEvents(ProductEvent productEvent) throws ResourceNotFoundException {
		log.info("ProductEvents being consumed : productId={}", productEvent.getProductId());
		switch (productEvent.getStoreName().toLowerCase()) {
		case "gap":
			webScrapingService.scrapeProductFirstTime(EcommerceStoresContants.GAP, productEvent);
			break;
		case "bananarepublic":
			webScrapingService.scrapeProductFirstTime(EcommerceStoresContants.BANANA_REPUBLIC, productEvent);
			break;
		case "athleta":
			webScrapingService.scrapeProductFirstTime(EcommerceStoresContants.ATHLETA, productEvent);
			break;
		case "oldnavy":
			webScrapingService.scrapeProductFirstTime(EcommerceStoresContants.OLD_NAVY, productEvent);
			break;
		default:
			log.error("Invalid store: {}", productEvent.getStoreName());
			break;
		}
	}
}
