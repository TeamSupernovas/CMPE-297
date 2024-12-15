package edu.sjsu.stealdeal.wss.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ECommerceStore {
	
	private String name;
	private SitemapDescription sitemapDescription;
	
}