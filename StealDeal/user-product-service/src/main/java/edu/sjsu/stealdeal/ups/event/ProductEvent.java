package edu.sjsu.stealdeal.ups.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductEvent {
	private long productId;
    private String productURL;
    private String storeName;
}
