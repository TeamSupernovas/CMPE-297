package edu.sjsu.stealdeal.ups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserProductResponse {
	
	private long userProductId;
	private String message;

}
