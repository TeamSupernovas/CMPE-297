package edu.sjsu.stealdeal.wss.messaging;

import static org.mockito.Mockito.*;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.sjsu.stealdeal.wss.event.ProductEvent;
import edu.sjsu.stealdeal.wss.service.WebScrapingService;
import edu.sjsu.stealdeal.wss.util.EcommerceStoresContants;
import lombok.extern.log4j.Log4j2;

@ExtendWith(MockitoExtension.class)
@Log4j2
public class ProductEventConsumerTest {

    @Mock
    private WebScrapingService webScrapingService;

    @InjectMocks
    private ProductEventConsumer productEventConsumer;

    @Test
    public void testProcessProductEvents_GapStore() throws ResourceNotFoundException {
        // Arrange
        ProductEvent productEvent = new ProductEvent();
        productEvent.setProductId(Long.valueOf(123));
        productEvent.setStoreName("gap");

        // Act
        productEventConsumer.processProductEvents(productEvent);

        // Assert
        verify(webScrapingService, times(1))
                .scrapeProductFirstTime(EcommerceStoresContants.GAP, productEvent);
    }

    @Test
    public void testProcessProductEvents_BananaRepublicStore() throws ResourceNotFoundException {
        // Arrange
        ProductEvent productEvent = new ProductEvent();
        productEvent.setProductId(Long.valueOf(124));
        productEvent.setStoreName("bananarepublic");

        // Act
        productEventConsumer.processProductEvents(productEvent);

        // Assert
        verify(webScrapingService, times(1))
                .scrapeProductFirstTime(EcommerceStoresContants.BANANA_REPUBLIC, productEvent);
    }

    @Test
    public void testProcessProductEvents_AthletaStore() throws ResourceNotFoundException {
        // Arrange
        ProductEvent productEvent = new ProductEvent();
        productEvent.setProductId(Long.valueOf(125));
        productEvent.setStoreName("athleta");

        // Act
        productEventConsumer.processProductEvents(productEvent);

        // Assert
        verify(webScrapingService, times(1))
                .scrapeProductFirstTime(EcommerceStoresContants.ATHLETA, productEvent);
    }

    @Test
    public void testProcessProductEvents_OldNavyStore() throws ResourceNotFoundException {
        // Arrange
        ProductEvent productEvent = new ProductEvent();
        productEvent.setProductId(Long.valueOf(126));
        productEvent.setStoreName("oldnavy");

        // Act
        productEventConsumer.processProductEvents(productEvent);

        // Assert
        verify(webScrapingService, times(1))
                .scrapeProductFirstTime(EcommerceStoresContants.OLD_NAVY, productEvent);
    }
}
