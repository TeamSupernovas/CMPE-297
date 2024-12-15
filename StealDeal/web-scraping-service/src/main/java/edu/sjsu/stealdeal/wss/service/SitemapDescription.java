package edu.sjsu.stealdeal.wss.service;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SitemapDescription {
	String url;
	List<String> productSectionPathInSitemap;
	String urlInSitemap; 
	String lastModifiedInSitemap;
}
