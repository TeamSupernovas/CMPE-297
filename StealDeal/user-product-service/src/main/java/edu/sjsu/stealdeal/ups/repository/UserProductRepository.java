package edu.sjsu.stealdeal.ups.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.sjsu.stealdeal.ups.model.UserProduct;

public interface UserProductRepository extends JpaRepository<UserProduct, Long> {
	public Page<UserProduct> findByUserId(long userId, Pageable pageable);
	
}
