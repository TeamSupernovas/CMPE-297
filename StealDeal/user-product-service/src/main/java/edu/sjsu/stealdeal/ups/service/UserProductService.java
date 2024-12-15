package edu.sjsu.stealdeal.ups.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.sjsu.stealdeal.ups.dto.CreateUserProductRequest;
import edu.sjsu.stealdeal.ups.dto.CreateUserProductResponse;
import edu.sjsu.stealdeal.ups.dto.GetPriceHistoryResponse;
import edu.sjsu.stealdeal.ups.dto.GetUserProductResponse;
import edu.sjsu.stealdeal.ups.dto.GetUserProductsResponse;
import edu.sjsu.stealdeal.ups.dto.PriceDTO;
import edu.sjsu.stealdeal.ups.dto.ProductDTO;
import edu.sjsu.stealdeal.ups.event.ProductEvent;
import edu.sjsu.stealdeal.ups.event.ProductEventProducer;
import edu.sjsu.stealdeal.ups.exception.ResourceNotFoundException;
import edu.sjsu.stealdeal.ups.model.PriceHistory;
import edu.sjsu.stealdeal.ups.model.Product;
import edu.sjsu.stealdeal.ups.model.UserProduct;
import edu.sjsu.stealdeal.ups.repository.PriceHistoryRepository;
import edu.sjsu.stealdeal.ups.repository.ProductRepository;
import edu.sjsu.stealdeal.ups.repository.UserProductRepository;
import jakarta.transaction.Transactional;

@Service
public class UserProductService {

	@Autowired
	private ProductEventProducer productEventProducer;

	@Autowired
	private UserProductRepository userProductRepository;

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private PriceHistoryRepository priceHistoryRepository;

	@Transactional
	public CreateUserProductResponse createProduct(CreateUserProductRequest createUserProductRequest) {

		Product product = Product.builder()
				.storeName(createUserProductRequest.getStoreName())
				.productURL(createUserProductRequest.getURL())
				.build();

		Product createdProduct = productRepository.save(product);

		UserProduct userProduct = UserProduct.builder()
				.userId(createUserProductRequest.getUserId())
				.productId(createdProduct.getProductId())
				.build();

		UserProduct createdUserProduct = userProductRepository.save(userProduct);

		ProductEvent productEvent = ProductEvent.builder()
				.productId(createdProduct.getProductId())
				.productURL(createdProduct.getProductURL())
				.storeName(createdProduct.getStoreName())
				.build();

		productEventProducer.sendEvent(productEvent);

		CreateUserProductResponse createUserProductResponse = CreateUserProductResponse.builder()
				.userProductId(createdUserProduct.getUserProductId())
				.message("Product saved for price tracking")
				.build();

		return createUserProductResponse;
	}
	
	public void deleteUserProduct(long userProductId) throws ResourceNotFoundException {
		UserProduct userProduct = userProductRepository.findById(userProductId)
				.orElseThrow(() -> new ResourceNotFoundException("User Product does not exist"));

		userProductRepository.delete(userProduct);
	}
	
	public GetUserProductResponse getUserProductById(long userProductId) throws ResourceNotFoundException {
		UserProduct userProduct = userProductRepository.findById(userProductId)
				.orElseThrow(() -> new ResourceNotFoundException("User Product does not exist"));

		Product product = productRepository.findById(userProduct.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product does not exist"));

		ProductDTO productDTO = toProductDTO(product, userProduct.getUserProductId());

		return GetUserProductResponse.builder().productDTO(productDTO).build();

	}

	private static ProductDTO toProductDTO(Product product, long userProductId) {
		ProductDTO productDTO = ProductDTO.builder()
				.productId(userProductId)
				.name(product.getName())
				.description(product.getDescription())
				.size(product.getSize())
				.color(product.getColor())
				.productImageURL(product.getProductImageURL())
				.commentSummary(product.getCommentSummary())
				.currentPrice(product.getCurrentPrice())
				.previousPrice(product.getPreviousPrice())
				.build();
		return productDTO;
	}
	
	public GetUserProductsResponse getUserProductsByUserId(long userProductId, Pageable pageable) {
		Page<UserProduct> userProductsPage = userProductRepository.findByUserId(userProductId, pageable);
		
		List<ProductDTO> productDTOs = new ArrayList<>();
		userProductsPage.getContent().forEach(userProduct -> {
	        productRepository.findById(userProduct.getProductId()).ifPresent(product -> {
	            productDTOs.add(toProductDTO(product, userProduct.getUserProductId()));
	        });
	    });

	    return GetUserProductsResponse.builder()
	        .productDTOs(productDTOs)
	        .currentPage(userProductsPage.getNumber())
			.pageSize(userProductsPage.getSize())
			.totalItems(userProductsPage.getTotalElements())
            .totalPages(userProductsPage.getTotalPages())
	        .build();

	}
	
	public GetPriceHistoryResponse getPriceHistory(long userProductId) throws ResourceNotFoundException {
		UserProduct userProduct = userProductRepository.findById(userProductId)
				.orElseThrow(() -> new ResourceNotFoundException("User Product does not exist"));
		
		List<PriceHistory> priceHistory =  priceHistoryRepository.findByProduct_productId(userProduct.getProductId());
		
		List<PriceDTO> priceDTO = new ArrayList<>();
		priceHistory.forEach(priceHis -> {
			priceDTO.add(toPriceDTO(priceHis));
			
		});
		
		return GetPriceHistoryResponse.builder().productId(userProduct.getUserProductId()).priceHistory(priceDTO).build();		
	}
	
	private static PriceDTO toPriceDTO(PriceHistory priceHistory) {
		return PriceDTO.builder().price(priceHistory.getPrice())
		.priceRecordTime(priceHistory.getPriceRecordTime()).build();
	}
}
