package edu.sjsu.stealdeal.wss.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.sjsu.stealdeal.wss.event.ProductEvent;
import edu.sjsu.stealdeal.wss.event.ScrapedProductEvent;
import edu.sjsu.stealdeal.wss.messaging.ScrapedProductProducer;
import edu.sjsu.stealdeal.wss.model.ScrapedProduct;
import edu.sjsu.stealdeal.wss.repository.ScrapedProductRepository;
import edu.sjsu.stealdeal.wss.util.EcommerceStoresContants;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class WebScrapingService {

	private static final int RELOAD_INTERVAL_SECONDS = 1800;
	private static final int SITEMAP_CACHE_SECONDS = 500;
	private ScheduledExecutorService executorService;
	private Map<String, ECommerceStore> supportedStoresByName;
	private LoadingCache<String, HashMap<String, Date>> sitemapLastModifiedCache;
	
	@Autowired
	private ProductLoader productLoader;
	
	@Autowired
	private ScrapedProductRepository scrapedProductRepository;
	
	@Autowired
	private ScrapedProductProducer scrapedProductProducer;
	
	
	
	public WebScrapingService(List<ECommerceStore> supportedStores) {
		executorService = Executors
				  .newScheduledThreadPool(2);
		this.supportedStoresByName = supportedStores.stream()
				.collect(Collectors.toMap(
						ECommerceStore::getName,
						Function.identity()
						));
		
		
		this.sitemapLastModifiedCache = CacheBuilder.newBuilder()
	        .expireAfterWrite(SITEMAP_CACHE_SECONDS, TimeUnit.SECONDS)
	        .build(new CacheLoader<String, HashMap<String, Date>>() {

		          @Override
		          public HashMap<String, Date> load(final String key) throws IOException, ParseException {
		        	  
		        	  ECommerceStore store = supportedStoresByName.get(key); 
		        	  SitemapDescription sitemapDescription = store.getSitemapDescription();
		        	   
		        	  
		            Document sitemapDocument = HttpFileLoader.loadFileToJsoupDocument(sitemapDescription.getUrl());
		            
		            //Document sitemapDocument = Jsoup.connect(sitemapDescription.url).get(); <- didn't work
		            
		            if (sitemapDocument == null) {
		              throw new IOException("Could not load sitemap");
		            }
		            List<Element> elements = List.of(sitemapDocument);
		            		            
		            for (String path: sitemapDescription.productSectionPathInSitemap) {
		              List<Element> children = new ArrayList<>();
		              for (Element element: elements) {
		                children.addAll(element.select(path));
		              }
		              elements = children;
		            }
		            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		            HashMap<String, Date> urlToLastModified = new HashMap<>();
		            for (Element element: elements) {
		              String url = element.select(sitemapDescription.urlInSitemap).text();
		              String lastModified = element.select(sitemapDescription.lastModifiedInSitemap).text();
		              //LOGGER.info("Found url " + url + " with last modified " + lastModified);
		              try {
		                urlToLastModified.put(url, dateFormat.parse(lastModified));
		              } catch (Exception e) {
		                e.printStackTrace();
		              }
		            }
		            return urlToLastModified;
		          }

		        });
        
        Random random = new Random();
        if (scrapedProductRepository != null) {
            scrapedProductRepository.findAll()
                    .stream()
                    .forEach(
                        scrapedProduct -> scheduleProductScraping(
                            EcommerceStoresContants.getECommerceStore(scrapedProduct.getStoreName()), 
                            scrapedProduct.getScrapedProductId(), random.nextInt(30 * 60))
                    );
        }
            
	}

    public ScrapedProductRepository getScrapedProductRepository() {
        return scrapedProductRepository;
    }
	
	
	public void scrapeProductFirstTime(ECommerceStore store, ProductEvent productEvent) {
		ScrapedProduct scrapedProduct = ScrapedProduct.builder()
				.productId(productEvent.getProductId())
				.productURL(productEvent.getProductURL())
				.storeName(productEvent.getStoreName())
				.build();
		ScrapedProduct createdScrapedProduct =  scrapedProductRepository.save(scrapedProduct);
		scrapeProduct(store, createdScrapedProduct);
        scheduleProductScraping(store, createdScrapedProduct.getScrapedProductId(), RELOAD_INTERVAL_SECONDS);
	}
	
	public void scheduleProductScraping(ECommerceStore store, long scrapedProductId, long delaySeconds) {
		executorService.schedule(new Runnable() {
			
			@Override
			public void run() {
				ScrapedProduct previouslyScrapedProduct = scrapedProductRepository.findById(scrapedProductId)
						.orElse(null);
				
				if (previouslyScrapedProduct == null) {
					// not in db .. never scrap
					log.info("Scraped Product Id not present {}", scrapedProductId );
					return;
				}
				LocalDateTime modifiedAt = previouslyScrapedProduct.getUpdatedAt();
				Date lastModifiedFromSitemap = getLastModified(store, previouslyScrapedProduct.getUrlInSitemap());
				if (lastModifiedFromSitemap != null && lastModifiedFromSitemap.toInstant().isBefore(modifiedAt.toInstant(ZoneOffset.UTC))) {
					// schedule for later
					scheduleProductScraping(store, previouslyScrapedProduct.getScrapedProductId(), RELOAD_INTERVAL_SECONDS);
					return;
				}
				scrapeProduct(store, previouslyScrapedProduct);
			}
			
		} , delaySeconds, TimeUnit.SECONDS);
		
	}
		
	public void scrapeProduct(ECommerceStore store, ScrapedProduct previouslyScrapedProduct) {
		
		
		ScrapedProduct newlyScrapedProduct = productLoader.load(store, previouslyScrapedProduct);
		if (newlyScrapedProduct == null) {
			log.info("newlyScrapedProduct is null for {}", previouslyScrapedProduct.getProductURL());
		} else {
			//add updated product in table
			newlyScrapedProduct = scrapedProductRepository.save(newlyScrapedProduct);
			sendScrapedProductEvent(newlyScrapedProduct);
		}
    }
	
	private void sendScrapedProductEvent(ScrapedProduct newlyScrapedProduct) {
		ScrapedProductEvent event = ScrapedProductEvent.builder()
				.color(newlyScrapedProduct.getColor())
				.commentSummary(newlyScrapedProduct.getCommentSummary())
				.currentPrice(newlyScrapedProduct.getCurrentPrice())
				.description(newlyScrapedProduct.getDescription())
				.name(newlyScrapedProduct.getName())
				.previousPrice(newlyScrapedProduct.getPreviousPrice())
				.priceUpdatedTime(newlyScrapedProduct.getUpdatedAt())
				.productId(newlyScrapedProduct.getProductId())
				.productImageURL(newlyScrapedProduct.getProductImageURL())
				.productURL(newlyScrapedProduct.getProductURL())
				.size(newlyScrapedProduct.getSize())
				.storeName(newlyScrapedProduct.getStoreName())
				.storeProductId(newlyScrapedProduct.getStoreProductId())
				.build();
		
		scrapedProductProducer.sendEvent(event);
		
	}
	
	public Date getLastModified(ECommerceStore store, String urlInSitemap) {
		try {
			return sitemapLastModifiedCache.get(store.getName())
	    		.get(urlInSitemap);
		} catch (Exception ex) {
			log.info("Exception in getLastModified", ex);
			return null;
		}
	}

}

