package edu.sjsu.stealdeal.ups.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import edu.sjsu.stealdeal.ups.dto.*;
import edu.sjsu.stealdeal.ups.event.ProductEventProducer;
import edu.sjsu.stealdeal.ups.exception.ResourceNotFoundException;
import edu.sjsu.stealdeal.ups.model.*;
import edu.sjsu.stealdeal.ups.repository.*;

class UserProductServiceTest {

    @InjectMocks
    private UserProductService userProductService;

    @Mock
    private ProductEventProducer productEventProducer;

    @Mock
    private UserProductRepository userProductRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateProduct() {
        // Create the request object
        CreateUserProductRequest request = CreateUserProductRequest.builder()
                .userId(1L)
                .URL("http://test.com")
                .storeName("Test Store")
                .build();

        // Mock Product saving
        Product product = Product.builder()
                .storeName("Test Store")
                .productURL("http://test.com")
                .build();
        Product createdProduct = Product.builder()
                .productId(1L)
                .storeName("Test Store")
                .productURL("http://test.com")
                .build();
        when(productRepository.save(any(Product.class))).thenReturn(createdProduct);

        UserProduct createdUserProduct = UserProduct.builder()
                .userProductId(1L)
                .userId(1L)
                .productId(1L)
                .build();
        when(userProductRepository.save(any(UserProduct.class))).thenReturn(createdUserProduct);

        // Call the method
        CreateUserProductResponse response = userProductService.createProduct(request);

        // Verify the response
        assertNotNull(response);
        assertEquals(1L, response.getUserProductId());
        assertEquals("Product saved for price tracking", response.getMessage());

        // Verify the producer interaction
        verify(productEventProducer, times(1)).sendEvent(any());
    }

    @Test
    void testDeleteUserProduct() {
        // Mock UserProduct
        UserProduct userProduct = UserProduct.builder()
                .userProductId(1L)
                .build();
        when(userProductRepository.findById(1L)).thenReturn(Optional.of(userProduct));

        // Call the method
        assertDoesNotThrow(() -> userProductService.deleteUserProduct(1L));

        // Verify the deletion
        verify(userProductRepository, times(1)).delete(userProduct);
    }

    @Test
    void testDeleteUserProduct_NotFound() {
        // Mock empty repository response
        when(userProductRepository.findById(1L)).thenReturn(Optional.empty());

        // Verify exception is thrown
        assertThrows(ResourceNotFoundException.class, () -> userProductService.deleteUserProduct(1L));
    }

    @Test
    void testGetUserProductById() throws ResourceNotFoundException {
        // Mock UserProduct and Product
        UserProduct userProduct = UserProduct.builder()
                .userProductId(1L)
                .productId(1L)
                .build();
        Product product = Product.builder()
                .productId(1L)
                .name("Test Product")
                .currentPrice(100.0)
                .build();
        when(userProductRepository.findById(1L)).thenReturn(Optional.of(userProduct));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Call the method
        GetUserProductResponse response = userProductService.getUserProductById(1L);

        // Verify the response
        assertNotNull(response);
        assertEquals(1L, response.getProductDTO().getProductId());
        assertEquals("Test Product", response.getProductDTO().getName());
    }

    @Test
    void testGetUserProductsByUserId() {
        // Mock UserProduct and Product
        UserProduct userProduct = UserProduct.builder()
                .userProductId(1L)
                .productId(1L)
                .build();
        Product product = Product.builder()
                .productId(1L)
                .name("Test Product")
                .currentPrice(100.0)
                .build();
        Pageable pageable = PageRequest.of(0, 5);
        Page<UserProduct> userProductPage = new PageImpl<>(List.of(userProduct), pageable, 1);

        when(userProductRepository.findByUserId(1L, pageable)).thenReturn(userProductPage);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Call the method
        GetUserProductsResponse response = userProductService.getUserProductsByUserId(1L, pageable);

        // Verify the response
        assertNotNull(response);
        assertEquals(1, response.getProductDTOs().size());
        assertEquals("Test Product", response.getProductDTOs().get(0).getName());
    }

    @Test
    void testGetPriceHistory() throws ResourceNotFoundException {
        // Mock a UserProduct
        UserProduct userProduct = UserProduct.builder()
                .userProductId(1L)
                .productId(1L)
                .build();

        // Mock PriceHistory data
        List<PriceHistory> priceHistories = Arrays.asList(
            PriceHistory.builder()
                .priceHistoryId(1L)
                .price(100.0)
                .priceRecordTime(LocalDateTime.of(2024, 1, 1, 10, 0))
                .createdAt(LocalDateTime.of(2024, 1, 1, 9, 0))
                .build(),
            PriceHistory.builder()
                .priceHistoryId(2L)
                .price(90.0)
                .priceRecordTime(LocalDateTime.of(2024, 2, 1, 10, 0))
                .createdAt(LocalDateTime.of(2024, 2, 1, 9, 0))
                .build()
        );

        when(userProductRepository.findById(1L)).thenReturn(Optional.of(userProduct));
        when(priceHistoryRepository.findByProduct_productId(1L)).thenReturn(priceHistories);

        // Call the method
        GetPriceHistoryResponse response = userProductService.getPriceHistory(1L);

        // Verify the response
        assertNotNull(response);
        assertEquals(1L, response.getProductId());
        assertEquals(2, response.getPriceHistory().size());

        // Verify first PriceHistory record
        assertEquals(100.0, response.getPriceHistory().get(0).getPrice());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), response.getPriceHistory().get(0).getPriceRecordTime());

        // Verify second PriceHistory record
        assertEquals(90.0, response.getPriceHistory().get(1).getPrice());
        assertEquals(LocalDateTime.of(2024, 2, 1, 10, 0), response.getPriceHistory().get(1).getPriceRecordTime());
    }
}
