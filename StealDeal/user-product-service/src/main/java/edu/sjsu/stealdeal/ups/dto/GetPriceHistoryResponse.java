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
public class GetPriceHistoryResponse {

    private long productId;
    private List<PriceDTO> priceHistory;
}
