# Phase 5 — Recommendation Module Refactoring

## Purpose
Revert all modules to ModelMapper (removing MapStruct), standardize mapper pattern across the entire project, add pagination to Recommendation endpoints, and fix N+1 queries in RecommendationService.

## Key Decision: ModelMapper over MapStruct
All three mappers (Inventory, Supplier, Recommendation) now use **ModelMapper** as the sole mapping library. This provides runtime flexibility, consistent configuration, and eliminates compile-time annotation processing overhead.

## Files Modified

### pom.xml
- **Removed**: `org.mapstruct:mapstruct:1.6.3` dependency
- **Removed**: `org.mapstruct:mapstruct-processor:1.6.3` from annotation processor paths
- **Added**: `org.modelmapper:modelmapper:3.2.0` dependency

### common/config/ModelMapperConfig.java (Created)
- Spring `@Configuration` that registers a `ModelMapper` bean
- Uses `MatchingStrategies.STRICT` for predictable field matching
- Enables `skipNullEnabled` to avoid overwriting existing values with null

### inventory/mapper/InventoryMapper.java
- **Was**: MapStruct `@Mapper(componentModel = "spring")` interface
- **Now**: ModelMapper `@Component` class with `@RequiredArgsConstructor`
- Hand-written methods for `StockItemResponse` (computes `status` field) and `StockMovementResponse` (maps nested `stockItem.id` → `stockItemId`)
- `ProductResponse` uses `modelMapper.map()` (all field names match directly)

### supplier/mapper/SupplierMapper.java
- **Was**: MapStruct `@Mapper(componentModel = "spring")` interface
- **Now**: ModelMapper `@Component` class with `@RequiredArgsConstructor`
- Hand-written `toSupplierResponse(Supplier, SupplierPerformance)` for composite response
- Hand-written `toSupplierProductResponse(SupplierProduct)` for nested supplier fields
- Hand-written `toPerformanceResponse(SupplierPerformance)` for nested supplier ID

### recommendation/mapper/RecommendationMapper.java
- **Was**: MapStruct `@Mapper(componentModel = "spring")` interface
- **Now**: ModelMapper `@Component` class with `@RequiredArgsConstructor`
- Uses `modelMapper.map()` for both single and list conversions (all field names match)

### recommendation/repository/PurchaseRecommendationRepository.java
- **Added**: `findAllByOrderByCreatedAtDesc(Pageable)` — paginated list all
- **Added**: `findByProductIdOrderByProductIdDesc(UUID, Pageable)` — paginated by product
- **Added**: `findByStatusOrderByCreatedAtDesc(String, Pageable)` — paginated by status

### recommendation/service/RecommendationService.java
- **Fixed N+1**: `generate()` now batch-fetches all `SupplierPerformance` records via `findAllBySupplierIdIn()` instead of calling `findBySupplierId()` per supplier in a loop
- **Added pagination**: `listAll(Pageable)`, `getByProductId(UUID, Pageable)`, `getByStatus(String, Pageable)` all return `Page<PurchaseRecommendation>`
- Removed old `List`-returning methods (now paginated only)

### recommendation/controller/RecommendationController.java
- `GET /api/v1/recommendations` now accepts `@PageableDefault(size=20, sort="createdAt", direction=DESC)` and returns `PagedResponse<PurchaseRecommendationResponse>`
- `GET /api/v1/recommendations/product/{productId}` now accepts `Pageable` and returns `PagedResponse`
- **Added**: `GET /api/v1/recommendations/status/{status}` — filter by status with pagination

### recommendation/dto/response/PurchaseRecommendationResponse.java
- **Removed**: Dead `fromEntity()` static method (mapper now handles conversion)

## N+1 Fix Detail
**Before** (N+1):
```java
supplierProducts.stream().map(sp -> {
    Optional<SupplierPerformance> perf =
        supplierPerformanceRepository.findBySupplierId(sp.getSupplier().getId()); // N queries
    ...
}).toList();
```

**After** (batch):
```java
List<UUID> supplierIds = supplierProducts.stream().map(sp -> sp.getSupplier().getId()).toList();
Map<UUID, SupplierPerformance> perfMap =
    supplierPerformanceRepository.findAllBySupplierIdIn(supplierIds).stream() // 1 query
        .collect(Collectors.toMap(p -> p.getSupplier().getId(), Function.identity()));
```

## Tests Updated
- `RecommendationServiceTest` updated to mock `findAllBySupplierIdIn()` instead of `findBySupplierId()`
- Added paginated tests: `listAll_ReturnsPaged()`, `getByProductId_ReturnsPaged()`

## Benefits
- **Consistent mapping library** across all modules (ModelMapper only)
- **No compile-time annotation processing** for mappers
- **Runtime configuration** via Spring bean (STRICT matching, skip nulls)
- **N+1 eliminated** in recommendation generation (1 query vs N queries)
- **Paginated list endpoints** for all recommendation queries
- **Filterable by status** endpoint added

## Remaining Improvements
- Add integration tests for paginated endpoints
- Consider adding ModelMapper configuration profiles per module if field naming diverges
