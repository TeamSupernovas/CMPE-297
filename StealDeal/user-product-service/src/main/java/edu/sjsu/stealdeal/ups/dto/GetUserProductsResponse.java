package edu.sjsu.stealdeal.ups.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetUserProductsResponse {

    private List<ProductDTO> productDTOs;
    private int currentPage;
    private long totalItems;
    private int totalPages;
    private int pageSize;
}
