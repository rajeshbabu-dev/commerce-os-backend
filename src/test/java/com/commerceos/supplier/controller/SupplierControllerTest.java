package com.commerceos.supplier.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerceos.common.dto.PagedResponse;
import com.commerceos.platform.security.JwtAuthenticationFilter;
import com.commerceos.platform.security.JwtUtil;
import com.commerceos.supplier.dto.request.CreateSupplierRequestDto;
import com.commerceos.supplier.dto.response.SupplierResponseDto;
import com.commerceos.supplier.service.SupplierService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SupplierController.class)
@AutoConfigureMockMvc(addFilters = false)
class SupplierControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private SupplierService supplierService;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("POST /api/v1/suppliers creates supplier")
  @WithMockUser(roles = "ADMIN")
  void createSupplier_Success() throws Exception {
    CreateSupplierRequestDto req =
        new CreateSupplierRequestDto(
            "Acme Corp", "contact@acme.com", "+1234567890", "123 St", "NET30");
    SupplierResponseDto resp =
        new SupplierResponseDto(
            UUID.randomUUID(),
            "Acme Corp",
            "contact@acme.com",
            "+1234567890",
            "123 St",
            "NET30",
            null,
            LocalDateTime.now(),
            LocalDateTime.now());

    when(supplierService.createSupplier(any())).thenReturn(resp);

    mockMvc
        .perform(
            post("/api/v1/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.name").value("Acme Corp"));
  }

  @Test
  @DisplayName("GET /api/v1/suppliers returns paged list")
  @WithMockUser(roles = "VIEWER")
  void listSuppliers_Success() throws Exception {
    SupplierResponseDto resp =
        new SupplierResponseDto(
            UUID.randomUUID(),
            "Acme Corp",
            "contact@acme.com",
            "+1234567890",
            "123 St",
            "NET30",
            null,
            LocalDateTime.now(),
            LocalDateTime.now());

    PagedResponse<SupplierResponseDto> paged =
        PagedResponse.from(new PageImpl<>(List.of(resp), PageRequest.of(0, 20), 1), List.of(resp));
    when(supplierService.listSuppliers(any())).thenReturn(paged);

    mockMvc
        .perform(get("/api/v1/suppliers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].name").value("Acme Corp"));
  }
}
