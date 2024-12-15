package edu.sjsu.stealdeal.ups.event;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.sjsu.stealdeal.ups.model.Product;
import edu.sjsu.stealdeal.ups.repository.PriceHistoryRepository;
import edu.sjsu.stealdeal.ups.repository.ProductRepository;

class PriceEventConsumerTest {

    @InjectMocks
    private PriceEventConsumer priceEventConsumer;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessPriceEvents_ProductExists() {
        // Mock input PriceEvent
        PriceEvent priceEvent = PriceEvent.builder()
                .productId(1L)
                .currentPrice(100.0)
                .previousPrice(90.0)
                .color("Red")
                .commentSummary("Excellent")
                .description("Test Product Description")
                .name("Test Product")
                .productImageURL("http://example.com/image.jpg")
                .productURL("http://example.com/product")
                .size("Medium")
                .storeProductId("12345")
                .priceUpdatedTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        // Mock Product in the database
        Product existingProduct = Product.builder()
                .productId(1L)
                .color("Blue")
                .commentSummary("Good")
                .currentPrice(90.0)
                .description("Old Description")
                .name("Old Product")
                .productImageURL("http://example.com/old_image.jpg")
                .productURL("http://example.com/old_product")
                .size("Small")
                .storeProductId("67890")
                .build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));

        // Mock Product save
        Product updatedProduct = existingProduct.toBuilder()
                .color("Red")
                .commentSummary("Excellent")
                .currentPrice(100.0)
                .description("Test Product Description")
                .name("Test Product")
                .productImageURL("http://example.com/image.jpg")
                .productURL("http://example.com/product")
                .size("Medium")
                .storeProductId("12345")
                .previousPrice(90.0)
                .build();
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Call the method
        assertDoesNotThrow(() -> priceEventConsumer.processPriceEvents(priceEvent));

        // Verify ProductRepository save call
        verify(productRepository, times(1)).save(updatedProduct);

        // Verify PriceHistoryRepository save call
        verify(priceHistoryRepository, times(1)).save(
                argThat(priceHistory -> 
                        priceHistory.getPrice() == 100.0 &&
                        priceHistory.getPriceRecordTime().equals(LocalDateTime.of(2024, 1, 1, 10, 0)) &&
                        priceHistory.getProduct().equals(updatedProduct)
                )
        );
    }

    @Test
    void testProcessPriceEvents_ProductDoesNotExist() {
        // Mock input PriceEvent
        PriceEvent priceEvent = PriceEvent.builder()
                .productId(1L)
                .currentPrice(100.0)
                .build();

        // Mock ProductRepository to return empty
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Call the method
        assertDoesNotThrow(() -> priceEventConsumer.processPriceEvents(priceEvent));

        // Verify no interactions with PriceHistoryRepository
        verify(priceHistoryRepository, never()).save(any());
    }
}
