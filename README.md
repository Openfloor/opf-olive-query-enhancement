# 올리브영 파트너오피스 - Backend 이관 및 쿼리 튜닝

## 과제 개요

기존 레거시 시스템의 '프로모션 상품 목록 조회' 기능을 Spring Boot 환경으로 이관하고, 비효율적인 SQL을 개선하는 과제

## 수행 요건

- [x] Framework: Spring Boot 2.7 (Java 17)
- [x] ORM: Spring Data JPA
- [x] API: `GET /api/v1/promotions` RESTful API 구현
- [x] SQL 개선: 레거시 쿼리 성능 문제 식별 및 튜닝

## 빠른 시작

```bash
# 실행
mvn spring-boot:run

# API 호출
curl http://localhost:8080/api/v1/promotions
```

## 문서

- [SQL 최적화 보고서](docs/SQL_OPTIMIZATION.md) - AS-IS vs TO-BE 분석
- [테스트 가이드](docs/TEST_GUIDE.md) - API 사용법 및 성능 테스트

## 기술 스택

| 구분 | 기술 |
|-----|-----|
| Framework | Spring Boot 2.7.18 |
| Language | Java 17 |
| ORM | Spring Data JPA |
| Database | H2 (테스트) |
| Build | Maven |
