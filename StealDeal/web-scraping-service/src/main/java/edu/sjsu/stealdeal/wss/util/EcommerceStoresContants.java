package edu.sjsu.stealdeal.wss.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.sjsu.stealdeal.wss.service.ECommerceStore;
import edu.sjsu.stealdeal.wss.service.SitemapDescription;

public class EcommerceStoresContants {
	
	public final static SitemapDescription gapSitemapDescription = new SitemapDescription(
		      "https://www.gap.com/native-product-sitemap.xml",
		      List.of("urlset", "url"),
		      "loc",
		      "lastmod"
		  );
	public final static SitemapDescription bananaRepublicSitemapDescription = new SitemapDescription(
		      "https://bananarepublic.gap.com/native-sitemap.xml",
		      List.of("urlset", "url"),
		      "loc",
		      "lastmod"
		  );
	public final static SitemapDescription oldNavySitemapDescription = new SitemapDescription(
		      "https://oldnavy.gap.com/native-sitemap.xml",
		      List.of("urlset", "url"),
		      "loc",
		      "lastmod"
		  );
	public final static SitemapDescription athletaSitemapDescription = new SitemapDescription(
		      "https://athleta.gap.com/native-sitemap.xml",
		      List.of("urlset", "url"),
		      "loc",
		      "lastmod"
		  );
	
	public static final ECommerceStore GAP = new ECommerceStore("gap", gapSitemapDescription);
	public static final ECommerceStore BANANA_REPUBLIC = new ECommerceStore("banarepublic", bananaRepublicSitemapDescription);
	public static final ECommerceStore OLD_NAVY = new ECommerceStore("oldnavy", oldNavySitemapDescription);
	public static final ECommerceStore ATHLETA = new ECommerceStore("athleta", athletaSitemapDescription);

    private static final Map<String, ECommerceStore> storeMap = new HashMap<>();

    static {
        storeMap.put(GAP.getName(), GAP);
        storeMap.put(BANANA_REPUBLIC.getName(), BANANA_REPUBLIC);
        storeMap.put(OLD_NAVY.getName(), OLD_NAVY);
        storeMap.put(ATHLETA.getName(), ATHLETA);
    }

    public static ECommerceStore getECommerceStore(String name) {
        return storeMap.get(name);
    }
}
