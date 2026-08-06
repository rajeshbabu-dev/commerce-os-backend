# Phase 1: Authentication Module & Cross-Cutting Concerns Refactoring

## Module Name
Authentication & Platform Infrastructure

## Purpose
Extract cross-cutting concerns (exception handling, security, logging, configuration) from the IAM module into a shared `platform` package, establishing proper dependency direction (modules depend on platform, not vice versa).

## Files Created (7 new files in platform package)

| File | Location | Purpose |
|------|----------|---------|
| `BusinessException.java` | `platform/exception/` | Cross-cutting business exception used by all modules |
| `GlobalExceptionHandler.java` | `platform/exception/` | Centralized REST exception handler returning `ApiResponse` |
| `CorrelationIdFilter.java` | `platform/logging/` | Request/response correlation ID for distributed tracing |
| `CorsConfig.java` | `platform/security/` | CORS configuration for frontend origins |
| `HealthController.java` | `platform/web/` | Health check endpoint for Docker/monitoring |
| `OpenApiConfig.java` | `platform/config/` | SpringDoc OpenAPI/Swagger configuration |
| `RedisConfig.java` | `platform/config/` | Redis template configuration for all modules |

## Files Modified (8 files updated)

| File | Change |
|------|--------|
| `iam/config/SecurityConfig.java` | Added security headers (HSTS, X-Frame-Options, X-Content-Type-Options, XSS), replaced raw JSON strings with `ApiResponse` + `ObjectMapper` in `AuthenticationEntryPoint` and `AccessDeniedHandler`, updated `CorrelationIdFilter` import to `platform.logging` |
| `iam/service/AuthService.java` | Updated `BusinessException` import to `platform.exception` |
| `inventory/service/InventoryService.java` | Updated `BusinessException` import to `platform.exception` |
| `supplier/service/SupplierService.java` | Updated `BusinessException` import to `platform.exception` |
| `recommendation/service/RecommendationService.java` | Updated `BusinessException` import to `platform.exception` |
| `iam/service/AuthServiceTest.java` | Updated `BusinessException` import to `platform.exception` |
| `inventory/service/InventoryServiceTest.java` | Updated `BusinessException` import to `platform.exception` |
| `supplier/service/SupplierServiceTest.java` | Updated `BusinessException` import to `platform.exception` |

## Files Removed (7 old files deleted)

| Old Location | Reason |
|-------------|--------|
| `iam/exception/BusinessException.java` | Moved to `platform/exception/` |
| `iam/exception/GlobalExceptionHandler.java` | Moved to `platform/exception/` |
| `iam/filter/CorrelationIdFilter.java` | Moved to `platform/logging/` |
| `iam/config/CorsConfig.java` | Moved to `platform/security/` |
| `iam/config/RedisConfig.java` | Moved to `platform/config/` |
| `iam/config/OpenApiConfig.java` | Moved to `platform/config/` |
| `iam/controller/HealthController.java` | Moved to `platform/web/` |

## Database Changes
None — this phase only reorganized Java packages.

## Security Changes
- **SecurityHeaders**: Added `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `X-XSS-Protection: 1; mode=block`, `Strict-Transport-Security: max-age=31536000; includeSubDomains`
- **AuthenticationEntryPoint**: Now returns structured `ApiResponse<Void>` with proper JSON serialization via `ObjectMapper` instead of raw string
- **AccessDeniedHandler**: Same improvement — structured `ApiResponse` instead of raw string

## Architecture Improvement
```
BEFORE (flat iam package):
  iam/
    config/SecurityConfig, CorsConfig, RedisConfig, OpenApiConfig, AppConfig, JwtConfig
    exception/BusinessException, GlobalExceptionHandler
    filter/CorrelationIdFilter, JwtAuthenticationFilter
    controller/AuthController, HealthController
    ...

AFTER (layered architecture):
  platform/                        iam/
    config/OpenApiConfig, RedisConfig   config/SecurityConfig, AppConfig, JwtConfig
    exception/BusinessException,        controller/AuthController
        GlobalExceptionHandler          dto/...
    logging/CorrelationIdFilter         entity/...
    security/CorsConfig                 filter/JwtAuthenticationFilter
    web/HealthController                service/AuthService
                                        util/JwtUtil
                                        redis/...
                                        repository/...
```

## Why Each Change Was Made
1. **BusinessException** is used by IAM, Inventory, Supplier, and Recommendation modules — it belongs in a shared package
2. **GlobalExceptionHandler** handles exceptions for ALL controllers — must be in a shared location
3. **CorrelationIdFilter** is a cross-cutting observability concern, not IAM-specific
4. **CorsConfig** configures CORS for the entire application, not just IAM
5. **HealthController** is a platform infrastructure endpoint, not IAM-specific
6. **OpenApiConfig** documents the entire API, not just IAM
7. **RedisConfig** provides Redis infrastructure used by multiple modules
8. **Security headers** prevent common web vulnerabilities (clickjacking, MIME sniffing, XSS, MITM)
9. **ApiResponse in handlers** ensures consistent response format even for authentication failures

## Benefits
- Clean dependency direction: `iam` → `platform`, `inventory` → `platform`, etc.
- Cross-cutting concerns are reusable across all current and future modules
- Security hardened with HTTP security headers
- Consistent error response format across all endpoints including auth failures
- Easier to add new modules (they just depend on `platform`)

## Remaining Improvements
- Move `JwtAuthenticationFilter` and `SecurityConfig` to `platform.security` (requires refactoring `JwtUtil` to remove `User` entity dependency)
- Move `JwtUtil` to `platform.security` after decoupling from `User` entity
- Add `UserContext` / `UserContextHolder` for request context propagation across modules
- Add database indexes for frequently queried columns
- Add pagination to list endpoints
- Implement MapStruct for DTO↔Entity mapping
