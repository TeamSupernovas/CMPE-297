package edu.sjsu.stealdeal.ups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetUserProductResponse {
	
	private ProductDTO productDTO;

}
