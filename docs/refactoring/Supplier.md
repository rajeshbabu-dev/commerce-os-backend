# Supplier Module Refactoring

## Purpose
Refactor the Supplier module to: fix N+1 query performance, add pagination to list endpoints, standardize on MapStruct mapping, and remove dead code.

## Files Modified

### Repositories
| File | Changes |
|------|---------|
| `SupplierPerformanceRepository.java` | Added `@EntityGraph(attributePaths = "supplier")` on `findBySupplierId()` to eagerly fetch the Supplier. Added `findAllBySupplierIdIn(List<UUID>)` batch query method (also with `@EntityGraph`) for N+1 fix. |
| `SupplierRepository.java` | Changed `findByDeactivatedAtIsNull()` to accept `Pageable` parameter, returning `Page<Supplier>` instead of `List<Supplier>`. |

### Service
| File | Changes |
|------|---------|
| `SupplierService.java` | Refactored `listSuppliers()` to accept `Pageable` and return `PagedResponse<SupplierResponse>`. **N+1 fix**: Instead of querying performance for each supplier in a loop (1 + N queries), now fetches all performance records in a single batch query and builds a `Map<UUID, SupplierPerformance>` for O(1) lookups. |

### Controller
| File | Changes |
|------|---------|
| `SupplierController.java` | Changed `listSuppliers()` endpoint to accept `@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)` and return `PagedResponse<SupplierResponse>` wrapped in `ApiResponse`. |

### DTOs
| File | Changes |
|------|---------|
| `SupplierResponse.java` | Removed dead `fromEntity()` static method. Now a pure record. |
| `SupplierProductResponse.java` | Removed dead `fromEntity()` static method. Now a pure record. |
| `SupplierPerformanceResponse.java` | Removed dead `fromEntity()` static method. Now a pure record. |

### Mapper
| File | Changes |
|------|---------|
| `SupplierMapper.java` | Removed unused single-arg `toSupplierResponse(Supplier)` method and unused `toSupplierResponseList(List<Supplier>)` method — the service always uses the two-arg overload. |

## API Changes

### `GET /api/v1/suppliers`
**Before:** Returns `ApiResponse<List<SupplierResponse>>` (unpaginated, no sorting)
**After:** Returns `ApiResponse<PagedResponse<SupplierResponse>>` with default `size=20, sort=createdAt, direction=desc`. Supports `?page=0&size=10&sort=name,asc` query parameters.

## Performance Improvements
- **N+1 Query Fix**: `listSuppliers()` previously made 1 + N queries (1 for suppliers + 1 per supplier for performance). Now makes exactly 2 queries regardless of page size.
- **EntityGraph**: `@EntityGraph` on `SupplierPerformanceRepository` eagerly fetches the Supplier association in the same query, avoiding lazy-load N+1.

## Why Each Change Was Made
- **N+1 fix**: The loop-based performance fetching was the most significant performance bottleneck in the supplier module.
- **Pagination**: Prevents unbounded result sets and aligns with the Inventory module pattern established in Phase 3.
- **Dead code removal**: `fromEntity()` was redundant now that MapStruct handles all entity-to-DTO conversions.
- **Unused mapper methods**: The single-arg `toSupplierResponse(Supplier)` was never called anywhere.

## Remaining Improvements
- Add unit tests for the paginated `listSuppliers()` method
- Add integration test for pagination and sorting
