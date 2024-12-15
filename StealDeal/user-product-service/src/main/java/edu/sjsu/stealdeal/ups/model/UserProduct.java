package edu.sjsu.stealdeal.ups.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "user_products")
public class UserProduct {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userProductId;
	private long userId;
	private long productId;
	
	@Column(name="createdAt", nullable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime deletedAt;
	
	@PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
