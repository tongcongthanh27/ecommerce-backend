# API backend — luồng xử lý theo từng endpoint

Tài liệu này mô tả luồng theo controller, DTO, service, repository và mapper hiện tại. Với mỗi endpoint, luồng đi theo thứ tự request → validation/quyền → truy vấn/kiểm tra → cập nhật dữ liệu → response. `page` và `page_number` bắt đầu từ 1 ở API, service trừ 1 khi tạo `PageRequest`; không có kiểm tra page/size riêng. Các tham số page/size bắt buộc hay mặc định theo đúng khai báo ở controller.

## Quy ước chung

- Xác thực dùng HTTP Basic; CSRF/CORS bị disable. Swagger và `/api/v1/auth/**` permitAll; `/api/v1/admin/**` yêu cầu ADMIN; `/api/v1/shipper/**` yêu cầu SHIPPER; GET `/api/v1/categories/**` và `/api/v1/products/**` công khai. Cart và order khách yêu cầu USER qua `@PreAuthorize`.
- Thành công thường trả `ApiResponse<T>`. Nếu controller không đặt code/message thì code mặc định là `0`, message null; auth đặt code `1000`. Delete thường trả message và không có result.
- `AppException` trả HTTP status, code, message từ `ErrorCode`. Validation/bind/type mismatch/thiếu request parameter trả HTTP 400, code 400, message lỗi validation hoặc “Invalid request parameters”. Chưa có handler tổng quát cho mọi exception khác.
- Ghi dữ liệu delete thường là soft delete (`deleted=true`). Ảnh dùng Cloudinary.

## Auth

### `POST /api/v1/auth/register`

```text
JSON RegisterRequest
→ Bean Validation (username/email/password/fullName/province/city/addressDetail bắt buộc; phone có thể null)
→ AccountService kiểm tra username và email trùng
→ Tạo User, BCrypt password, gán role USER và deleted=false
→ Lưu User
→ map LoginResponse
→ ApiResponse(code=1000, message đăng ký thành công, result=LoginResponse)
```

Trùng username/email trả `USERNAME_ALREADY_EXISTS`/`EMAIL_ALREADY_EXISTS`.

### `POST /api/v1/auth/login`

```text
JSON LoginRequest(username, password)
→ AuthenticationManager xác thực credentials
→ Lấy User theo authentication name
→ map LoginResponse
→ gán token là Base64(username:password)
→ ApiResponse(code=1000, message đăng nhập thành công, result=LoginResponse)
```

`token` này không phải JWT; cấu hình xác thực thực tế vẫn là HTTP Basic. Sai credentials do Spring Security xử lý.

### `GET /api/v1/auth/me`

```text
HTTP request → lấy Principal → tra User theo username → map LoginResponse
→ ApiResponse(code=1000, message="Success", result=LoginResponse)
```

Security permitAll cho `/auth/**`, dù endpoint cần Principal. Gọi không xác thực có thể không có Principal và không được ánh xạ thành lỗi nghiệp vụ rõ ràng.

## User quản trị (`ADMIN`)

### `POST /api/v1/admin/users`

```text
multipart/form-data UserCreateRequest
→ ADMIN + Bean Validation
→ trim username/phone, lowercase email
→ kiểm tra username/email/phone đã dùng bởi user chưa xóa
→ map User, BCrypt password
→ avatar có file? upload Cloudinary và gán URL/public ID
→ save User
→ ApiResponse<UserResponse> (có id, không trả password)
```

Lỗi trùng lần lượt: `USERNAME_ALREADY_EXISTS`, `EMAIL_ALREADY_EXISTS`, `PHONE_NUMBER_ALREADY_EXISTS`.

### `GET /api/v1/admin/users?page_size=...&page_number=...`

```text
ADMIN + hai query bắt buộc
→ PageRequest(page_number - 1, page_size)
→ findAllByDeletedFalse
→ map từng User thành UserResponse
→ ApiResponse<Page<UserResponse>>
```

### `GET /api/v1/admin/users/{userId}`

```text
ADMIN + userId → findByIdAndDeletedFalse
→ không có: USER_NOT_FOUND
→ có: map UserResponse → ApiResponse<UserResponse>
```

### `PUT /api/v1/admin/users/{userId}`

```text
ADMIN + multipart UserUpdateRequest
→ Bean Validation → tìm user chưa xóa (không có: USER_NOT_FOUND)
→ từng trường khác null được cập nhật; chuỗi rỗng ở trường profile bị từ chối
→ username/email/phone kiểm tra trùng trên user khác; chuẩn hóa username/email/phone
→ avatar mới? upload Cloudinary, cập nhật URL/public ID rồi xóa ảnh cũ
→ map UserResponse → ApiResponse<UserResponse>
```

### `DELETE /api/v1/admin/users/{userId}`

```text
ADMIN + userId → tìm user chưa xóa (không có: USER_NOT_FOUND)
→ set deleted=true
→ ApiResponse<Void>(message="Delete User Successfully")
```

### `PATCH /api/v1/admin/users/{userId}/role?role=...`

```text
ADMIN + userId + UserRole
→ tìm user chưa xóa (không có: USER_NOT_FOUND)
→ gán role
→ map UserResponse → ApiResponse<UserResponse>
```

### `GET /api/v1/admin/users/filter?username=&email=&full_name=&role=&page=1&size=10`

```text
ADMIN + các filter tùy chọn
→ Specification bắt đầu bằng deleted=false
→ username/email/full_name có giá trị: LIKE %value%; role có giá trị: bằng chính xác
→ PageRequest(page - 1, size, createdAt DESC)
→ findAll(specification) → map UserResponse
→ ApiResponse<Page<UserResponse>>
```

## Category

### `POST /api/v1/admin/categories` (ADMIN)

```text
multipart CategoryCreateRequest → ADMIN + Bean Validation
→ tên đã tồn tại trong category chưa xóa? có: CATEGORY_NAME_ALREADY_EXISTS
→ parentId có? tìm parent chưa xóa; không có: CATEGORY_NOT_FOUND
→ map Category, gán parent
→ ảnh có? upload Cloudinary và gán URL/public ID
→ save → map CategoryResponse → ApiResponse<CategoryResponse>
```

### `GET /api/v1/categories?page_size=10&page_number=1` (public)

```text
query phân trang (mặc định 10/1)
→ PageRequest(page_number - 1, page_size)
→ findAllByDeletedFalse → map CategoryResponse
→ ApiResponse<Page<CategoryResponse>>
```

### `GET /api/v1/categories/{categoryId}` (public)

```text
categoryId → findByIdAndDeletedFalse
→ không có: CATEGORY_NOT_FOUND
→ có: map CategoryResponse (children không được dựng ở response thường)
→ ApiResponse<CategoryResponse>
```

### `GET /api/v1/categories/tree` (public)

```text
findAllByDeletedFalse
→ map từng category thành CategoryResponse theo ID
→ category không parent thành root; category có parent nằm trong tập kết quả được gắn vào children của parent
→ trả List<CategoryResponse> trong ApiResponse
```

### `PUT /api/v1/admin/categories/{categoryId}` (ADMIN)

```text
multipart CategoryUpdateRequest → tìm category chưa xóa (không có: CATEGORY_NOT_FOUND)
→ name khác null/rỗng: trim, kiểm tra trùng category khác, cập nhật
→ description khác null: cập nhật (chuỗi rỗng xóa mô tả)
→ parentId null: giữ; rỗng: bỏ parent; có giá trị: tìm parent chưa xóa và ngăn tự tham chiếu/chu trình
→ ảnh mới: upload xong mới xóa ảnh cũ
→ map CategoryResponse → ApiResponse<CategoryResponse>
```

### `DELETE /api/v1/admin/categories/{categoryId}` (ADMIN)

```text
categoryId → tìm category chưa xóa (không có: CATEGORY_NOT_FOUND)
→ set deleted=true, không cascade xuống product/category con
→ ApiResponse<Void>(message="Delete Category Successfully")
```

### `GET /api/v1/categories/filter?name=&parent_id=&page=1&size=10` (public)

```text
filter tùy chọn → Specification deleted=false
→ name có: LIKE %name%; parent_id có: parent.id bằng parent_id
→ PageRequest(page - 1, size, createdAt DESC)
→ map CategoryResponse → ApiResponse<Page<CategoryResponse>>
```

## Product

### `POST /api/v1/admin/products` (ADMIN)

```text
multipart ProductCreateRequest → ADMIN + Bean Validation
→ tìm category theo ID và deleted=false (không có: CATEGORY_NOT_FOUND)
→ trim tên; tên trùng product chưa xóa: PRODUCT_NAME_ALREADY_EXISTS
→ map Product, gán category
→ ảnh có? upload Cloudinary và gán thumbnail URL/public ID
→ save → map ProductResponse → ApiResponse<ProductResponse>
```

### `PUT /api/v1/admin/products/{productId}` (ADMIN)

```text
multipart ProductUpdateRequest dạng partial
→ tìm product chưa xóa (không có: PRODUCT_NOT_FOUND)
→ categoryId khác null/rỗng: tìm category chưa xóa (không có: CATEGORY_NOT_FOUND), gán
→ name khác null/rỗng: trim, kiểm tra trùng product khác, cập nhật
→ description khác null: cập nhật
→ ảnh mới: upload trước, sau đó xóa ảnh cũ và cập nhật URL/public ID
→ map ProductResponse → ApiResponse<ProductResponse>
```

### `DELETE /api/v1/admin/products/{productId}` (ADMIN)

```text
productId → tìm product chưa xóa (không có: PRODUCT_NOT_FOUND)
→ set deleted=true
→ ApiResponse<Void>(message="Delete Product Successfully")
```

### `GET /api/v1/products?page_size=10&page_number=1` (public)

```text
query phân trang (mặc định 10/1) → PageRequest(page_number - 1, page_size)
→ findAllByDeletedFalse → map ProductResponse (categoryId/categoryName)
→ ApiResponse<Page<ProductResponse>>
```

### `GET /api/v1/products/{productId}` (public)

```text
productId → tìm product deleted=false (không có: PRODUCT_NOT_FOUND)
→ tải variant thuộc product và deleted=false
→ map ProductDetailResponse + danh sách ProductVariantResponse
→ ApiResponse<ProductDetailResponse>
```

### `GET /api/v1/products/filter?name=&category_id=&page=1&size=10` (public)

```text
filter tùy chọn → Specification product deleted=false
→ name có: LIKE %name%
→ category_id có: lấy category đó và các hậu duệ, lọc product theo các category ID
→ PageRequest(page - 1, size, createdAt DESC) → map ProductResponse
→ ApiResponse<Page<ProductResponse>>
```

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
