package edu.sjsu.stealdeal.ups.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
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
@Table(name = "products")
public class Product {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Column(name="createdAt", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime deletedAt;
	
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<PriceHistory> priceHistory;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
