# Java 21 + Spring Boot 4.0.1 REST API 実務鉄板パターン カンニングペーパー

## 目次
1. [プロジェクト構成](#1-プロジェクト構成)
2. [Controller層](#2-controller層)
3. [Service層](#3-service層)
4. [Repository層](#4-repository層)
5. [エラーハンドリング](#5-エラーハンドリング)
6. [バリデーション](#6-バリデーション)
7. [Stream API実務パターン](#7-stream-api実務パターン)
8. [トランザクション](#8-トランザクション)
9. [ページネーション](#9-ページネーション)
10. [DTO変換](#10-dto変換)

---

## 1. プロジェクト構成

```
src/main/java/com/example/
├── controller/       # API層
├── service/          # ビジネスロジック層
├── repository/       # データアクセス層
├── entity/           # DB Entity
├── dto/              # リクエスト・レスポンス
│   ├── request/
│   └── response/
├── exception/        # カスタム例外
└── config/           # 設定クラス
```

---

## 2. Controller層

### 基本形（CRUD全パターン）

```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated // クラスレベルでバリデーション有効化
public class OrderController {

    private final OrderService service;

    // 一覧取得 (GET /api/orders)
    @GetMapping
    public List<OrderResponse> getAll() {
        return service.findAll();
    }

    // ID指定取得 (GET /api/orders/1)
    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return service.findById(id);
    }

    // 検索 (GET /api/orders/search?status=PENDING)
    @GetMapping("/search")
    public List<OrderResponse> search(
        @RequestParam(required = false) OrderStatus status,
        @RequestParam(required = false) String customerName
    ) {
        return service.search(status, customerName);
    }

    // 新規作成 (POST /api/orders)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201返す
    public OrderResponse create(@RequestBody @Valid OrderCreateRequest request) {
        return service.create(request);
    }

    // 更新 (PUT /api/orders/1)
    @PutMapping("/{id}")
    public OrderResponse update(
        @PathVariable Long id,
        @RequestBody @Valid OrderUpdateRequest request
    ) {
        return service.update(id, request);
    }

    // 一部更新 (PATCH /api/orders/1/status)
    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(
        @PathVariable Long id,
        @RequestBody @Valid StatusUpdateRequest request
    ) {
        return service.updateStatus(id, request.status());
    }

    // 削除 (DELETE /api/orders/1)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204返す
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
```

### ページネーション対応

```java
@GetMapping
public Page<OrderResponse> getAll(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "id,desc") String[] sort
) {
    return service.findAll(PageRequest.of(page, size, Sort.by(parseSort(sort))));
}

private Sort.Order[] parseSort(String[] sort) {
    return Arrays.stream(sort)
        .map(s -> {
            String[] parts = s.split(",");
            return parts.length == 2 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Order.desc(parts[0])
                : Sort.Order.asc(parts[0]);
        })
        .toArray(Sort.Order[]::new);
}
```

---

## 3. Service層

### 基本形

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // デフォルトは読み取り専用
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    // 一覧取得
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    // ID指定取得
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return toResponse(order);
    }

    // 検索 (動的クエリ)
    public List<OrderResponse> search(OrderStatus status, String customerName) {
        List<Order> orders;
        
        if (status != null && customerName != null) {
            orders = orderRepository.findByStatusAndCustomerNameContaining(status, customerName);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status);
        } else if (customerName != null) {
            orders = orderRepository.findByCustomerNameContaining(customerName);
        } else {
            orders = orderRepository.findAll();
        }
        
        return orders.stream()
            .map(this::toResponse)
            .toList();
    }

    // 新規作成
    @Transactional // 書き込みは readOnly = false
    public OrderResponse create(OrderCreateRequest request) {
        // 関連エンティティの存在チェック
        Customer customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));

        // エンティティ作成
        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .customer(customer)
            .amount(request.amount())
            .status(OrderStatus.PENDING)
            .build();

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    // 更新
    @Transactional
    public OrderResponse update(Long id, OrderUpdateRequest request) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        // 更新処理
        order.setAmount(request.amount());
        order.setStatus(request.status());
        // JPAの変更検知で自動保存される (saveは不要)

        return toResponse(order);
    }

    // ステータス更新のみ
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        order.setStatus(status);
        return toResponse(order);
    }

    // 削除
    @Transactional
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order", id);
        }
        orderRepository.deleteById(id);
    }

    // DTO変換 (private)
    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getCustomer().getName(),
            order.getAmount(),
            order.getStatus(),
            order.getCreatedAt()
        );
    }

    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis();
    }
}
```

### 複雑な集計処理

```java
@Transactional(readOnly = true)
public OrderSummaryResponse getSummary(Long customerId) {
    List<Order> orders = orderRepository.findByCustomerId(customerId);

    // 合計金額
    long totalAmount = orders.stream()
        .mapToLong(Order::getAmount)
        .sum();

    // ステータスごとの件数
    Map<OrderStatus, Long> countByStatus = orders.stream()
        .collect(Collectors.groupingBy(
            Order::getStatus,
            Collectors.counting()
        ));

    // 平均金額
    double averageAmount = orders.stream()
        .mapToLong(Order::getAmount)
        .average()
        .orElse(0.0);

    return new OrderSummaryResponse(
        totalAmount,
        orders.size(),
        averageAmount,
        countByStatus
    );
}
```

---

## 4. Repository層

### 基本形

```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // 命名規約クエリ (実装不要)
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByStatus(OrderStatus status);
    
    List<Order> findByCustomerId(Long customerId);
    
    List<Order> findByCustomerNameContaining(String name);
    
    List<Order> findByStatusAndCustomerNameContaining(OrderStatus status, String name);
    
    // 日付範囲検索
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // ステータスリストで検索
    List<Order> findByStatusIn(List<OrderStatus> statuses);
    
    // 存在チェック
    boolean existsByOrderNumber(String orderNumber);
    
    // カウント
    long countByStatus(OrderStatus status);
}
```

### @Query使用パターン

```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    // JPQL
    @Query("SELECT o FROM Order o WHERE o.amount >= :minAmount")
    List<Order> findByMinAmount(@Param("minAmount") long minAmount);

    // JOIN FETCH (N+1問題対策)
    @Query("SELECT o FROM Order o JOIN FETCH o.customer WHERE o.id = :id")
    Optional<Order> findByIdWithCustomer(@Param("id") Long id);

    // ネイティブSQL (複雑な集計)
    @Query(value = """
        SELECT status, COUNT(*) as count, SUM(amount) as total
        FROM orders
        WHERE customer_id = :customerId
        GROUP BY status
        """, nativeQuery = true)
    List<Object[]> getStatsByCustomer(@Param("customerId") Long customerId);

    // 更新クエリ
    @Modifying
    @Query("UPDATE Order o SET o.status = :status WHERE o.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") OrderStatus status);
}
```

---

## 5. エラーハンドリング

### カスタム例外

```java
// 基底例外
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

// リソースが見つからない
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s (ID: %d) が見つかりません", resourceName, id));
    }
}

// ビジネスルール違反
public class BusinessRuleViolationException extends BusinessException {
    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
```

### グローバル例外ハンドラ

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // リソース未検出
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
    }

    // バリデーションエラー
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "",
                (existing, replacement) -> existing // 重複時は既存を保持
            ));

        log.warn("Validation failed: {}", errors);
        return new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "入力値に誤りがあります",
            LocalDateTime.now(),
            errors
        );
    }

    // ビジネスルール違反
    @ExceptionHandler(BusinessRuleViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBusinessRule(BusinessRuleViolationException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
    }

    // その他の例外
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "システムエラーが発生しました",
            LocalDateTime.now()
        );
    }
}
```

### エラーレスポンスDTO

```java
public record ErrorResponse(
    int status,
    String message,
    LocalDateTime timestamp,
    Map<String, String> errors
) {
    public ErrorResponse(int status, String message, LocalDateTime timestamp) {
        this(status, message, timestamp, null);
    }
}
```

---

## 6. バリデーション

### リクエストDTO

```java
public record OrderCreateRequest(
    @NotNull(message = "顧客IDは必須です")
    Long customerId,

    @NotBlank(message = "商品名は必須です")
    @Size(max = 100, message = "商品名は100文字以内です")
    String productName,

    @Positive(message = "金額は正の数である必要があります")
    @Max(value = 10000000, message = "金額は1000万円以内です")
    long amount,

    @Min(value = 1, message = "数量は1以上です")
    @Max(value = 999, message = "数量は999以内です")
    int quantity,

    @Email(message = "メールアドレスの形式が正しくありません")
    String email,

    @Pattern(regexp = "^[0-9]{3}-[0-9]{4}-[0-9]{4}$", message = "電話番号の形式が正しくありません")
    String phoneNumber
) {}
```

### カスタムバリデーション

```java
// アノテーション定義
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureDateValidator.class)
public @interface FutureDate {
    String message() default "日付は未来である必要があります";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// バリデータ実装
public class FutureDateValidator implements ConstraintValidator<FutureDate, LocalDate> {
    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // nullチェックは@NotNullに任せる
        }
        return value.isAfter(LocalDate.now());
    }
}

// 使用例
public record EventCreateRequest(
    @NotNull
    @FutureDate(message = "イベント日は未来の日付を指定してください")
    LocalDate eventDate
) {}
```

---

## 7. Stream API実務パターン

### パターン1: フィルタリング + 変換

```java
// 特定条件のIDリストを取得
List<Long> activeOrderIds = orders.stream()
    .filter(order -> order.getStatus() == OrderStatus.ACTIVE)
    .map(Order::getId)
    .toList();

// 条件に合うものだけDTO変換
List<OrderResponse> responses = orders.stream()
    .filter(order -> order.getAmount() >= 10000)
    .map(this::toResponse)
    .toList();
```

### パターン2: Mapへの変換

```java
// IDをキーにしたMap
Map<Long, Order> orderMap = orders.stream()
    .collect(Collectors.toMap(Order::getId, Function.identity()));

// カスタムキー (注文番号)
Map<String, Order> orderByNumber = orders.stream()
    .collect(Collectors.toMap(Order::getOrderNumber, Function.identity()));
```

### パターン3: グループ化

```java
// ステータスごとにグループ化
Map<OrderStatus, List<Order>> byStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::getStatus));

// 顧客ごとの合計金額
Map<Long, Long> totalByCustomer = orders.stream()
    .collect(Collectors.groupingBy(
        order -> order.getCustomer().getId(),
        Collectors.summingLong(Order::getAmount)
    ));

// ステータスごとの件数
Map<OrderStatus, Long> countByStatus = orders.stream()
    .collect(Collectors.groupingBy(
        Order::getStatus,
        Collectors.counting()
    ));
```

### パターン4: 集計

```java
// 合計
long totalAmount = orders.stream()
    .mapToLong(Order::getAmount)
    .sum();

// 平均
double averageAmount = orders.stream()
    .mapToLong(Order::getAmount)
    .average()
    .orElse(0.0);

// 最大値
Optional<Order> maxOrder = orders.stream()
    .max(Comparator.comparing(Order::getAmount));

// 最小値
long minAmount = orders.stream()
    .mapToLong(Order::getAmount)
    .min()
    .orElse(0L);
```

### パターン5: 複雑な処理

```java
// 重複除去
List<String> uniqueCustomerNames = orders.stream()
    .map(order -> order.getCustomer().getName())
    .distinct()
    .toList();

// ソート
List<Order> sortedOrders = orders.stream()
    .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
    .toList();

// 制限
List<Order> top10 = orders.stream()
    .sorted(Comparator.comparing(Order::getAmount).reversed())
    .limit(10)
    .toList();

// フラット化 (注文 → 注文明細)
List<OrderItem> allItems = orders.stream()
    .flatMap(order -> order.getItems().stream())
    .toList();

// 条件判定
boolean hasLargeOrder = orders.stream()
    .anyMatch(order -> order.getAmount() > 1000000);

boolean allPending = orders.stream()
    .allMatch(order -> order.getStatus() == OrderStatus.PENDING);
```

### パターン6: Optionalとの組み合わせ

```java
// 最初の一致を取得
Optional<Order> firstActive = orders.stream()
    .filter(order -> order.getStatus() == OrderStatus.ACTIVE)
    .findFirst();

// 条件に合うものを処理
orders.stream()
    .filter(order -> order.getAmount() > 10000)
    .findFirst()
    .ifPresent(order -> {
        // 処理
    });

// 値取得 + デフォルト値
String orderNumber = orders.stream()
    .filter(order -> order.getStatus() == OrderStatus.PENDING)
    .findFirst()
    .map(Order::getOrderNumber)
    .orElse("なし");
```

---

## 8. トランザクション

### 基本パターン

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // クラスレベルはreadOnly
public class OrderService {

    // 読み取り専用 (クラスのデフォルトを使用)
    public OrderResponse findById(Long id) {
        // ...
    }

    // 書き込みあり
    @Transactional // readOnly = false
    public OrderResponse create(OrderCreateRequest request) {
        // ...
    }

    // 明示的にreadonlyを指定
    @Transactional(readOnly = false)
    public OrderResponse update(Long id, OrderUpdateRequest request) {
        // ...
    }
}
```

### 複数リポジトリを使う場合

```java
@Transactional
public OrderResponse createWithInventory(OrderCreateRequest request) {
    // 1. 注文作成
    Order order = orderRepository.save(new Order(/* ... */));

    // 2. 在庫減らす
    Inventory inventory = inventoryRepository.findByProductId(request.productId())
        .orElseThrow();
    inventory.decrease(request.quantity());
    // inventoryRepository.save(inventory); // 不要 (変更検知で自動保存)

    // 3. ログ記録
    auditLogRepository.save(new AuditLog(/* ... */));

    return toResponse(order);
    // メソッド終了時に全てcommit (エラー時は全てrollback)
}
```

### 例外発生時のロールバック

```java
@Transactional
public void processOrder(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    
    // ビジネスルール違反で例外 → 自動ロールバック
    if (order.getStatus() != OrderStatus.PENDING) {
        throw new BusinessRuleViolationException("処理できない状態です");
    }
    
    order.setStatus(OrderStatus.PROCESSING);
    // RuntimeExceptionならロールバック
    // Checked Exceptionはロールバックしない (要注意)
}
```

---

## 9. ページネーション

### Serviceでの実装

```java
@Transactional(readOnly = true)
public Page<OrderResponse> findAll(Pageable pageable) {
    return orderRepository.findAll(pageable)
        .map(this::toResponse); // Pageも map() 使える
}

@Transactional(readOnly = true)
public Page<OrderResponse> searchByStatus(OrderStatus status, Pageable pageable) {
    return orderRepository.findByStatus(status, pageable)
        .map(this::toResponse);
}
```

### Repositoryでの定義

```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    Page<Order> findByCustomerNameContaining(String name, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.amount >= :minAmount")
    Page<Order> findByMinAmount(@Param("minAmount") long minAmount, Pageable pageable);
}
```

### Controllerでの利用

```java
@GetMapping
public Page<OrderResponse> getAll(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    @RequestParam(defaultValue = "id") String sortBy,
    @RequestParam(defaultValue = "desc") String direction
) {
    Sort sort = "desc".equalsIgnoreCase(direction)
        ? Sort.by(sortBy).descending()
        : Sort.by(sortBy).ascending();
    
    Pageable pageable = PageRequest.of(page, size, sort);
    return service.findAll(pageable);
}
```

### レスポンス例

```json
{
  "content": [
    { "id": 1, "orderNumber": "ORD001", ... },
    { "id": 2, "orderNumber": "ORD002", ... }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true
}
```

---

## 10. DTO変換

### Entity → Response変換

```java
// 基本パターン (private メソッド)
private OrderResponse toResponse(Order order) {
    return new OrderResponse(
        order.getId(),
        order.getOrderNumber(),
        order.getCustomer().getName(),
        order.getAmount(),
        order.getStatus(),
        order.getCreatedAt()
    );
}

// Stream で一括変換
List<OrderResponse> responses = orders.stream()
    .map(this::toResponse)
    .toList();

// Page で変換
Page<OrderResponse> responses = orderPage.map(this::toResponse);
```

### 関連エンティティ含む変換

```java
private OrderDetailResponse toDetailResponse(Order order) {
    return new OrderDetailResponse(
        order.getId(),
        order.getOrderNumber(),
        // 顧客情報
        new CustomerInfo(
            order.getCustomer().getId(),
            order.getCustomer().getName(),
            order.getCustomer().getEmail()
        ),
        // 明細リスト
        order.getItems().stream()
            .map(item -> new OrderItemInfo(
                item.getProductName(),
                item.getQuantity(),
                item.getPrice()
            ))
            .toList(),
        order.getAmount(),
        order.getStatus(),
        order.getCreatedAt()
    );
}
```

### MapStructを使う場合

```java
// Mapper定義
@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse toResponse(Order order);
    
    List<OrderResponse> toResponseList(List<Order> orders);
    
    @Mapping(target = "customerName", source = "customer.name")
    OrderResponse toResponseWithCustomerName(Order order);
}

// Service での使用
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    
    public List<OrderResponse> findAll() {
        return orderMapper.toResponseList(orderRepository.findAll());
    }
}
```

---

## 補足: よく使うアノテーション一覧

### クラスレベル

```java
@RestController         // Controller
@Service               // Service
@Repository            // Repository (通常はinterfaceなので省略可)
@Configuration         // 設定クラス
@Component             // その他のBean

@RequiredArgsConstructor  // finalフィールドのコンストラクタ生成(Lombok)
@Slf4j                    // ログ用フィールド生成(Lombok)

@Transactional(readOnly = true)  // トランザクション設定
@Validated                       // バリデーション有効化
```

### メソッドレベル

```java
@GetMapping           // GET
@PostMapping          // POST
@PutMapping           // PUT
@PatchMapping         // PATCH
@DeleteMapping        // DELETE

@Transactional        // トランザクション
@ResponseStatus       // HTTPステータス指定
```

### パラメータレベル

```java
@PathVariable         // パス変数
@RequestParam         // クエリパラメータ
@RequestBody          // リクエストボディ
@Valid               // バリデーション実行

@NotNull             // null不可
@NotBlank            // 空文字不可
@Size                // サイズ制限
@Min / @Max          // 数値範囲
@Email               // メール形式
@Pattern             // 正規表現
```

---

## まとめ: 開発フロー

1. **Entity作成** → DB設計に合わせて
2. **Repository作成** → メソッド名でクエリ自動生成
3. **DTO作成** (Record) → Request/Response
4. **Service作成** → ビジネスロジック + DTO変換
5. **Controller作成** → API定義
6. **例外ハンドラ追加** → GlobalExceptionHandler
7. **テスト実装** → 実務では必須

この流れで実装すれば、Java 21 + Spring Boot 4.0.1 のREST APIは9割対応可能です。
