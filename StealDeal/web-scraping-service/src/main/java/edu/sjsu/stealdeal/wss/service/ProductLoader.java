package edu.sjsu.stealdeal.wss.service;

import java.net.URI;
import java.util.Arrays;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.sjsu.stealdeal.wss.model.ScrapedProduct;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ProductLoader {

	public ScrapedProduct load(ECommerceStore store, ScrapedProduct previouslyScrapedProduct) {
		String url =  previouslyScrapedProduct.getProductURL();
		Document htmlDocument = null;
		URI documentUri = null;

		try {
			documentUri = new URI(url);
		} catch (Exception ex) {
			log.info("uri not properly formatted: " + url);
			return null;
		}
		try {
			htmlDocument = HttpFileLoader.loadFileToJsoupDocument(url);
			if (htmlDocument == null) {
				log.info("Could not load html document for url: {}", url);
				return null;
			}
		} catch (Exception ex) {
			log.info("exception in loading html document for url: {}", url, ex);
			return null;
		}

		ScrapedProduct.ScrapedProductBuilder productBuilder = previouslyScrapedProduct.toBuilder();
		productBuilder.previousPrice(previouslyScrapedProduct.getCurrentPrice());
		
		// find storeProductId from uri itself
		for (String pair: documentUri.getQuery().split("&")) {
			int idx = pair.indexOf("=");
			String queryKey = pair.substring(0, idx);
			if (queryKey.equals("pid")) {
				productBuilder.storeProductId(pair.substring(idx + 1));
				break;
			}
		}

		String normalizedUrl = htmlDocument.selectFirst("head").selectFirst("link[rel=canonical]").attr("href");

		productBuilder.urlInSitemap(normalizedUrl);

		
		String origPriceData = htmlDocument.getElementsByTag("div").stream()
				.filter(element -> element.hasClass("pdp-pricing")).map(Element::text).findFirst().orElse(null);
		
		if (Strings.isNullOrEmpty(origPriceData)) {
			origPriceData = htmlDocument.getElementsByTag("div").stream()
					.filter(element -> element.hasClass("amount-price")).map(Element::text).findFirst().orElse(null);
		}
		
		try {
            String [] data = origPriceData.split(" ");// to cater to values like Now $18
			String price = Arrays.stream(data)
                .filter(p -> p.charAt(0) == '$')
                .findFirst()
                .orElse(data[0]);	
			
			
			if (price.charAt(0) == '$') {
				price = price.substring(1); // substring is required when price comes as $88.00 
			} 
		
			productBuilder.currentPrice(Double.valueOf(price.trim()));
					
		} catch (Exception ex) {
			log.info("Exception for price data: {}", origPriceData ,ex);
		}

		String description = htmlDocument.selectFirst("head").selectFirst("meta[name=description]").attr("content");

		productBuilder.description(description);

		String imageUrl = htmlDocument.selectFirst("head").selectFirst("link[rel=preload][as=image]").attr("href");
		
		if (imageUrl.charAt(0) == '/') {
			//image url is relative
			imageUrl = documentUri.getScheme() + "://" + documentUri.getHost() + imageUrl;
		}

		productBuilder.productImageURL(imageUrl);

		String size = htmlDocument.getElementsByTag("input").stream()
				.filter(element -> element.attr("name").equals("buy-box-Size"))
				.filter(element -> element.attr("type").equals("radio"))
				.filter(element -> element.attr("aria-checked").equals("true")).map(element -> element.attr("value"))
				.findFirst().orElse(null);
		productBuilder.size(size);

		String color = htmlDocument.getElementsByTag("span").stream()
				.filter(element -> element.attr("class").equals("swatch-label__value")).map(Element::text).findFirst()
				.orElse(null);
		productBuilder.color(color);

		Element info = htmlDocument.getElementsByTag("script").stream()
				.filter(element -> "pdpData".equals(element.id())).findFirst().orElse(null);
		if (info == null) {
			log.info("script not found for JSON parsing");
			return productBuilder.build();
		}
		try {
			String scriptText = info.html();
			int position = scriptText.indexOf("window.__PRODUCT_PAGE_STATE__");
			String jsonString = findLargestSubstringWithinBracket(scriptText, position).trim();
			if (jsonString.charAt(0) == '"' && jsonString.charAt(jsonString.length() - 1) == '"') {
				// correct string
				jsonString = StringEscapeUtils.unescapeJava(jsonString.substring(1, jsonString.length() - 1));
				JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject()
						.getAsJsonObject("productData");
				// LOGGER.info("Found product data " + jsonObject.toString());
				productBuilder.name(jsonObject.get("name").getAsString());
				// productData.put("sizeChartId", jsonObject.get("sizeChartId").getAsString());
			}
		} catch (Exception ex) {
			log.info("Exception in JSON parsing: {}", ex.getMessage(), ex);
		}
		return productBuilder.build();
	}

	public static String findLargestSubstringWithinBracket(String input, int searchPos) {
		int startIndex = -1;
		int endIndex = 0;

		for (int i = searchPos; i < input.length(); i++) {
			char currentChar = input.charAt(i);
			if (currentChar == '(' && startIndex == -1) {
				// Found the opening bracket
				startIndex = i;
			} else if (currentChar == ')') {
				// Found a closing bracket
				endIndex = i;
			}
		}

		// Extract the largest substring
		return input.substring(startIndex + 1, endIndex);
	}
}
