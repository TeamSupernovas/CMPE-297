package edu.sjsu.stealdeal.ups.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.sjsu.stealdeal.ups.dto.CreateUserProductRequest;
import edu.sjsu.stealdeal.ups.dto.CreateUserProductResponse;
import edu.sjsu.stealdeal.ups.dto.GetPriceHistoryResponse;
import edu.sjsu.stealdeal.ups.dto.GetUserProductResponse;
import edu.sjsu.stealdeal.ups.dto.GetUserProductsResponse;
import edu.sjsu.stealdeal.ups.exception.ResourceNotFoundException;
import edu.sjsu.stealdeal.ups.service.UserProductService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/user-products")
public class UserProductController {
	
	@Autowired
	private UserProductService userProductService;

    @GetMapping("/{userProductId}")
    public ResponseEntity<GetUserProductResponse> getUserProductById(@PathVariable long userProductId) throws ResourceNotFoundException {
        GetUserProductResponse getUserProductResponse = userProductService.getUserProductById(userProductId);
        return ResponseEntity.ok(getUserProductResponse);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<GetUserProductsResponse> getUserProductsByUserId(@PathVariable long userId, Pageable page) {
        GetUserProductsResponse getUserProductsResponse = userProductService.getUserProductsByUserId(userId, page);
        return ResponseEntity.ok(getUserProductsResponse);
    }

    @GetMapping("/{userProductId}/price-history")
    public ResponseEntity<GetPriceHistoryResponse> getPriceHistory(@PathVariable long userProductId) throws ResourceNotFoundException {
        GetPriceHistoryResponse getPriceHistoryResponse = userProductService.getPriceHistory(userProductId);
        return ResponseEntity.ok(getPriceHistoryResponse);
    }

    @PostMapping("/")
    public ResponseEntity<CreateUserProductResponse> createUserProduct(@RequestBody CreateUserProductRequest createUserProductRequest) {
    	CreateUserProductResponse createUserProductResponse = userProductService.createProduct(createUserProductRequest);
        URI location = URI.create("/user-products/" + createUserProductResponse.getUserProductId());
     return ResponseEntity.created(location).body(createUserProductResponse);
    }

    @DeleteMapping("/{userProductId}")
    public ResponseEntity<Void> deleteUserProduct(@PathVariable long userProductId) throws ResourceNotFoundException {
    	userProductService.deleteUserProduct(userProductId);
        return ResponseEntity.noContent().build();
    }
}