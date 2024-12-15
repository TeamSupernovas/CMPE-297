package edu.sjsu.stealdeal.ups.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.sjsu.stealdeal.ups.model.PriceHistory;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
	
	public List<PriceHistory> findByProduct_productId(long productId);
	
}
