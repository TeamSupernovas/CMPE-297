package edu.sjsu.stealdeal.ups.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceEvent {

	private long productId;
	private String name;
	private String description;
	private String productURL;
	private String storeProductId;
    private String storeName;
    private double currentPrice;
    private double previousPrice;
    private String size;
    private String color;
    private String commentSummary;
    private String productImageURL;
    private LocalDateTime priceUpdatedTime;
}
