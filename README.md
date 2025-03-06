## 타입어택 이벤트 처리 시스템 프로토타입 서버 개발

### A서버
#### 역할
- 클라이언트에서 WebFlux 스트리밍 요청을 수신
- 중복 요청 체크 (Redis)
- Kafka로 메시지 전송
- MySQL에 Batch Insert (비동기, 코루틴 사용)
#### 주요 기술 스택
- Spring WebFlux + 코루틴 (비동기 요청 처리)
- Redis (중복 요청 체크)
- Kafka Producer (이벤트 전송)
- R2DBC + MySQL (비동기 배치 저장)
---
### B서버
#### 역할
- Kafka Consumer로 메시지 수신
- Redis에 데이터 저장 (User별 key-value 저장)
- Redis Stream을 통해 C 서버로 데이터 전송
### 주요 기술 스택
- Kafka Consumer (Batch Mode) (대량 메시지 처리)
- Redis (데이터 저장 및 Stream 사용)
- Reactive Programming (Mono, Flux)
---
### C서버
#### 역할
- Redis Stream을 구독하여 데이터 수신
- MySQL에 Batch Insert (R2DBC)
- Chunked 처리하여 DB 부하 최적화
### 주요 기술 스택
- Redis Stream Consumer (Reactive 방식으로 데이터 수신)
- R2DBC + MySQL (비동기 배치 저장)
