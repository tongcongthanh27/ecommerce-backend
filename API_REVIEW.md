# Rà soát API backend — luồng xử lý từng API

Tài liệu này mô tả route trong controller và logic service đang chạy. Mỗi API được viết theo dạng đầu vào → truy vấn/kiểm tra → thay đổi dữ liệu → response; các nhánh lỗi thể hiện theo code hiện tại.

## Quyền truy cập chung

- Spring Security dùng HTTP Basic; CSRF và CORS bị disable.
- Swagger và `/api/v1/auth/**` được mở; `/api/v1/admin/**` yêu cầu ADMIN; `/api/v1/shipper/**` yêu cầu SHIPPER.
- GET `/api/v1/categories/**` và `/api/v1/products/**` công khai; các request khác cần xác thực. Cart/order kiểm tra thêm USER hoặc ADMIN bằng `@PreAuthorize`.
- Chưa thấy controller cho `/api/v1/auth/**` hoặc `/api/v1/shipper/**`. `/test` là endpoint hiện có để đọc thông tin principal.

## Tài khoản và xác thực

### `GET /test` — thông tin tài khoản hiện tại

```text
HTTP Basic credentials
        ↓
AccountService.loadUserByUsername(username)
        ↓
AccountRepository.findByUsername(username)
        ├─ Không tìm thấy → USERNOTFOUND
        └─ Tìm thấy → dựng UserDetails(username, password, role)
                         ↓
Controller lấy username từ Principal
        ↓
AccountService.getUserByUsername(username)
        ↓
AuthMapper chuyển User thành LoginResponse
        ↓
ApiResponse(code=1000, message="Success", result=LoginResponse)
```

### Quản trị user — `/api/v1/admin/users` (ADMIN)

#### `POST /api/v1/admin/users` — tạo user

```text
multipart/form-data + validation
        ↓
Kiểm tra username trùng user chưa xóa?
        ├─ Có → USERNAME_ALREADY_EXIST
        └─ Không → kiểm tra email trùng?
                      ├─ Có → EMAIL_ALREADY_EXIST
                      └─ Không → kiểm tra phone trùng?
                                   ├─ Có → PHONE_ALREADY_EXIST
                                   └─ Không
                                        ↓
                              UserMapper.toUser(request)
                                        ↓
                              BCrypt mã hóa password
                                        ↓
                              Có avatar? → upload Cloudinary → gán URL/public ID
                                        ↓
                              userRepository.save(user)
                                        ↓
                              map UserResponse → ApiResponse
```

#### `GET /api/v1/admin/users?page_size=&page_number=` — danh sách user

```text
page_size + page_number (bắt buộc)
        ↓
PageRequest(page_number - 1, page_size)
        ↓
findAllByDeletedFalse(pageable)
        ↓
Map User → UserResponse
        ↓
ApiResponse<Page<UserResponse>>
```

#### `GET /api/v1/admin/users/{userId}` — chi tiết user

```text
userId
        ↓
findByIdAndDeletedFalse(userId)
        ├─ Không có → USERNOTFOUND
        └─ Có → map UserResponse → ApiResponse
```

#### `PUT /api/v1/admin/users/{userId}` — cập nhật user

```text
userId + multipart/form-data
        ↓
Tìm user chưa xóa → không có: USERNOTFOUND
        ↓
username khác null? → kiểm tra trùng user khác → gán username
        ↓
email khác null? → kiểm tra trùng user khác → gán email
        ↓
fullName/address khác null? → cập nhật trường tương ứng
        ↓
phone khác null? → kiểm tra trùng user khác → gán phone
        ↓
Avatar mới? → upload Cloudinary → cập nhật URL/public ID → xóa ảnh cũ nếu có
        ↓
Map UserResponse → ApiResponse
```

#### `DELETE /api/v1/admin/users/{userId}` — xóa user

```text
userId → tìm user chưa xóa → không có: USERNOTFOUND
        ↓
đặt deleted=true (soft delete)
        ↓
Controller trả message thành công
```

#### `PATCH /api/v1/admin/users/{userId}/role?role=` — đổi role

```text
userId + role → tìm user chưa xóa → không có: USERNOTFOUND
        ↓
gán role → map UserResponse → ApiResponse
```

#### `GET /api/v1/admin/users/filter?username=&email=&full_name=&role=&page=&size=` — lọc user

```text
Filter tùy chọn + page/size (mặc định 1/10)
        ↓
Specification ban đầu không điều kiện
        ↓
username/email/full_name có giá trị → thêm LIKE "%giá trị%"
        ↓
role có giá trị → thêm role = giá trị
        ↓
PageRequest(page - 1, size, createdAt DESC)
        ↓
findAll(specification, pageable) → map UserResponse
```

Lưu ý: service không thêm `deleted=false` vào filter này, nên có thể trả user đã soft-delete.

## Danh mục — `/api/v1/categories`

#### `POST /api/v1/admin/categories` — tạo danh mục (ADMIN)

```text
multipart/form-data + validation
        ↓
Kiểm tra tên trùng category chưa xóa? → Có: CATEGORY_NAME_EXISTED
        ↓ Không
parentId được gửi? → tìm parent chưa xóa → không có: CATEGORY_NOT_FOUND
        ↓
Map request → Category; gán parent
        ↓
Có ảnh? → upload Cloudinary → gán imageUrl/publicId
        ↓
save Category → map CategoryResponse → ApiResponse
```

#### `GET /api/v1/categories?page_size=10&page_number=1` — danh sách danh mục

```text
page_size/page_number (mặc định 10/1)
        ↓
PageRequest(page_number - 1, page_size)
        ↓
findAllByDeletedFalse(pageable) → map CategoryResponse
        ↓
ApiResponse<Page<CategoryResponse>>
```

#### `GET /api/v1/categories/{categoryId}` — chi tiết danh mục

```text
categoryId → tìm category deleted=false
        ├─ Không có → CATEGORY_NOT_FOUND
        └─ Có → map CategoryResponse → ApiResponse
```

#### `GET /api/v1/categories/tree` — cây danh mục

```text
Tải tất cả category deleted=false
        ↓
Map CategoryResponse theo category ID
        ↓
Duyệt lại danh sách
        ├─ Không parent → thêm node vào roots
        └─ Có parent trong map → thêm node vào parent.children
        ↓
Trả danh sách roots
```

#### `PUT /api/v1/admin/categories/{categoryId}` — cập nhật danh mục (ADMIN)

```text
categoryId + multipart/form-data → tìm category chưa xóa
        ├─ Không có → CATEGORY_NOT_FOUND
        └─ Có
             ↓
name khác null/rỗng? → cập nhật name
             ↓
parentId null? → giữ parent hiện tại
parentId rỗng? → đặt parent = null
parentId có giá trị? → cấm parentId = categoryId → tìm parent chưa xóa → gán parent
             ↓
Ảnh mới? → upload → cập nhật URL/public ID → xóa ảnh cũ
             ↓
Map CategoryResponse
```

Lưu ý: xử lý parent bị lặp trong service; không kiểm tra tên trùng khi cập nhật hoặc chu trình parent nhiều cấp.

#### `DELETE /api/v1/admin/categories/{categoryId}` — xóa danh mục (ADMIN)

```text
categoryId → tìm category chưa xóa → không có: CATEGORY_NOT_FOUND
        ↓
đặt deleted=true → controller trả message thành công
```

Không thấy cascade xóa category con hoặc product.

#### `GET /api/v1/categories/filter?name=&parent_id=&page=1&size=10` — lọc danh mục

```text
Filter tùy chọn + page/size (mặc định 1/10)
        ↓
Specification deleted=false
        ↓
name có? → AND name LIKE "%name%"
parent_id có? → AND parent.id = parent_id
        ↓
PageRequest(page - 1, size, createdAt DESC)
        ↓
findAll → map CategoryResponse → ApiResponse<Page<CategoryResponse>>
```

## Sản phẩm — `/api/v1/products`

#### `POST /api/v1/admin/products` — tạo sản phẩm (ADMIN)

```text
multipart/form-data + validation
        ↓
Tìm category theo ID và deleted=false → không có: CATEGORY_NOT_FOUND
        ↓
Tên trùng product chưa xóa? → Có: PRODUCT_EXISTED
        ↓ Không
Map request → Product; gán category
        ↓
Có ảnh? → upload Cloudinary → gán thumbnail URL/public ID
        ↓
save Product → map ProductResponse
```

#### `DELETE /api/v1/admin/products/{productId}` — xóa sản phẩm (ADMIN)

```text
productId → tìm product chưa xóa → không có: PRODUCT_NOT_FOUND
        ↓
đặt deleted=true (soft delete) → controller trả message thành công
```

#### `PUT /api/v1/admin/products/{productId}` — cập nhật sản phẩm (ADMIN)

```text
productId + multipart/form-data → tìm product chưa xóa
        ├─ Không có → PRODUCT_NOT_FOUND
        └─ Có
             ↓
categoryId có giá trị? → tìm category chưa xóa → không có: CATEGORY_NOT_FOUND → gán
             ↓
name có giá trị? → kiểm tra trùng product khác → có: PRODUCT_EXISTED → gán
             ↓
description khác null? → cập nhật description
             ↓
Ảnh mới? → xóa ảnh Cloudinary cũ nếu có → upload ảnh mới → cập nhật URL/public ID
             ↓
Map ProductResponse
```

#### `GET /api/v1/products?page_size=10&page_number=1` — danh sách sản phẩm

```text
page_size/page_number (mặc định 10/1)
        ↓
PageRequest(page_number - 1, page_size)
        ↓
findAllByDeletedFalse → map ProductResponse (category ID/name)
        ↓
ApiResponse<Page<ProductResponse>>
```

#### `GET /api/v1/products/{productId}` — chi tiết sản phẩm

```text
productId → tìm product deleted=false → không có: PRODUCT_NOT_FOUND
        ↓
Tìm variants deleted=false thuộc product
        ↓
Map Product → ProductDetailResponse
        ↓
Map variants → ProductVariantResponse → gắn vào response
```

#### `GET /api/v1/products/filter?name=&category_id=&page=1&size=10` — lọc sản phẩm

```text
Filter tùy chọn + page/size (mặc định 1/10)
        ↓
Specification product deleted=false
        ↓
name có? → AND name LIKE "%name%"
category_id có? → lấy category hiện tại và hậu duệ bằng đệ quy
        ↓
AND product.category.id thuộc danh sách category IDs
        ↓
PageRequest(page - 1, size, createdAt DESC) → map ProductResponse
```

#### `GET /api/v1/products/category/{categoryId}?page_size=10&page_number=1` — sản phẩm theo danh mục

```text
categoryId + page_size/page_number (mặc định 10/1)
        ↓
Tìm category deleted=false → không có: CATEGORY_NOT_FOUND
        ↓
Lấy category ID hiện tại + các hậu duệ
        ↓
PageRequest(page_number - 1, page_size)
        ↓
Tìm product deleted=false thuộc các category IDs → map ProductResponse
```

## Biến thể sản phẩm — `/api/v1/admin` (ADMIN)

#### `POST /api/v1/admin/products/{productId}/variants` — tạo variant

```text
productId + multipart/form-data + validation
        ↓
Tìm product cha chưa xóa → không có: PRODUCT_NOT_FOUND
        ↓
variantName trùng variant chưa xóa? → Có: PRODUCT_VARIANT_EXISTED
SKU trùng variant chưa xóa? → Có: PRODUCT_VARIANT_EXISTED
        ↓
Map request → ProductVariant; upload ảnh nếu có; gán product
        ↓
save variant → map response → quantity > 0: IN_STOCK, ngược lại OUT_OF_STOCK
```

#### `PUT /api/v1/admin/products/variants/{variantId}` — cập nhật variant

```text
variantId + multipart/form-data → tìm variant chưa xóa
        ├─ Không có → VARIANTNOTFOUND
        └─ Có
             ↓
variantName gửi lên và trùng variant khác? → Có: PRODUCT_VARIANT_EXISTED
sku gửi lên và trùng variant khác? → Có: SKU_ALREADY_EXISTS
             ↓
ProductVariantMapper.updateVariant(request, variant)
             ↓
Ảnh mới? → upload Cloudinary → cập nhật URL/public ID
             ↓
save → map response → tính stockStatus theo quantity
```

#### `DELETE /api/v1/admin/products/variants/{variantId}` — xóa variant

```text
variantId → tìm variant deleted=false → không có: VARIANTNOTFOUND
        ↓
đặt deleted=true → controller trả message thành công
```

#### `GET /api/v1/admin/product-variants?page_size=&page_number=` — danh sách variant

```text
page_size + page_number (bắt buộc)
        ↓
PageRequest(page_number - 1, page_size)
        ↓
findAllByDeletedFalse → map ProductVariantResponse
```

#### `GET /api/v1/admin/product-variants/filter?variant_name=&sku=&product_id=&min_price=&max_price=&page=1&size=10` — lọc variant

```text
Filter tùy chọn + page/size (mặc định 1/10)
        ↓
Specification deleted=false
        ↓
variant_name/SKU? → AND LIKE "%giá trị%"
product_id? → AND product.id = product_id
min_price? → AND price >= min_price
max_price? → AND price <= max_price
        ↓
PageRequest(page - 1, size, createdAt DESC) → map ProductVariantResponse
```

## Giỏ hàng — `/api/v1/carts/items` (USER)

#### `POST /api/v1/carts/items` — thêm vào giỏ

```text
username + productVariantId + quantity (validation quantity >= 1)
        ↓
Tìm User theo username → không có: USERNOTFOUND
        ↓
Tìm Cart của user
        ├─ Chưa có → tạo Cart mới, gắn User, save Cart
        └─ Đã có → dùng Cart hiện tại
             ↓
Tìm ProductVariant deleted=false → không có: VARIANTNOTFOUND
        ↓
Tìm CartItem deleted=false theo cartId + variantId
        ├─ Chưa có → kiểm tra quantity <= quantityInStock
        │              ├─ Không đạt → INSUFFICIENT_STOCK
        │              └─ Đạt → tạo CartItem, gán cart/variant/quantity, save
        └─ Đã có → newQuantity = quantity hiện tại + quantity yêu cầu
                       ↓
                 kiểm tra newQuantity <= quantityInStock
                       ├─ Không đạt → INSUFFICIENT_STOCK
                       └─ Đạt → cập nhật quantity, save
        ↓
Map CartItemResponse
```

#### `GET /api/v1/carts/items` — xem giỏ

```text
username từ Principal → tìm Cart
        ├─ Không có → CartResponse(items=[], totalItems=0, subtotal=0)
        └─ Có → tải CartItems cùng ProductVariant
                    ↓
              Với từng item: totalItems += quantity
                    ↓
              subtotal += variant.price × quantity
                    ↓
              tồn variant > 0? → IN_STOCK : OUT_OF_STOCK
                    ↓
              map từng CartItemResponse và stockStatus
                    ↓
              CartMapper tạo CartResponse
```

#### `DELETE /api/v1/carts/items/{cartItemId}` — xóa item khỏi giỏ

```text
cartItemId + username
        ↓
Tìm Cart của user → không có: CARTNOTFOUND
        ↓
Tìm CartItem theo id + cartId + deleted=false → không có: CART_ITEM_NOT_FOUND
        ↓
đặt CartItem.deleted=true (soft delete)
        ↓
Controller trả message "Xoa san pham thanh cong"
```

#### `PUT /api/v1/carts/items/{cartItemId}` — cập nhật số lượng

```text
cartItemId + username + request.quantity
        ↓
Tìm Cart của user → không có: CARTNOTFOUND
        ↓
Tìm CartItem theo id + cartId + deleted=false → không có: CART_ITEM_NOT_FOUND
        ↓
Lấy ProductVariant từ CartItem
        ↓
quantity > quantityInStock?
        ├─ Có → INSUFFICIENT_STOCK
        └─ Không → CartItem.quantity = quantity
                       ↓
                 map CartItemResponse
```

Lưu ý: DTO không có `@NotNull`/`@Min(1)`; service cũng không chặn rõ quantity null, 0 hoặc âm.

## Đơn hàng người dùng — `/api/v1/orders` (USER)

#### `POST /api/v1/orders` — tạo đơn hàng

```text
username + OrderCreateRequest
        ↓
Tìm User deleted=false → không có: USERNOTFOUND
        ↓
Tìm Cart theo username → không có: CARTNOTFOUND
        ↓
Tải CartItems cùng ProductVariants
        ↓
Duyệt từng CartItem
        ├─ quantity > tồn kho → INSUFFICIENTSTOCK
        └─ subtotal += variant.price × quantity
             ↓
Có couponCode?
        ├─ Không → coupon=null, discount=0
        └─ Có → tìm coupon: code đúng, deleted=false, ACTIVE,
                trong thời hạn, usage chưa đạt limit
                  ├─ Không hợp lệ → COUPONINVALID
                  └─ Hợp lệ → so subtotal với minOrderValue
                                  ├─ Chưa đạt → discount=0, coupon vẫn gắn
                                  └─ Đạt → tính discount theo loại
                                             ├─ PERCENTAGE → subtotal × % / 100
                                             └─ FIXED_AMOUNT → discountValue, không quá subtotal
                                  → áp maxDiscountAmount nếu có
             ↓
shippingFee = 500000
        ↓
grandTotal = subtotal + shippingFee - discount
        ↓
Map request → Order; gán recipient, user, tracking number,
status=PENDING, paymentStatus=UNPAID, coupon và các số tiền
        ↓
orderRepository.save(order)
        ↓
Coupon có? → tăng usedCount
        ↓
Từng CartItem → tạo OrderItem (unitPrice = giá hiện tại)
             → gán order → trừ tồn kho → CartItem.deleted=true
        ↓
saveAll(OrderItems)
        ↓
Tạo TrackingLog(PENDING, "Order created") → save
        ↓
Map Order + OrderItems → OrderResponse
```

Lưu ý: payment method được map từ request nhưng service không tạo Payment/thực hiện thanh toán; không có kiểm tra cart rỗng.

#### `GET /api/v1/orders?page_size=10&page_number=1` — danh sách đơn của tôi

```text
username + page_size/page_number (mặc định 10/1)
        ↓
Tìm User deleted=false → không có: USERNOTFOUND
        ↓
PageRequest(page_number - 1, page_size, createdAt DESC)
        ↓
Tìm Order theo userId + deleted=false
        ↓
Map Order → OrderSummaryResponse
        ↓
ApiResponse<Page<OrderSummaryResponse>>
```

#### `GET /api/v1/orders/filter?status=&page_size=10&page_number=1` — lọc đơn của tôi

```text
username + status (bắt buộc) + page_size/page_number
        ↓
Tìm User deleted=false → không có: USERNOTFOUND
        ↓
PageRequest(page_number - 1, page_size, createdAt DESC)
        ↓
Specification: deleted=false AND user.id=currentUser.id AND status=request.status
        ↓
findAll(specification, pageable) → map OrderSummaryResponse
```

#### `GET /api/v1/orders/{orderId}` — chi tiết đơn của tôi

```text
orderId + username
        ↓
Tìm Order theo id + user.username + deleted=false
        ├─ Không có/không thuộc user → ORDER_NOT_FOUND
        └─ Có → map OrderResponse
                    ↓
              Tìm OrderItems theo orderId
                    ↓
              Map dòng hàng (SKU, variant, unitPrice, quantity, subtotal)
                    ↓
              Gắn items → trả OrderResponse
```

#### `PUT /api/v1/orders/{orderId}/cancel` — hủy đơn

```text
orderId + username
        ↓
Tìm Order theo id + username + deleted=false → không có: ORDER_NOT_FOUND
        ↓
status là PENDING hoặc CONFIRMED?
        ├─ Không → ORDER_CANNOT_BE_CANCELLED
        └─ Có → đặt status=CANCELLED
                    ↓
              Tải OrderItems
                    ↓
              Mỗi item: variant.quantityInStock += item.quantity
                    ↓
              Tạo TrackingLog(CANCELLED, "Order cancelled by customer")
                    ↓
              save TrackingLog → controller trả message thành công
```

Lưu ý: coupon `usedCount` không hoàn lại; paymentStatus không đổi.

## Đơn hàng quản trị — `/api/v1/admin/orders` (ADMIN)

#### `GET /api/v1/admin/orders?page_size=10&page_number=1` — danh sách đơn

```text
page_size/page_number (mặc định 10/1)
        ↓
PageRequest(page_number - 1, page_size, createdAt DESC)
        ↓
Tìm Order deleted=false
        ↓
Map AdminOrderResponse (id→orderId, createdAt→createdDate,
grandTotal→amount, user.fullName→customerName)
```

#### `GET /api/v1/admin/orders/filter?status=&page_size=10&page_number=1` — lọc đơn theo trạng thái

```text
status + page_size/page_number
        ↓
PageRequest(page_number - 1, page_size, createdAt DESC)
        ↓
Specification: deleted=false AND status=request.status
        ↓
findAll(specification, pageable) → map AdminOrderResponse
```

#### `GET /api/v1/admin/orders/{orderId}` — chi tiết đơn quản trị

```text
orderId → tìm Order deleted=false → không có: ORDER_NOT_FOUND
        ↓
Map OrderResponse → tìm OrderItems theo orderId
        ↓
Map items → gắn vào response → trả về
```

#### `PATCH /api/v1/admin/orders/{orderId}/status?status=` — cập nhật trạng thái

```text
orderId + trạng thái đích
        ↓
Tìm Order deleted=false → không có: ORDER_NOT_FOUND
        ↓
currentStatus.canTransitionTo(newStatus)?
        ├─ Không → INVALID_ORDER_STATUS_TRANSITION
        └─ Có → cập nhật Order.status
                    ↓
              Tạo TrackingLog(newStatus, "Order updated by admin") → save
                    ↓
              Map OrderResponse (không gắn items)
```

Chuyển tiếp hợp lệ: PENDING→CONFIRMED→PICKING→SHIPPING; SHIPPING→DELIVERED hoặc FAILED; FAILED→RETURNING hoặc REATTEMPT. CANCELLED/REJECTED không được chuyển đến từ hàm này.

## Coupon quản trị — `/api/v1/admin/coupons` (ADMIN)

#### `POST /api/v1/admin/coupons` — tạo coupon

```text
CouponCreateRequest JSON + validation
        ↓
Code trùng coupon chưa xóa?
        ├─ Có → COUPON_ALREADY_EXISTS
        └─ Không → map request → Coupon
                    ↓
              đặt deleted=false → save
                    ↓
              map CouponResponse
```

#### `GET /api/v1/admin/coupons?page_size=&page_number=` — danh sách coupon

```text
page_size + page_number (bắt buộc)
        ↓
PageRequest(page_number - 1, page_size)
        ↓
findAllByDeletedFalse → map CouponResponse
```

#### `GET /api/v1/admin/coupons/{couponId}` — chi tiết coupon

```text
couponId → tìm coupon deleted=false
        ├─ Không có → COUPONNOTFOUND
        └─ Có → map CouponResponse
```

#### `PUT /api/v1/admin/coupons/{couponId}` — cập nhật coupon

```text
couponId + CouponUpdateRequest JSON
        ↓
Tìm coupon deleted=false → không có: COUPONNOTFOUND
        ↓
CouponMapper.updateCoupon(request, coupon)
        ↓
Map CouponResponse
```

Request update không có code nên không đổi coupon code.

#### `DELETE /api/v1/admin/coupons/{couponId}` — xóa coupon

```text
couponId → tìm coupon deleted=false → không có: COUPONNOTFOUND
        ↓
đặt deleted=true (soft delete) → controller trả message thành công
```

#### `PATCH /api/v1/admin/coupons/{couponId}/status?status=` — cập nhật trạng thái coupon

```text
couponId + CouponStatus
        ↓
Tìm coupon deleted=false → không có: COUPONNOTFOUND
        ↓
Gán coupon.status = status → map CouponResponse
```

#### `GET /api/v1/admin/coupons/filter?code=&status=&discount_type=&start_date_from=&start_date_to=&end_date_from=&end_date_to=&page=1&size=10` — lọc coupon

```text
Filter tùy chọn (date-time ISO) + page/size (mặc định 1/10)
        ↓
Specification deleted=false
        ↓
code? → AND code LIKE "%code%"
status? → AND status = status
discount_type? → AND discountType = discount_type
start_date_from/to? → AND startDate >= from, <= to
end_date_from/to? → AND endDate >= from, <= to
        ↓
PageRequest(page - 1, size, createdAt DESC)
        ↓
findAll(specification, pageable) → map CouponResponse
```

## Ghi chú chung

- Hầu hết thao tác delete là soft delete (`deleted=true`), không xóa vật lý.
- Ảnh được upload qua Cloudinary; implementation đang dùng folder `ecommerce/categories` cho mọi loại ảnh.
- Filter nhận page bắt đầu từ 1 và service đổi về 0-based; page/size không hợp lệ chưa được chuẩn hóa thành lỗi riêng.
- `AppException` được `GlobalExceptionHandler` chuyển thành HTTP status, code và message. Chưa thấy handler tổng quát cho mọi lỗi khác.
- Nội dung phản ánh code trong controller/service hiện tại; route chưa có controller không có flow tương ứng.
