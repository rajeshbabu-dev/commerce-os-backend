package com.commerceos.inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceos.inventory.dto.request.CreateProductRequestDto;
import com.commerceos.inventory.dto.response.ProductResponseDto;
import com.commerceos.inventory.entity.Product;
import com.commerceos.inventory.mapper.InventoryMapper;
import com.commerceos.inventory.service.InventoryService;
import com.commerceos.platform.security.JwtAuthenticationFilter;
import com.commerceos.platform.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private InventoryService inventoryService;
  @MockitoBean private InventoryMapper inventoryMapper;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("POST /api/v1/inventory/products creates product")
  @WithMockUser(roles = "ADMIN")
  void createProduct_Success() throws Exception {
    CreateProductRequestDto req =
        new CreateProductRequestDto("Widget A", "SKU-123", "Test widget", "PCS");
    Product product =
        Product.builder().id(UUID.randomUUID()).name("Widget A").sku("SKU-123").build();
    ProductResponseDto resp =
        new ProductResponseDto(
            product.getId(),
            "Widget A",
            "SKU-123",
            "Test widget",
            "PCS",
            LocalDateTime.now(),
            LocalDateTime.now());

    when(inventoryService.createProduct(any())).thenReturn(product);
    when(inventoryMapper.toProductResponse(product)).thenReturn(resp);

    mockMvc
        .perform(
            post("/api/v1/inventory/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.name").value("Widget A"))
        .andExpect(jsonPath("$.data.sku").value("SKU-123"));
  }

  @Test
  @DisplayName("GET /api/v1/inventory/products returns paged list")
  @WithMockUser(roles = "VIEWER")
  void listProducts_Success() throws Exception {
    Product product =
        Product.builder().id(UUID.randomUUID()).name("Widget A").sku("SKU-123").build();
    ProductResponseDto resp =
        new ProductResponseDto(
            product.getId(),
            "Widget A",
            "SKU-123",
            "Test widget",
            "PCS",
            LocalDateTime.now(),
            LocalDateTime.now());

    when(inventoryService.listProducts(any())).thenReturn(new PageImpl<>(List.of(product)));
    when(inventoryMapper.toProductResponseList(any())).thenReturn(List.of(resp));

    mockMvc
        .perform(get("/api/v1/inventory/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].name").value("Widget A"));
  }
}
