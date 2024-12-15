package edu.sjsu.stealdeal.wss.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductEvent {
	
	private String productURL;
	private String storeName;
	private long productId;

}
