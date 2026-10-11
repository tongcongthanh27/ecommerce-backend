# Phân tích project và nội dung CV Java Backend Fresher/Junior

> Phạm vi: rà soát mã nguồn trong `src/main/java`, cấu hình Maven/YAML, README, tài liệu `API_REVIEW.md` và test hiện có. Tài liệu mô tả những gì mã nguồn thể hiện; chưa xác nhận quyền sở hữu cá nhân, khả năng chạy với dịch vụ ngoài hoặc triển khai production.

## 1. Tổng quan project

### Tên gợi ý
**E-Commerce Order Management Backend**

### Mục đích và kiến trúc
Backend REST API cho cửa hàng trực tuyến, có luồng quản trị danh mục/sản phẩm/biến thể, khách mua hàng và shipper xử lý đơn. Mã tổ chức theo Controller → Service → Repository, dùng DTO/MapStruct để trao đổi dữ liệu, Spring Data JPA để lưu quan hệ vào MySQL.

### Công nghệ có bằng chứng sử dụng
- Java 17; Spring Boot (parent khai báo 4.1.1), Spring Web MVC.
- Spring Data JPA/Hibernate, MySQL Connector/J.
- Spring Security, HTTP Basic, BCrypt password encoder, kiểm tra vai trò trong controller.
- Jakarta Validation, Spring Data `Pageable`/`Specification`.
- MapStruct, Lombok.
- Cloudinary SDK cho upload/xóa ảnh; Spring Mail/`JavaMailSender` cho email.
- SpringDoc OpenAPI/Swagger UI.
- Maven Wrapper.

Các công nghệ trên có mặt trong `pom.xml` và phần lớn được gọi trực tiếp trong code; riêng cấu hình kết nối dịch vụ cần sửa/đưa secret vào môi trường đúng cách trước khi xác nhận chạy thực tế. Git chỉ được xác nhận nếu bạn đã quản lý repository bằng Git; trong danh sách file không có thông tin đủ để chứng minh workflow.

## 2. Tính năng và mức độ hoàn thiện

### Đã triển khai trong mã nguồn

- **Catalog:** CRUD danh mục dạng cây, tạo/cập nhật/xóa mềm; CRUD sản phẩm và biến thể; trang chi tiết sản phẩm kèm biến thể. Luồng controller → service → repository ở `src/main/java/org/example/ecommerc_shop/controller/CategoryController.java`, `ProductController.java`, `ProductVariantController.java` và các service `CategoryServiceImpl.java`, `ProductServiceImpl.java`, `ProductVariantServiceImpl.java`. Entity/quan hệ ở `entity/Category.java`, `Product.java`, `ProductVariant.java`.
- **Tìm kiếm/lọc:** lọc danh mục, sản phẩm, biến thể, coupon, user và đơn hàng bằng các `Specification` kết hợp `PageRequest`; lọc sản phẩm theo danh mục hiện tại và hậu duệ. Bằng chứng: `service/specification/`, cùng `ProductServiceImpl.java`, `CategoryServiceImpl.java`, `ProductVariantServiceImpl.java`, `CouponServiceImpl.java`, `UserServiceImpl.java`, `OrderServiceImpl.java`.
- **Tài khoản:** đăng ký kiểm tra username/email trùng và mã hóa mật khẩu BCrypt; endpoint login xác thực qua `AuthenticationManager`; endpoint `/me`. Bằng chứng: `controller/AuthenticationController.java`, `service/impl/AccountServiceImpl.java`, `config/WebSecurityConfig.java`.
- **Vai trò:** ADMIN quản lý catalog/coupon/user/order; USER dùng cart và order; SHIPPER xem đơn được giao và cập nhật tiến trình. Có giới hạn role ở `config/WebSecurityConfig.java` và `@PreAuthorize` tại các controller.
- **Cart và đặt hàng:** thêm/sửa/xóa item, kiểm tra tồn khi thêm, tạo đơn trong transaction, tính subtotal/discount/shipping/grand total, áp coupon, giảm tồn kho, lưu order items và tracking log; hủy đơn được phép ở trạng thái PENDING/CONFIRMED và hoàn kho. Bằng chứng: `controller/CartController.java`, `controller/OrderController.java`, `service/impl/CartServiceImpl.java`, `service/impl/OrderServiceImpl.java`, `repository/CartItemRepository.java`, `repository/CouponRepository.java`.
- **Luồng giao hàng:** giới hạn chuyển trạng thái của admin và shipper; khi admin xác nhận đơn sẽ tìm shipper theo tỉnh; shipper chỉ sửa đơn gán cho mình. Ghi `TrackingLog`; phát sinh thông báo email khi SHIPPING/DELIVERED. Bằng chứng: `common/OrderStatus.java`, `service/impl/OrderServiceImpl.java`, `entity/TrackingLog.java`, `entity/Notification.java`.
- **Coupon:** CRUD quản trị, lọc theo code/status/loại/ngày; truy vấn coupon hợp lệ kiểm tra ACTIVE, thời hạn và usage limit; tính phần trăm/số tiền cố định và mức giảm tối đa. Bằng chứng: `CouponController.java`, `CouponServiceImpl.java`, `repository/CouponRepository.java`, phần `createOrder` trong `OrderServiceImpl.java`.
- **Tích hợp ảnh và email:** upload/xóa ảnh qua Cloudinary; scheduler mỗi 60 giây lấy tối đa 50 notification PENDING để gửi email và cập nhật SENT/FAILED. Bằng chứng: `service/impl/CloudinaryServiceImpl.java`, `config/CloudinaryConfig.java`, `service/impl/EmailServiceImpl.java`, `scheduler/NotificationScheduler.java`, `EcommercShopApplication.java`.
- **Validation, lỗi và auditing:** DTO có constraint validation và controller dùng `@Valid`/`@Validated`; `AppException` được chuyển thành response chuẩn; BaseEntity có created/updated/deleted và auditing. Bằng chứng: `dto/request/`, `exception/GlobalExceptionHandler.java`, `exception/ErrorCode.java`, `entity/BaseEntity.java`, `config/JpaAuditingConfig.java`.
- **Phân trang/truy vấn quan hệ:** repository có `JOIN FETCH` cho order items/cart items và `@EntityGraph` cho một số trang order; đây là kỹ thuật kiểm soát fetch có trong code, chưa có bằng chứng benchmark hoặc xử lý triệt để N+1 toàn ứng dụng. Bằng chứng: `repository/OrderItemRepository.java`, `CartItemRepository.java`, `OrderRepository.java`.

### Có cấu trúc nhưng chưa triển khai nghiệp vụ đầy đủ / cần xác nhận

- `Payment` entity và các enum payment tồn tại, nhưng không có PaymentRepository/service/controller/gateway; tạo order chỉ lưu phương thức thanh toán và đặt `PaymentStatus.UNPAID`. Chưa ghi “tích hợp thanh toán”.
- `Review` entity có nhưng không thấy endpoint/service/repository cho đánh giá.
- `Notification` đang triển khai một phần cho email trạng thái giao hàng; không có API cho người dùng xem thông báo.
- Không tìm thấy Dockerfile, compose, pipeline CI/CD hoặc cấu hình deploy.
- Test duy nhất là `EcommercShopApplicationTests.contextLoads`; chưa có bằng chứng test nghiệp vụ/integration ngoài việc khai báo dependency test.

### Cấu hình và hạn chế đáng chú ý

- `application.yaml` chứa username/password MySQL dạng literal và Cloudinary placeholders có định dạng `${...}` trông như giá trị nhưng được Spring hiểu là tên biến; cần cấu hình lại bằng environment variables. Không nên đưa thông tin xác thực vào CV/tài liệu công khai. Mail dùng `MAIL_USERNAME`/`MAIL_PASSWORD`.
- `AuthenticationController.login` gán chuỗi Base64 `username:password` vào trường `token`; đây không phải JWT hay token phiên an toàn. Nên mô tả authentication là HTTP Basic, không phải JWT.
- `@PreAuthorize` được dùng nhưng không thấy `@EnableMethodSecurity`; cần xác nhận method security thực sự được kích hoạt ở runtime. URL `/api/v1/admin/**` vẫn có role rule trong security filter chain, còn kiểm soát USER/SHIPPER dựa vào method annotation.
- `UpdateCartItemRequest` không có constraint `@NotNull`/`@Min(1)`, và service không chặn quantity bằng 0/âm rõ ràng.
- Chưa thấy kiểm tra cart trống khi tạo order; cập nhật trạng thái coupon usage/cancel và xử lý thanh toán cần rà soát thêm trước khi giới thiệu như luồng hoàn chỉnh.
- `GlobalExceptionHandler` chỉ xử lý `AppException`, chưa thấy handler riêng cho validation hoặc lỗi tổng quát.
- Có `@CreatedBy`/`@LastModifiedBy` auditing nhưng AuditorAware trả username; tính đầy đủ phụ thuộc auditing activation/cấu hình runtime. Không khẳng định mọi thao tác lưu đều được kiểm thử.

## 3. Nội dung CV tiếng Anh theo form cung cấp

### E-Commerce Shop Backend | [Project dates]
**Role:** Backend Engineer *(confirm your role)*  
**GitHub:** [Current project repository](.)

**Key Contributions:**

- Developed layered REST APIs for product categories, products, product variants, coupons, user administration, shopping carts, and orders using Spring MVC, service classes, Spring Data JPA, and DTO mapping.
- Implemented paginated browsing and dynamic filtering for catalog and administrative data using Spring Data `Pageable` and JPA `Specification`.
- Secured administrative, customer, and shipper workflows with Spring Security HTTP Basic, BCrypt password encoding, and role-based access rules.
- Implemented transactional order creation with inventory checks, coupon discount calculation, order-item price snapshots, stock updates, and tracking history.
- Added order lifecycle rules and shipper workflows, including province-based shipper assignment, ownership checks, and stock restoration for eligible cancellations.
- Integrated Cloudinary for image upload/deletion and scheduled email notifications for shipping and delivery status changes.
- Modeled relational data with JPA entities and added targeted `JOIN FETCH`/`@EntityGraph` queries for cart and order reads.

**Technologies used:** Java 17, Spring Boot, Spring MVC, Spring Security, Spring Data JPA, Hibernate, MySQL, Jakarta Validation, MapStruct, Lombok, Maven, SpringDoc OpenAPI, Cloudinary, Spring Mail.

> Replace `[Project dates]` and confirm your role before using this entry. The repository does not establish when the project was completed or which parts you personally implemented.

## 4. Ghi chú xác minh trước khi chốt CV

Nội dung CV ở trên được tạo từ repository `ecommerc_shop_backend` hiện tại. Không đưa vào đây các chi tiết của hai project Fashion E-commerce/Music App trước đó, vì đó là project khác và không có trong source code đang phân tích. Các bullet mô tả chức năng có bằng chứng trong code; bạn vẫn cần xác nhận vai trò cá nhân, phần đóng góp, và ngày dự án.

## 5. Technical highlights để trao đổi phỏng vấn

1. **Tạo đơn nguyên tử:** `OrderServiceImpl.createOrder` chạy `@Transactional`, kiểm tra user/cart/tồn, tính tiền và coupon, tạo order + order items + tracking log, trừ kho và đánh dấu cart items đã xóa. Nắm rõ transaction rollback và các tình huống tranh chấp tồn kho chưa được giải quyết bằng khóa/optimistic locking.
2. **Lọc động:** `ProductSpec`, `CouponSpec`, `UserSpec`, `OrderSpec` kết hợp predicate theo input tùy chọn và `PageRequest`; giải thích cách 1-based page được đổi thành 0-based.
3. **Máy trạng thái đơn:** `OrderStatus.canTransitionTo`, cùng điều kiện vai trò trong `OrderServiceImpl`; phân biệt quyền chuyển tiếp của admin và shipper, và cách lưu `TrackingLog`.
4. **Coupon và tổng tiền:** `CouponRepository.findValidCoupon` lọc trạng thái/thời hạn/usage; service tính percentage/fixed discount, min order, max discount; cần nêu rõ shipping fee hiện đang hard-code.
5. **Fetch dữ liệu quan hệ:** `JOIN FETCH` cho order/cart items và `@EntityGraph` trong `OrderRepository`; giải thích mục đích giảm truy vấn lazy theo ngữ cảnh, tránh tuyên bố đã benchmark/khử mọi N+1.

## 6. Kỹ năng nên ghi trong CV

- **Programming Languages:** Java 17 (được dùng trong project).
- **Backend Technologies:** Spring Boot, Spring MVC, Spring Data JPA, Hibernate, Spring Security, Jakarta Validation, MapStruct, Spring Mail.
- **Database:** MySQL, relational entity modeling, JPA repositories, JPQL, dynamic Specifications.
- **Tools:** Maven/Maven Wrapper, Swagger/OpenAPI, Cloudinary, Lombok.
- **Concepts:** REST API, layered architecture, DTO mapping, role-based access control, HTTP Basic, BCrypt, pagination/filtering, transaction management, soft delete, auditing, order lifecycle, inventory and coupon business rules.

Chỉ ghi các mục bạn có thể giải thích và xác nhận đã trực tiếp sử dụng; mã nguồn không cho biết mức độ thành thạo.

## 7. Không nên ghi là đã hoàn thành (theo bằng chứng hiện có)

- JWT authentication, refresh token, OAuth2 hoặc session/token management.
- Cổng thanh toán/tích hợp payment gateway; code mới lưu phương thức và trạng thái ban đầu.
- Chức năng review/đánh giá sản phẩm.
- Docker, CI/CD, triển khai production, quy mô người dùng hoặc tải thực tế.
- Đã tối ưu hiệu năng theo số liệu hoặc loại bỏ toàn bộ N+1.
- Tự động kiểm thử nghiệp vụ toàn diện; repository chỉ có test context-load.
- Gửi email/upload Cloudinary thành công trong môi trường thật (chưa xác minh credential/runtime).
- Cá nhân đã tự phát triển toàn bộ project, phần trăm đóng góp, quy mô nhóm và thời gian làm dự án: cần bạn xác nhận.

## 8. Câu hỏi phỏng vấn có thể phát sinh

1. **Mô tả luồng tạo đơn và transaction.** Hãy lần từ `OrderController` tới `OrderServiceImpl`; giải thích dữ liệu nào được ghi và điều gì xảy ra nếu một bước ném exception.
2. **Làm sao tránh bán vượt tồn kho khi hai request đặt cùng lúc?** Code hiện chỉ đọc số lượng rồi trừ; hãy tìm hiểu transaction isolation, optimistic/pessimistic locking hoặc câu lệnh update có điều kiện.
3. **Coupon hợp lệ được xác định ra sao?** Giải thích query `findValidCoupon`, min order, usage limit, max discount, và vấn đề cập nhật đồng thời `usedCount`.
4. **HTTP Basic hoạt động thế nào trong project? Trường `token` login có phải JWT không?** Nêu rõ nó là Base64 credentials, không phải JWT; chỉ gửi qua TLS trong ứng dụng phù hợp và cân nhắc bỏ tên token gây nhầm lẫn.
5. **Phân biệt authentication và authorization.** Trình bày `UserDetailsService`, BCrypt, role mapping, filter-chain rules và `@PreAuthorize`; lưu ý xác minh method security activation.
6. **Giải thích `Specification` và phân trang.** Chọn ví dụ `ProductSpec`/`CouponSpec`, mô tả predicate tùy chọn và page index.
7. **Tại sao dùng `JOIN FETCH`/`EntityGraph`?** Chỉ ra repository method và phạm vi quan hệ được nạp; chưa có benchmark chứng minh mức cải thiện.
8. **Mô hình trạng thái đơn hoạt động thế nào?** Phân tích chuyển tiếp admin/shipper, tự gán shipper theo tỉnh, kiểm tra shipper sở hữu đơn và ghi lịch sử.
9. **Soft delete và auditing được thực hiện thế nào?** Giải thích `BaseEntity`, `deleted`, repository methods, `JpaAuditingConfig`; chỉ ra các truy vấn cần bảo đảm luôn loại record đã xóa.
10. **Thông báo email có retry không?** Scheduler hiện chọn tối đa 50 PENDING, gửi, đánh dấu SENT/FAILED; code hiện không retry FAILED tự động.
11. **Nếu upload ảnh thành công nhưng lưu DB thất bại thì xử lý file mồ côi ra sao?** Đọc `CloudinaryServiceImpl` và các service gọi nó; đây là điểm cần cân nhắc về bù trừ transaction ngoài DB.
12. **Project được kiểm thử thế nào và chạy ra sao?** Hiện chỉ có context-load test; cần tự xác nhận cấu hình MySQL, Cloudinary/Mail và bổ sung kiểm thử nghiệp vụ trước khi khẳng định chất lượng runtime.

## Xác nhận cần từ bạn trước khi dùng CV

- Bạn trực tiếp làm những phần nào (catalog, auth/security, cart/order, coupon, shipper, Cloudinary/email)?
- Đây là project cá nhân hay nhóm; nếu nhóm, phần đóng góp cụ thể của bạn là gì?
- Bạn đã chạy và kiểm chứng MySQL, Cloudinary, SMTP ở môi trường nào chưa?
