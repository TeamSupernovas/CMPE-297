package edu.sjsu.stealdeal.wss.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder=true)
@Entity
@Table(name = "scraped_products")
public class ScrapedProduct {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long scrapedProductId;
	private long productId;
	
	@Column(columnDefinition = "TEXT")
    private String name;
	
	@Column(columnDefinition = "TEXT")
    private String description;
	
	@Column(columnDefinition = "TEXT")
    private String productURL;
	
    private String storeProductId;
    private String storeName;
    private double currentPrice;
    private double previousPrice;
    private String size;
    private String color;
    
    @Column(columnDefinition = "TEXT")
    private String commentSummary;
    
    @Column(columnDefinition = "TEXT")
    private String productImageURL;
    
    @Column(columnDefinition = "TEXT")
    private String urlInSitemap;
    @Column(name="createdAt", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt;
    
    @Column(name="updatedAt", nullable = false, updatable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime updatedAt;

	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
    	updatedAt = LocalDateTime.now();
    }
    
    
}
