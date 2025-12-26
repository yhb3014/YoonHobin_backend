# 송금 서비스

계좌 간 송금 시스템을 멀티모듈 구조로 구현했습니다. JPA 기반으로 데이터 연동하고, Docker Compose로 실행합니다.

## 아키텍처 개요

```
[API Controller]
       |
       v
[Application Service]
       |
       v
[Domain Service] -> [Domain Model]
       |
       v
[Repository Interface] -> [JPA Adapter] -> [PostgreSQL]
```

## 모듈 구성

- `remittance-api`: REST API, 애플리케이션 서비스, 설정, OpenAPI(Swagger)
- `remittance-domain`: 도메인 서비스, 정책, 리포지토리 인터페이스
- `remittance-infra`: JPA 엔티티, 리포지토리 어댑터

## 데이터 모델 요약

- `accounts`
  - 계좌번호, 잔액, 상태(활성/해지)
- `transactions`
  - 거래 타입(입금/출금/이체), 금액, 수수료, 상대 계좌, 잔액 전후, 그룹 ID, 생성 시각

## 동시성 처리

- 계좌 갱신 시 `PESSIMISTIC_WRITE` 락 사용
- 교차 이체 시 계좌번호 정렬로 락 획득 순서 고정(데드락 방지)

## 외부 라이브러리 사용 목적

- `springdoc-openapi`: API 명세/Swagger UI 제공
- `testcontainers`: 통합 테스트에서 PostgreSQL 환경 제공

## API 엔드포인트

Base URL: `http://localhost:8080`

### 계좌

- `POST /api/account` (등록)
- `DELETE /api/account/{accountNumber}` (삭제)

### 송금

- `POST /api/transfer/deposit/{accountNumber}` (입금)
- `POST /api/transfer/withdraw/{accountNumber}` (출금)
- `POST /api/transfer/transfers` (이체)

### 거래내역

- `GET /api/transaction/{accountNumber}`

## Swagger UI

- `http://localhost:8080/swagger-ui/index.html`

## Docker Compose 실행

```bash
docker compose up -d --build
```

중지:

```bash
docker compose down
```

## 환경 변수

`docker-compose.yml` 기본값

- `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/remittance`
- `SPRING_DATASOURCE_USERNAME=remittance`
- `SPRING_DATASOURCE_PASSWORD=remittance`

## 테스트

테스트는 Testcontainers를 사용하므로 **Docker Desktop 실행이 필요**합니다.

Windows:

```bash
.\gradlew.bat test

.\gradlew.bat :remittance-domain:test
.\gradlew.bat :remittance-api:test
```

## 로컬 실행

```bash
.\gradlew.bat :remittance-api:bootRun
```

## API 요청/응답 예시

### 계좌 등록

요청:

```bash
curl -X POST "http://localhost:8080/api/account" ^
  -H "Content-Type: application/json" ^
  -d "{\"accountNumber\":\"111-111-111\"}"
```

응답:

```json
{
  "success": true,
  "data": { "accountNumber": "111-111-111" },
  "error": null,
  "timestamp": "2025-12-26T15:00:00+09:00"
}
```

### 계좌 삭제

요청:

```bash
curl -X DELETE "http://localhost:8080/api/account/111-111-111"
```

응답:

```json
{
  "success": true,
  "data": null,
  "error": null,
  "timestamp": "2025-12-26T15:00:00+09:00"
}
```

### 입금

요청:

```bash
curl -X POST "http://localhost:8080/api/transfer/deposit/111-111-111" ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\":10000}"
```

응답:

```json
{
  "success": true,
  "data": { "accountNumber": "111-111-111", "balance": 10000 },
  "error": null,
  "timestamp": "2025-12-26T15:00:00+09:00"
}
```

### 출금

요청:

```bash
curl -X POST "http://localhost:8080/api/transfer/withdraw/111-111-111" ^
  -H "Content-Type: application/json" ^
  -d "{\"amount\":5000}"
```

응답:

```json
{
  "success": true,
  "data": { "accountNumber": "111-111-111", "balance": 5000 },
  "error": null,
  "timestamp": "2025-12-26T15:00:00+09:00"
}
```

### 이체

요청:

```bash
curl -X POST "http://localhost:8080/api/transfer/transfers" ^
  -H "Content-Type: application/json" ^
  -d "{\"fromAccount\":\"111-111-111\",\"toAccount\":\"111-111-112\",\"amount\":5000}"
```

응답:

```json
{
  "success": true,
  "data": {
    "fromAccountNumber": "111-111-111",
    "fromBalance": 4950,
    "toAccountNumber": "111-111-112",
    "toBalance": 5000,
    "fee": 50
  },
  "error": null,
  "timestamp": "2025-12-26T15:00:00+09:00"
}
```

### 거래내역 조회

요청:

```bash
curl "http://localhost:8080/api/transaction/111-111-111?page=0&size=20"
```

응답:

```json
{
  "success": true,
  "data": {
    "accountNumber": "111-111-111",
    "page": 0,
    "size": 20,
    "items": [
      {
        "type": "WITHDRAW",
        "amount": 5000,
        "feeAmount": 0,
        "counterpartyAccountNumber": null,
        "balanceBefore": 10000,
        "balanceAfter": 5000,
        "groupId": null,
        "createdAt": "2025-12-26T15:00:00+09:00"
      }
    ]
  },
  "error": null,
  "timestamp": "2025-12-26T15:00:00+09:00"
}
```

## 참고 자료

- https://www.postgresql.org/docs/current/explicit-locking.html
- https://notavoid.tistory.com/119
