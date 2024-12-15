package edu.sjsu.stealdeal.wss.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import edu.sjsu.stealdeal.wss.model.ScrapedProduct;

public class ProductLoaderTest {

    private ProductLoader productLoader;

    @BeforeEach
    public void setUp() {
        productLoader = new ProductLoader();
    }

    @Test
    public void testLoad_Success() throws Exception {
        // Arrange
        String url = "http://example.com/product?pid=123";
        String htmlContent = """
                <html>
                  <head>
                    <link rel="canonical" href="http://example.com/product" />
                    <meta name="description" content="A great product" />
                    <link rel="preload" as="image" href="/images/product.jpg" />
                  </head>
                  <body>
                    <div class="pdp-pricing">Now $25.00</div>
                    <input name="buy-box-Size" type="radio" aria-checked="true" value="M" />
                    <span class="swatch-label__value">Red</span>
                  </body>
                </html>
                """;

        Document mockDocument = Parser.parse(htmlContent, url);

        try (MockedStatic<HttpFileLoader> mockedStatic = mockStatic(HttpFileLoader.class)) {
            mockedStatic.when(() -> HttpFileLoader.loadFileToJsoupDocument(url)).thenReturn(mockDocument);

            ScrapedProduct previouslyScrapedProduct = ScrapedProduct.builder()
                    .productURL(url)
                    .currentPrice(20.0) // Previous price
                    .build();

            ECommerceStore store = new ECommerceStore("TestStore", null);

            // Act
            ScrapedProduct result = productLoader.load(store, previouslyScrapedProduct);

            // Assert
            assertNotNull(result);
            assertEquals(25.00, result.getCurrentPrice());
            assertEquals("A great product", result.getDescription());
            assertEquals("http://example.com/images/product.jpg", result.getProductImageURL());
            assertEquals("M", result.getSize());
            assertEquals("Red", result.getColor());
            assertEquals("http://example.com/product", result.getUrlInSitemap());
            assertEquals("123", result.getStoreProductId());
        }
    }

    @Test
    public void testLoad_InvalidURL() {
        // Arrange
        String invalidUrl = "invalid-url";
        ScrapedProduct previouslyScrapedProduct = ScrapedProduct.builder()
                .productURL(invalidUrl)
                .build();

        ECommerceStore store = new ECommerceStore("TestStore", null);

        // Act
        ScrapedProduct result = productLoader.load(store, previouslyScrapedProduct);

        // Assert
        assertNull(result);
    }

    @Test
    public void testLoad_MissingPrice() throws Exception {
        // Arrange
        String url = "http://example.com/product?pid=123";
        String htmlContent = """
        <html>
            <head>
                <link rel="canonical" href="http://example.com/product" />
                <meta name="description" content="A great product" />
                <link rel="preload" as="image" href="/images/product.jpg" />
            </head>
            <body>
                <div class="some-other-class">No price here</div>
            </body>
        </html>
        """;
    
        Document mockDocument = Parser.parse(htmlContent, url);
    
        try (MockedStatic<HttpFileLoader> mockedStatic = mockStatic(HttpFileLoader.class)) {
            mockedStatic.when(() -> HttpFileLoader.loadFileToJsoupDocument(url)).thenReturn(mockDocument);
    
            ScrapedProduct previouslyScrapedProduct = ScrapedProduct.builder()
                    .productURL(url)
                    .build();
    
            ECommerceStore store = new ECommerceStore("TestStore", null);
    
            // Act
            ScrapedProduct result = productLoader.load(store, previouslyScrapedProduct);
    
            // Assert
            assertNotNull(result); // Ensure the product object is created
            assertEquals(0.0,result.getCurrentPrice()); // Price should be null due to missing price
            assertEquals("http://example.com/product", result.getUrlInSitemap()); // Canonical URL should be set
            assertEquals("A great product", result.getDescription()); // Description should be set
            assertEquals("http://example.com/images/product.jpg", result.getProductImageURL()); // Image URL should be set
        }
    }
}
