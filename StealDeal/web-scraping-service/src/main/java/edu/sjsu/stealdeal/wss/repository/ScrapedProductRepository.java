package edu.sjsu.stealdeal.wss.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.sjsu.stealdeal.wss.model.ScrapedProduct;

@Repository
public interface ScrapedProductRepository extends JpaRepository<ScrapedProduct, Long>{

}
