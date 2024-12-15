package edu.sjsu.stealdeal.ups.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {

    private long productId;
    private String name;
    private String description;
    private String size;
    private String color;
    private String productImageURL;
    private String commentSummary;
    private double currentPrice;
    private double previousPrice;
}
