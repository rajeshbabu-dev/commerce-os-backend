package com.commerceos.iam.controller;

import com.commerceos.iam.dto.ApiResponse;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Health check endpoint for Docker Compose health checks and monitoring. */
@RestController
public class HealthController {

  @GetMapping("/api/v1/health")
  public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
    Map<String, Object> healthInfo =
        Map.of(
            "status", "UP",
            "service", "commerceos",
            "timestamp", Instant.now().toString());
    return ResponseEntity.ok(ApiResponse.success("Service is healthy", healthInfo));
  }
}
