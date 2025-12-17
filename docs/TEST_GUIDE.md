# 테스트 가이드

## 1. 환경 요구사항

- Java 17+
- Maven 3.6+

---

## 2. 프로젝트 실행

```bash
# 프로젝트 디렉토리로 이동
cd source

# 의존성 설치 및 실행
mvn spring-boot:run
```

서버 시작 후 `http://localhost:8080` 에서 접근 가능합니다.

---

## 3. API 목록

### 3.1 프로모션 조회 API (본 과제 구현)

```bash
GET /api/v1/promotions
```

**요청 예시:**
```bash
curl http://localhost:8080/api/v1/promotions
```

**응답 예시:**
```json
[
  {
    "prodCd": "P001",
    "prodNm": "올리브영 스킨케어 세트",
    "promoPrice": 29900,
    "startDt": "20251201",
    "endDt": "20251231"
  }
]
```

---

### 3.2 테스트 데이터 생성 API

대량 데이터로 성능 테스트 시 사용합니다.

```bash
POST /api/data/generate?productCount={상품수}&promotionCount={프로모션수}
```

**요청 예시:**
```bash
# 상품 10,000건, 프로모션 50,000건 생성
curl -X POST "http://localhost:8080/api/data/generate?productCount=10000&promotionCount=50000"
```

**응답 예시:**
```json
{
  "message": "Test data generated",
  "products": 10000,
  "promotions": 50000
}
```

---

### 3.3 성능 비교 API

AS-IS와 TO-BE 쿼리의 성능을 비교합니다.

```bash
GET /api/benchmark/compare?iterations={반복횟수}
```

**요청 예시:**
```bash
# 100회 반복 측정
curl "http://localhost:8080/api/benchmark/compare?iterations=100"
```

**응답 예시:**
```json
{
  "iterations": 100,
  "AS-IS (Legacy)": {
    "avg": 1250.5,
    "min": 980.0,
    "max": 1850.0,
    "p50": 1200.0,
    "p95": 1580.0,
    "unit": "microseconds (μs)"
  },
  "TO-BE (Optimized)": {
    "avg": 850.2,
    "min": 720.0,
    "max": 1200.0,
    "p50": 830.0,
    "p95": 1050.0,
    "unit": "microseconds (μs)"
  },
  "improvement_percent": "32.01%"
}
```

---

## 4. 성능 테스트 실행 순서

### Step 1: 서버 시작
```bash
mvn spring-boot:run
```

### Step 2: 대량 데이터 생성
```bash
curl -X POST "http://localhost:8080/api/data/generate?productCount=10000&promotionCount=50000"
```

### Step 3: 성능 비교 실행
```bash
curl "http://localhost:8080/api/benchmark/compare?iterations=100"
```

### Step 4: 결과 확인
- `avg`: 평균 실행 시간
- `p50`: 중간값 (50% 백분위)
- `p95`: 95% 백분위
- `improvement_percent`: TO-BE 대비 개선율

---

## 5. H2 Console 접속

인메모리 DB 데이터 확인용

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:partnerdb`
- Username: `sa`
- Password: (빈값)

---

## 6. 프로젝트 구조

```
src/main/java/com/oliveyoung/partner/
├── PartnerOfficeApplication.java     # 메인
├── controller/
│   ├── PromotionController.java      # GET /api/v1/promotions
│   ├── BenchmarkController.java      # 성능 비교 API
│   └── DataGeneratorController.java  # 테스트 데이터 생성
├── service/
│   └── PromotionService.java
├── repository/
│   ├── PromotionRepository.java      # JPA (TO-BE)
│   └── PromotionJdbcRepository.java  # JDBC (AS-IS vs TO-BE 비교)
├── entity/
│   ├── Product.java
│   └── Promotion.java
└── dto/
    └── PromotionProductDto.java
```

---

## 7. 초기 더미 데이터

서버 시작 시 `data.sql`에서 자동 로드됩니다.

| 테이블 | 건수 | 비고 |
|-------|-----|-----|
| TB_PRODUCT | 3건 | P003은 USE_YN='N' |
| TB_PROMOTION | 3건 | 현재 날짜 기준 유효 프로모션 |
