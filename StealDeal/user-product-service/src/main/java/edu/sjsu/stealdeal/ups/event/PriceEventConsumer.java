package edu.sjsu.stealdeal.ups.event;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import edu.sjsu.stealdeal.ups.model.PriceHistory;
import edu.sjsu.stealdeal.ups.model.Product;
import edu.sjsu.stealdeal.ups.repository.PriceHistoryRepository;
import edu.sjsu.stealdeal.ups.repository.ProductRepository;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class PriceEventConsumer {
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private PriceHistoryRepository priceHistoryRepository;
	
	@KafkaListener(topics = "${stealdeal.ups.kafka.priceeventtopic}", containerFactory = "priceEventKafkaListenerContainerFactory")
	public void processPriceEvents(PriceEvent priceEvent) throws ResourceNotFoundException {
		log.info("PriceEvents being consumed for : productId={}", priceEvent.getProductId());
		
		//get product from db
		Product product = productRepository.findById(priceEvent.getProductId()).orElse(null);
		if (product == null) {
			log.info("product cannot be retrieved for id {}", priceEvent.getProductId());
			return;
		}
		

		
		
		// update db
		Product updatedProduct = product.toBuilder()
			.color(priceEvent.getColor())
			.commentSummary(priceEvent.getCommentSummary())
			.currentPrice(priceEvent.getCurrentPrice())
			.name(priceEvent.getName())
			.description(priceEvent.getDescription())
			.previousPrice(priceEvent.getPreviousPrice())
			.productImageURL(priceEvent.getProductImageURL())
			.productURL(priceEvent.getProductURL())
			.size(priceEvent.getSize())
			.storeProductId(priceEvent.getStoreProductId())
			.build();
		
		updatedProduct = productRepository.save(updatedProduct);
		
		PriceHistory priceHistoryItem = PriceHistory.builder()
				.product(updatedProduct)
				.price(priceEvent.getCurrentPrice())
				.priceRecordTime(priceEvent.getPriceUpdatedTime())
				.build();
		
		priceHistoryRepository.save(priceHistoryItem);
		
	}
}
