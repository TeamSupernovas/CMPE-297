package edu.sjsu.stealdeal.ups.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.sjsu.stealdeal.ups.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
