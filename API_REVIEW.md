# API Review — project hiện tại

Tài liệu này mô tả luồng API thực tế của project, dựa trên mã nguồn đang có trong `controller`, `service`, `repository`, `exception`, và `config`. Mục tiêu là ghi lại cách project đang hoạt động, không mô tả theo suy đoán.

## 1. Quy ước chung

### 1.1. Response format
- Tất cả API thành công thường trả về `ApiResponse<T>`.
- Class `ApiResponse` có 3 trường: `code`, `message`, `result`.
- Đối với nhiều endpoint, `result` là `Page<T>`, `List<T>`, hoặc object DTO.
- Khi controller không gắn `code`/`message`, các giá trị còn lại sẽ phụ thuộc vào builder ở mỗi method; không phải lúc nào cũng đồng nhất.

### 1.2. Error handling
- `AppException` được bắt ở `GlobalExceptionHandler`.
- `ErrorCode` chứa mã lỗi, message và `HttpStatusCode`.
- Validation errors (`@Valid`, `BindException`, `ConstraintViolationException`, `MethodArgumentTypeMismatchException`, `MissingServletRequestParameterException`) được trả về HTTP 400.
- Chưa có handler chung cho mọi exception không phải `AppException` hoặc validation; vậy nên project vẫn còn chỗ để hoàn thiện.

### 1.3. Soft delete
- Nhiều entity dùng pattern `deleted = true` hoặc `findBy...DeletedFalse` thay vì xóa vật lý.
- Dù `deleted` không xuất hiện ở mọi entity nhưng convention tổng quát là soft delete.

### 1.4. Cấu hình bảo mật
- `WebSecurityConfig` disable CSRF và CORS.
- Dùng `httpBasic()`.
- Public endpoints:
  - `/v3/api-docs/**`
  - `/swagger-ui/**`
  - `/api/v1/auth/**`
  - `GET /api/v1/categories/**`
  - `GET /api/v1/products/**`
- Admin-only: `/api/v1/admin/**`
- Shipper-only: `/api/v1/shipper/**`
- Các API khác yêu cầu xác thực theo username/password.

## 2. Auth API

### `POST /api/v1/auth/register`

Request:
- `RegisterRequest`
- `@Validated @RequestBody`

Luồng:
1. Controller gọi `accountService.register(request)`.
2. Service kiểm tra trùng username/email.
3. Tạo `User` mới với password được mã hóa bằng BCrypt.
4. Gán role `USER`.
5. Lưu user vào DB.
6. Map ra `LoginResponse`.
7. Trả về `ApiResponse` với `code = 1000`, message `"Đăng ký thành công"`.

Error chính:
- `USERNAME_ALREADY_EXISTS`
- `EMAIL_ALREADY_EXISTS`

### `POST /api/v1/auth/login`

Request:
- `LoginRequest` gồm `username` và `password`

Luồng:
1. `AuthenticationManager` xác thực bằng `UsernamePasswordAuthenticationToken`.
2. Lấy user qua `authentication.getName()`.
3. Map sang `LoginResponse`.
4. Token được tạo bằng `Base64(username:password)`.
5. Trả về `ApiResponse` với `code = 1000` và message `"Đăng nhập thành công"`.

Lưu ý:
- Đây không phải JWT.
- Project đang dùng HTTP Basic authentication thực tế, nên `token` ở đây chỉ là chuỗi credentials dạng Base64 theo chuẩn Basic Auth.

### `GET /api/v1/auth/me`

Luồng:
1. Nhận `Principal` từ request.
2. Lấy `username` từ principal.
3. Tìm user trong DB.
4. Map sang `LoginResponse`.
5. Trả `ApiResponse` với code 1000.

Điểm cần lưu ý:
- Endpoint này được permitAll theo config, nhưng thực chất cần principal. Nếu request không có auth, code sẽ có thể lỗi do `principal == null`.

## 3. User management (`/api/v1/admin/users`)

### `POST /api/v1/admin/users`

Request:
- `UserCreateRequest` dạng `multipart/form-data`

Luồng:
1. `ADMIN` role kiểm tra qua `@PreAuthorize`.
2. Validation DTO.
3. Normalize username/phone và lowercase email.
4. Kiểm tra user với username/email/phone đã bị xóa chưa.
5. Lưu user mới.
6. Nếu có avatar, upload lên Cloudinary trước khi lưu.
7. Trả `UserResponse`.

Error chính:
- `USERNAME_ALREADY_EXISTS`
- `EMAIL_ALREADY_EXISTS`
- `PHONE_NUMBER_ALREADY_EXISTS`

### `GET /api/v1/admin/users`

Query:
- `page_size`
- `page_number`

Luồng:
1. Kiểm tra quyền admin.
2. Gọi `userService.getAllUsers(pageSize, pageNumber)`.
3. Service tạo `PageRequest` với `page_number - 1`.
4. Query `findAllByDeletedFalse`.
5. Map `User -> UserResponse`.
6. Return `Page<UserResponse>`.

### `GET /api/v1/admin/users/{userId}`
- Lấy user theo `userId` và `deleted = false`
- Nếu không tồn tại: `USER_NOT_FOUND`

### `PUT /api/v1/admin/users/{userId}`
- Dùng `multipart` để cập nhật user.
- Chỉ cập nhật field không null.
- Validate duplicate username/email/phone nếu trường đó được đổi.
- Nếu có avatar mới, upload Cloudinary rồi xóa ảnh cũ.

### `DELETE /api/v1/admin/users/{userId}`
- Soft delete user bằng `deleted = true`.
- Trả message `Delete User Successfully`.

### `PATCH /api/v1/admin/users/{userId}/role`
- Tham số: `role` kiểu `UserRole`
- Nếu `role == null`: `INVALID_USER_ROLE`
- Gán role mới cho user.

### `GET /api/v1/admin/users/filter`
Query:
- `username`
- `email`
- `full_name`
- `role`
- `page`
- `size`

Luồng:
1. Dùng `Specification` tích hợp điều kiện `deleted = false`.
2. `LIKE` ở username/email/fullName.
3. Match role exact.
4. Sort theo `createdAt` DESC.
5. Trả `Page<UserResponse>`.

## 4. Category API

### `POST /api/v1/admin/categories`
- `ADMIN` only
- Tạo category bằng `multipart/form-data`
- Nếu tên category đã tồn tại: `CATEGORY_NAME_ALREADY_EXISTS`
- Nếu `parentId` không hợp lệ: `CATEGORY_NOT_FOUND`
- Nếu có ảnh: upload Cloudinary

### `GET /api/v1/categories`
- Public
- Default: `page_size=10`, `page_number=1`
- Service trả `Page<CategoryResponse>`

### `GET /api/v1/categories/{categoryId}`
- Public
- Trả category theo id và chưa bị xóa
- Nếu không tồn tại: `CATEGORY_NOT_FOUND`

### `GET /api/v1/categories/tree`
- Public
- Lấy tất cả category chưa xóa và dựng cây theo parent-child
- Trả `List<CategoryResponse>`

### `PUT /api/v1/admin/categories/{categoryId}`
- Admin-only
- Cập nhật name/description/parentId/avatar
- Nếu `parentId` là chính mình hoặc tạo vòng lặp thì báo lỗi `INVALID_PARENT_CATEGORY`

### `DELETE /api/v1/admin/categories/{categoryId}`
- Admin-only
- Soft delete category
- Message: `Delete Category Successfully`

### `GET /api/v1/categories/filter`
- Public
- Filter `name`, `parent_id`, `page`, `size`
- Dùng `Specification` + `PageRequest`

## 5. Product API

### `POST /api/v1/admin/products`
- Admin-only
- `multipart/form-data`
- Validate category tồn tại
- Kiểm tra duplicate product name
- Upload thumbnail nếu có
- Trả `ProductResponse`

### `PUT /api/v1/admin/products/{productId}`
- Admin-only
- Cập nhật partial fields
- Nếu thay category: check category hợp lệ
- Nếu đổi tên: check duplicate tên khác
- Nếu upload ảnh mới: upload lên Cloudinary rồi xóa ảnh cũ

### `DELETE /api/v1/admin/products/{productId}`
- Soft delete product
- Message: `Delete Product Successfully`

### `GET /api/v1/products`
- Public
- Query: `page_size`, `page_number`
- `Page<ProductResponse>`

### `GET /api/v1/products/{productId}`
- Public
- Lấy product và danh sách biến thể thuộc product
- Trả `ProductDetailResponse`

### `GET /api/v1/products/filter`
- Public
- Filter: `name`, `category_id`, `page`, `size`
- Dùng specification + parent-child category matching

### `GET /api/v1/products/category/{categoryId}`
- Public
- Lấy sản phẩm theo category id

## 6. Product Variant API

### `POST /api/v1/admin/products/{productId}/variants`
- Admin-only
- Tạo biến thể cho product
- Validate `name` và `sku` không blank
- Check duplicate tên/SKU

### `PUT /api/v1/admin/products/variants/{variantId}`
- Admin-only
- Cập nhật biến thể
- Có thể cập nhật giá, số lượng, trạng thái, tên, SKU

### `DELETE /api/v1/admin/products/variants/{variantId}`
- Admin-only
- Soft delete nếu có logic tương ứng

### `GET /api/v1/admin/product-variants`
- Admin-only
- Pagination `page_size`, `page_number`

### `GET /api/v1/admin/product-variants/statistics`
- Admin-only
- Trả thống kê tổng quan về biến thể sản phẩm

### `GET /api/v1/admin/product-variants/filter`
- Admin-only
- Filter: `variant_name`, `sku`, `product_id`, `min_price`, `max_price`, `page`, `size`

### `GET /api/v1/admin/product-variants/export`
- Admin-only
- Export Excel bằng EasyExcel
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

## 7. Coupon API

Route base: `/api/v1/admin/coupons`

### CRUD
- `POST /api/v1/admin/coupons` create
- `GET /api/v1/admin/coupons` list paging
- `GET /api/v1/admin/coupons/{couponId}` get by id
- `PUT /api/v1/admin/coupons/{couponId}` update
- `DELETE /api/v1/admin/coupons/{couponId}` delete
- `PATCH /api/v1/admin/coupons/{couponId}/status` update status

### Filter
`GET /api/v1/admin/coupons/filter`
- `code`
- `status`
- `discount_type`
- `start_date_from`, `start_date_to`
- `end_date_from`, `end_date_to`
- `page`, `size`

Rules kiểm tra nghiệp vụ:
- `INVALID_COUPON_DATES` nếu end date < start date
- `INVALID_COUPON_DISCOUNT` nếu discount % > 100
- `COUPON_ALREADY_EXISTS` nếu code trùng

## 8. Cart API

Base path: `/api/v1/carts`

### `POST /api/v1/carts/items`
- User-only
- Thêm item vào giỏ
- `AddToCartRequest`
- kiểm tra stock, product variant, user

### `GET /api/v1/carts/items`
- User-only
- Lấy giỏ hàng hiện tại của user
- Trả `CartResponse`

### `PUT /api/v1/carts/items/{cartItemId}`
- User-only
- Cập nhật số lượng item
- kiểm tra `quantity` hợp lệ và stock đủ

### `DELETE /api/v1/carts/items/{cartItemId}`
- User-only
- Xóa item khỏi giỏ

## 9. Order API

### User side

#### `POST /api/v1/orders`
- User-only
- Tạo đơn hàng từ cart hoặc request tạo đơn
- `OrderCreateRequest`
- Kiểm tra tồn kho, coupon, user, status hợp lệ

#### `GET /api/v1/orders`
- User-only
- Danh sách đơn hàng của user theo pagination
- Trả `Page<OrderSummaryResponse>`

#### `GET /api/v1/orders/filter`
- User-only
- Filter theo `status`

#### `GET /api/v1/orders/{orderId}`
- User-only
- Trả chi tiết đơn hàng của user

#### `PUT /api/v1/orders/{orderId}/cancel`
- User-only
- Hủy đơn nếu trạng thái cho phép
- Nếu không thể hủy: `ORDER_CANNOT_BE_CANCELLED`

#### `GET /api/v1/orders/{orderId}/tracking`
- User-only
- Lấy lịch sử tracking order

### Admin side

#### `GET /api/v1/admin/orders/statistics`
- Admin-only
- Trả số liệu thống kê order

#### `GET /api/v1/admin/orders`
- Admin-only
- Danh sách tất cả đơn hàng

#### `GET /api/v1/admin/orders/filter`
- Admin-only
- Filter theo `status`, `page_size`, `page_number`

#### `GET /api/v1/admin/orders/{orderId}`
- Admin-only
- Chi tiết order của admin

#### `PATCH /api/v1/admin/orders/{orderId}/status`
- Admin-only
- Cập nhật trạng thái order
- Kiểm tra `INVALID_ORDER_STATUS_TRANSITION`

#### `GET /api/v1/admin/order/export`
- Admin-only
- Export Excel list order

### Shipper side

#### `GET /api/v1/shipper/orders/`
- Shipper-only
- Danh sách đơn hàng được giao cho shipper

#### `PATCH /api/v1/shipper/orders/{orderId}/status`
- Shipper-only
- Cập nhật trạng thái vận chuyển

## 10. Dòng dữ liệu hiện tại của project

Có thể tóm tắt luồng đi của project như sau:

- Client gọi REST endpoint
- Security xác thực và phân quyền theo role
- Controller nhận request và validate DTO
- Service thực hiện business logic
- Repository truy vấn DB
- Mapper chuyển entity sang response DTO
- Exception handler trả `ApiResponse` chuẩn hoặc `HttpStatus`

## 11. Điểm hiện tại cần lưu ý

- Project đang ở mức backend API thương mại điện tử demo/portfolio, chưa phải hệ thống production-ready hoàn chỉnh.
- Authentication đang dùng HTTP Basic thay vì JWT.
- `application.yaml` đang chứa cấu hình local (database, mail, Cloudinary), cần dùng environment variables hoặc local external config khi deploy.
- Một số endpoint, logic và validation còn chưa đồng nhất hoàn toàn về format response, nhưng nhìn chung đã có nền tảng khá rõ ràng cho việc phát triển tiếp.

## 12. Kết luận

Project có đầy đủ các module cốt lõi của một backend ecommerce: auth, user, catalog, product, variant, cart, order, coupon, phân quyền và export. Đây là một project rất phù hợp để dùng làm minh chứng kỹ năng Java backend fresher, vì nó cho thấy khả năng xây dựng API theo architecture rõ ràng, xử lý lỗi hợp lý và điều phối logic giữa các layer.

Category không tồn tại/bị xóa khi lấy ID hậu duệ trả `CATEGORY_NOT_FOUND`.

### `GET /api/v1/products/category/{categoryId}?page_size=10&page_number=1` (public)

```text
categoryId + query phân trang mặc định 10/1
→ xác nhận category chưa xóa (không có: CATEGORY_NOT_FOUND)
→ lấy category hiện tại và hậu duệ
→ PageRequest → query product deleted=false trong nhóm category
→ map ProductResponse → ApiResponse<Page<ProductResponse>>
```

## Product variant (ADMIN)

### `POST /api/v1/admin/products/{productId}/variants`

```text
multipart ProductVariantCreateRequest → ADMIN + Bean Validation
→ tìm product chưa xóa (không có: PRODUCT_NOT_FOUND)
→ tên variant trùng trong cùng product: PRODUCT_VARIANT_NAME_ALREADY_EXISTS
→ SKU trim/uppercase; SKU trùng toàn hệ thống hiện cũng bị báo PRODUCT_VARIANT_NAME_ALREADY_EXISTS
→ map variant, upload ảnh nếu có, gán product, save
→ map ProductVariantResponse; stockStatus = IN_STOCK nếu quantity > 0, ngược lại OUT_OF_STOCK
→ ApiResponse<ProductVariantResponse>
```

### `PUT /api/v1/admin/products/variants/{variantId}`

```text
multipart ProductVariantUpdateRequest dạng partial
→ tìm variant chưa xóa (không có: PRODUCT_VARIANT_NOT_FOUND)
→ tên không blank và không trùng variant khác cùng product
→ SKU không blank, trim/uppercase và không trùng variant khác
→ map các trường khác null; ảnh mới upload xong mới xóa ảnh cũ
→ save, map response và tính stockStatus
→ ApiResponse<ProductVariantResponse>
```

Tên/SKU blank trả `INVALID_PRODUCT_VARIANT`; tên trùng trả `PRODUCT_VARIANT_NAME_ALREADY_EXISTS`; SKU trùng trả `PRODUCT_VARIANT_SKU_ALREADY_EXISTS`.

### `DELETE /api/v1/admin/products/variants/{variantId}`

```text
variantId → tìm variant chưa xóa (không có: PRODUCT_VARIANT_NOT_FOUND)
→ set deleted=true
→ ApiResponse<Void>(message="Delete Variant success")
```

### `GET /api/v1/admin/product-variants?page_size=...&page_number=...`

```text
ADMIN + page_size/page_number bắt buộc
→ PageRequest(page_number - 1, page_size)
→ findAllByDeletedFalse → map response và tính stockStatus
→ ApiResponse<Page<ProductVariantResponse>>
```

### `GET /api/v1/admin/product-variants/filter?variant_name=&sku=&product_id=&min_price=&max_price=&page=1&size=10`

```text
filter tùy chọn → Specification deleted=false
→ variant_name/SKU LIKE; product_id bằng; min_price ≤ price; max_price ≥ price
→ min_price > max_price: INVALID_PRICE_RANGE
→ PageRequest(page - 1, size, createdAt DESC) → map response + stockStatus
→ ApiResponse<Page<ProductVariantResponse>>
```

## Cart (USER)

### `POST /api/v1/carts/items`

```text
JSON AddToCartRequest → validate productVariantId không blank, quantity >= 1
→ tìm user theo principal username (không có: USER_NOT_FOUND)
→ tìm cart; nếu chưa có thì tạo và gán user
→ tìm variant deleted=false (không có: PRODUCT_VARIANT_NOT_FOUND)
→ tìm cart item còn hoạt động
   → chưa có: kiểm tra quantity ≤ tồn kho, tạo item
   → đã có: cộng quantity hiện tại + yêu cầu, kiểm tra ≤ tồn kho, cập nhật
→ hết tồn: INSUFFICIENT_CART_STOCK
→ map CartItemResponse → ApiResponse<CartItemResponse>
```

### `GET /api/v1/carts/items`

```text
username từ Principal → tìm cart
→ không có: CartResponse(totalItems=0, subtotal=0, items=[])
→ có: tải items cùng variant; cộng quantity và price × quantity
→ gắn IN_STOCK/OUT_OF_STOCK từng item
→ CartMapper → ApiResponse<CartResponse>
```

### `PUT /api/v1/carts/items/{cartItemId}`

```text
USER + cartItemId + JSON UpdateCartItemRequest.quantity
→ tìm cart theo username (không có: CART_NOT_FOUND)
→ tìm item thuộc cart và deleted=false (không có: CART_ITEM_NOT_FOUND)
→ nếu quantity > tồn kho: INSUFFICIENT_CART_STOCK
→ gán quantity, map CartItemResponse → ApiResponse<CartItemResponse>
```

DTO/service hiện không chặn rõ quantity null, 0 hoặc âm; chỉ kiểm tra vượt tồn.

### `DELETE /api/v1/carts/items/{cartItemId}`

```text
USER + cartItemId → tìm cart (không có: CART_NOT_FOUND)
→ tìm item thuộc cart và deleted=false (không có: CART_ITEM_NOT_FOUND)
→ soft delete item
→ ApiResponse<Void>(message="Xoa san pham thanh cong")
```

## Order khách hàng (USER)

### `POST /api/v1/orders`

```text
JSON OrderCreateRequest → validate paymentMethod, recipient và địa chỉ
→ tìm User và Cart (USER_NOT_FOUND/CART_NOT_FOUND)
→ tải cart items, kiểm tra từng quantity ≤ tồn; cộng subtotal
→ couponCode có: tìm coupon ACTIVE, trong hạn, chưa hết usage
   → không hợp lệ: INVALID_COUPON
   → subtotal đạt minOrderValue: tính discount phần trăm/cố định, áp maxDiscountAmount và không vượt subtotal
   → chưa đạt minOrderValue: coupon vẫn gắn vào order, discount = 0
→ shippingFee = 500000; grandTotal = subtotal + shippingFee - discount
→ tạo Order PENDING/UNPAID, tracking number; save
→ tăng usedCount coupon nếu có
→ tạo OrderItem, trừ tồn kho, soft delete cart items; lưu items và TrackingLog
→ map Order + items → ApiResponse<OrderResponse>
```

Nếu tồn kho thiếu trả `INSUFFICIENT_ORDER_INVENTORY`. Chưa chặn cart rỗng, chưa tạo Payment/thực hiện giao dịch.

### `GET /api/v1/orders?page_size=10&page_number=1`

```text
USER + page query mặc định 10/1
→ tìm user chưa xóa (không có: USER_NOT_FOUND)
→ PageRequest(page_number - 1, page_size, createdAt DESC)
→ tìm order của user deleted=false → map OrderSummaryResponse
→ ApiResponse<Page<OrderSummaryResponse>>
```

### `GET /api/v1/orders/filter?status=...&page_size=10&page_number=1`

```text
USER + status bắt buộc + page query mặc định 10/1
→ tìm user chưa xóa
→ Specification order deleted=false AND user hiện tại AND status
→ PageRequest(createdAt DESC) → map OrderSummaryResponse
→ ApiResponse<Page<OrderSummaryResponse>>
```

### `GET /api/v1/orders/{orderId}`

```text
USER + orderId + principal username
→ tìm order ID + user username + deleted=false
→ không có/không thuộc user: ORDER_NOT_FOUND
→ tải order items, map OrderResponse và gắn items
→ ApiResponse<OrderResponse>
```

### `PUT /api/v1/orders/{orderId}/cancel`

```text
USER + orderId → tìm order theo user và deleted=false (không có: ORDER_NOT_FOUND)
→ chỉ PENDING/CONFIRMED được hủy; trạng thái khác: ORDER_CANNOT_BE_CANCELLED
→ set CANCELLED, cộng lại số lượng variant cho mỗi order item
→ lưu TrackingLog(CANCELLED)
→ ApiResponse<Void>(message="Huy don hang thanh cong")
```

Không hoàn usedCount coupon và không đổi paymentStatus.

## Order quản trị (ADMIN)

### `GET /api/v1/admin/orders?page_size=10&page_number=1`

```text
ADMIN + page query mặc định 10/1 → PageRequest(createdAt DESC)
→ findAllByDeletedFalse → map AdminOrderResponse
→ ApiResponse<Page<AdminOrderResponse>>
```

### `GET /api/v1/admin/orders/filter?status=...&page_size=10&page_number=1`

```text
ADMIN + status bắt buộc + page query mặc định 10/1
→ Specification deleted=false AND status; fetch user
→ PageRequest(createdAt DESC) → map AdminOrderResponse
→ ApiResponse<Page<AdminOrderResponse>>
```

### `GET /api/v1/admin/orders/{orderId}`

```text
ADMIN + orderId → tìm order deleted=false (không có: ORDER_NOT_FOUND)
→ tải items, map OrderResponse + items
→ ApiResponse<OrderResponse>
```

### `PATCH /api/v1/admin/orders/{orderId}/status?status=...`

```text
ADMIN + orderId + trạng thái đích
→ tìm order deleted=false (không có: ORDER_NOT_FOUND)
→ chỉ PENDING→CONFIRMED hoặc CONFIRMED→PICKING và phải hợp lệ theo transition
→ transition sai: INVALID_ORDER_STATUS_TRANSITION
→ khi CONFIRMED: tìm và gán shipper theo province (không có: SHIPPER_NOT_FOUND)
→ cập nhật order, ghi TrackingLog
→ map OrderResponse (response luồng này không gắn items)
```

## Order shipper (SHIPPER)

### `GET /api/v1/shipper/orders/?page_size=10&page_number=1`

```text
SHIPPER + page query mặc định 10/1 + principal username
→ tìm user shipper chưa xóa (không có: USER_NOT_FOUND)
→ PageRequest(createdAt DESC), tìm order được gán cho shipper và chưa xóa
→ map ShipperOrderResponse → ApiResponse<Page<ShipperOrderResponse>>
```

### `PATCH /api/v1/shipper/orders/{orderId}/status?status=...`

```text
SHIPPER + orderId + trạng thái đích + principal username
→ tìm order deleted=false (không có: ORDER_NOT_FOUND)
→ kiểm tra shipper được gán đúng principal; sai: UNAUTHORIZED
→ chỉ PICKING→SHIPPING hoặc SHIPPING→DELIVERED/FAILED; sai: INVALID_ORDER_STATUS_TRANSITION
→ cập nhật status, tạo notification cho SHIPPING/DELIVERED, ghi TrackingLog
→ map ShipperOrderResponse → ApiResponse<ShipperOrderResponse>
```

## Coupon quản trị (ADMIN)

### `POST /api/v1/admin/coupons`

```text
JSON CouponCreateRequest → Bean Validation
→ endDate phải sau startDate; percentage ≤100; min/max order value không âm; usageLimit null hoặc ≥1
→ trim/uppercase code; kiểm tra code trùng → COUPON_ALREADY_EXISTS
→ map Coupon, set deleted=false, save
→ map CouponResponse → ApiResponse<CouponResponse>
```

### `GET /api/v1/admin/coupons?page_size=...&page_number=...`

```text
ADMIN + hai query bắt buộc → PageRequest(page_number - 1, page_size)
→ findAllByDeletedFalse → map CouponResponse
→ ApiResponse<Page<CouponResponse>>
```

### `GET /api/v1/admin/coupons/{couponId}`

```text
couponId → findByIdAndDeletedFalse
→ không có: COUPON_NOT_FOUND
→ có: map CouponResponse → ApiResponse<CouponResponse>
```

### `PUT /api/v1/admin/coupons/{couponId}`

```text
JSON CouponUpdateRequest dạng partial → tìm coupon chưa xóa (không có: COUPON_NOT_FOUND)
→ min/max order value không âm, usageLimit null hoặc ≥1
→ validate ngày và discount trên các giá trị cuối sau update
→ MapStruct update trường khác null; DTO không có code nên không đổi code
→ map CouponResponse → ApiResponse<CouponResponse>
```

### `DELETE /api/v1/admin/coupons/{couponId}`

```text
couponId → tìm coupon chưa xóa (không có: COUPON_NOT_FOUND)
→ set deleted=true
→ ApiResponse<Void>(message="Delete Coupon Successfully")
```

### `PATCH /api/v1/admin/coupons/{couponId}/status?status=...`

```text
couponId + CouponStatus → tìm coupon chưa xóa (không có: COUPON_NOT_FOUND)
→ status null: INVALID_COUPON_VALUES; ngược lại gán status
→ map CouponResponse → ApiResponse<CouponResponse>
```

### `GET /api/v1/admin/coupons/filter?code=&status=&discount_type=&start_date_from=&start_date_to=&end_date_from=&end_date_to=&page=1&size=10`

```text
filter tùy chọn (date-time ISO) → Specification deleted=false
→ code LIKE, status/discountType bằng; các mốc from/to áp dụng >= hoặc <= tương ứng
→ PageRequest(page - 1, size, createdAt DESC)
→ map CouponResponse → ApiResponse<Page<CouponResponse>>
```

## Ghi chú tồn tại trong code

- `/api/v1/auth/me` đang permitAll nhưng cần Principal; `/api/v1/auth/login` trả Base64 credentials chứ không phải JWT.
- Tạo variant có SKU trùng hiện trả nhầm mã `PRODUCT_VARIANT_NAME_ALREADY_EXISTS`.
- Update cart item thiếu validation quantity null/0/âm; tạo order chưa chặn cart rỗng.
- Một số ErrorCode inventory legacy không có entity/API inventory tương ứng.
- Phân trang được truyền thẳng vào Spring Data, không có bước validate page/size riêng.
