package edu.sjsu.stealdeal.ups.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Builder
@Entity
@Table(name = "price_history")
public class PriceHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private long priceHistoryId;
    private double price;
    private LocalDateTime priceRecordTime;
    @Column(name="createdAt", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime deletedAt;
	
    @ManyToOne
    @JoinColumn(name = "productId")
    private Product product;
    
	@PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
