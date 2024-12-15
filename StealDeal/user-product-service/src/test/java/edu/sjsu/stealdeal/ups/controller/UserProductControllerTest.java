package edu.sjsu.stealdeal.ups.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.sjsu.stealdeal.ups.dto.*;
import edu.sjsu.stealdeal.ups.exception.ResourceNotFoundException;
import edu.sjsu.stealdeal.ups.service.UserProductService;

class UserProductControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private UserProductController userProductController;

    @Mock
    private UserProductService userProductService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userProductController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetUserProductById() throws Exception {
        long userProductId = 1L;

        GetUserProductResponse response = GetUserProductResponse.builder()
                .productDTO(ProductDTO.builder()
                        .productId(userProductId)
                        .name("Test Product")
                        .currentPrice(100.0)
                        .build())
                .build();

        when(userProductService.getUserProductById(userProductId)).thenReturn(response);

        mockMvc.perform(get("/user-products/{userProductId}", userProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productDTO.productId").value(userProductId))
                .andExpect(jsonPath("$.productDTO.name").value("Test Product"));

        verify(userProductService, times(1)).getUserProductById(userProductId);
    }

    @Test
    void testCreateUserProduct() throws Exception {
        CreateUserProductRequest request = CreateUserProductRequest.builder()
                .userId(1L)
                .URL("http://test.com")
                .storeName("Test Store")
                .build();

        CreateUserProductResponse response = CreateUserProductResponse.builder()
                .userProductId(1L)
                .message("Product saved for price tracking")
                .build();

        when(userProductService.createProduct(any(CreateUserProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/user-products/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userProductId").value(1))
                .andExpect(jsonPath("$.message").value("Product saved for price tracking"));

        verify(userProductService, times(1)).createProduct(any(CreateUserProductRequest.class));
    }

    @Test
    void testDeleteUserProduct() throws Exception {
        long userProductId = 1L;

        doNothing().when(userProductService).deleteUserProduct(userProductId);

        mockMvc.perform(delete("/user-products/{userProductId}", userProductId))
                .andExpect(status().isNoContent());

        verify(userProductService, times(1)).deleteUserProduct(userProductId);
    }
}
