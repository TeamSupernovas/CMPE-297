package edu.sjsu.stealdeal.wss.service;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.LoadingCache;

import edu.sjsu.stealdeal.wss.event.ProductEvent;
import edu.sjsu.stealdeal.wss.event.ScrapedProductEvent;
import edu.sjsu.stealdeal.wss.messaging.ScrapedProductProducer;
import edu.sjsu.stealdeal.wss.model.ScrapedProduct;
import edu.sjsu.stealdeal.wss.repository.ScrapedProductRepository;
import edu.sjsu.stealdeal.wss.util.EcommerceStoresContants;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WebScrapingServiceTest {

    @Mock
    private ScrapedProductRepository scrapedProductRepository;

    @Mock
    private ProductLoader productLoader;

    @Mock
    private ScrapedProductProducer scrapedProductProducer;

    @Mock
    private LoadingCache<String, HashMap<String, Date>> sitemapLastModifiedCache;

    private WebScrapingService webScrapingService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this); 
        // Create a SitemapDescription using the builder
        SitemapDescription sitemapDescription = SitemapDescription.builder()
            .url("http://example.com/sitemap")
            .productSectionPathInSitemap(List.of("//product"))
            .urlInSitemap("loc")
            .lastModifiedInSitemap("lastmod")
            .build();

        List<ECommerceStore> mockSupportedStores = List.of(
                new ECommerceStore("gap", sitemapDescription)
        );

        // Manually instantiate WebScrapingService
        webScrapingService = new WebScrapingService(mockSupportedStores);

        // Use reflection to inject mocked dependencies
        injectMocks(webScrapingService);
        
        
    }

    @Test
    public void testScrapeProductFirstTime() {
        // Arrange
        ProductEvent productEvent = ProductEvent.builder()
                .productId(123L)
                .productURL("http://example.com/product")
                .storeName("gap")
                .build();

        ECommerceStore store = EcommerceStoresContants.getECommerceStore("TestStore");
        ScrapedProduct savedScrapedProduct = ScrapedProduct.builder()
                .scrapedProductId(1L)
                .productId(123L)
                .storeName("gap")
                .productURL("http://example.com/product")
                .build();

        when(scrapedProductRepository.save(any(ScrapedProduct.class))).thenReturn(savedScrapedProduct);

        // Act
        webScrapingService.scrapeProductFirstTime(store, productEvent);

        // Assert
        verify(scrapedProductRepository, times(1)).save(any(ScrapedProduct.class));
        verify(productLoader, times(1)).load(eq(store), any(ScrapedProduct.class));
    }

    @Test
    public void testScheduleProductScraping() throws Exception {
        // Arrange
        ECommerceStore store = EcommerceStoresContants.getECommerceStore("gap");
        ScrapedProduct scrapedProduct = ScrapedProduct.builder()
                .scrapedProductId(1L)
                .storeName("gap")
                .build();

        when(scrapedProductRepository.findById(1L)).thenReturn(Optional.of(scrapedProduct));

        // Act
        webScrapingService.scheduleProductScraping(store, 1L, 5);

        // Allow some time for the task to execute
        TimeUnit.SECONDS.sleep(6);

        // Assert
        verify(scrapedProductRepository, atLeastOnce()).findById(1L);
    }

    @Test
    public void testScrapeProduct() {
        // Arrange
        ECommerceStore store = EcommerceStoresContants.getECommerceStore("gap");
        ScrapedProduct oldProduct = ScrapedProduct.builder()
                .scrapedProductId(1L)
                .productURL("http://example.com/product")
                .storeName("gap")
                .build();
        ScrapedProduct newProduct = ScrapedProduct.builder()
                .scrapedProductId(1L)
                .productURL("http://example.com/product")
                .storeName("gap")
                .currentPrice(50.0)
                .build();

        when(productLoader.load(store, oldProduct)).thenReturn(newProduct);
        when(scrapedProductRepository.save(any(ScrapedProduct.class))).thenReturn(newProduct);

        // Act
        webScrapingService.scrapeProduct(store, oldProduct);

        // Assert
        verify(productLoader, times(1)).load(store, oldProduct);
        verify(scrapedProductRepository, times(1)).save(newProduct);
        verify(scrapedProductProducer, times(1)).sendEvent(any(ScrapedProductEvent.class));
    }

    /**
     * Injects mocked dependencies into the WebScrapingService instance using reflection.
     */
    private void injectMocks(WebScrapingService webScrapingService) throws Exception {
        
        Field repositoryField = WebScrapingService.class.getDeclaredField("scrapedProductRepository");
        repositoryField.setAccessible(true);
        repositoryField.set(webScrapingService, scrapedProductRepository);

        Field producerField = WebScrapingService.class.getDeclaredField("scrapedProductProducer");
        producerField.setAccessible(true);
        producerField.set(webScrapingService, scrapedProductProducer);

        Field loaderField = WebScrapingService.class.getDeclaredField("productLoader");
        loaderField.setAccessible(true);
        loaderField.set(webScrapingService, productLoader);

        Field cacheField = WebScrapingService.class.getDeclaredField("sitemapLastModifiedCache");
        cacheField.setAccessible(true);
        cacheField.set(webScrapingService, sitemapLastModifiedCache);
    }
}
