# 나만의 에세이 - Backend

> 일상의 기록이 나만의 책이 되는 경험

매일 4개 슬롯(독서/상담/힐링/일기) 기록을 통해 AI가 주간 에세이로 변환해주는 서비스의 백엔드 API입니다.

## 빠른 시작

### 필요 조건

- Java 17+
- Docker & Docker Compose
- PostgreSQL 15+

### 로컬 실행

```bash
# 저장소 클론
git clone https://github.com/your-org/my-essay-backend.git
cd my-essay-backend

# 환경변수 설정
cp .env.example .env
# .env 파일에서 OpenAI API 키 등 설정

# Docker로 데이터베이스 실행
docker-compose up -d postgres

# 애플리케이션 실행
./gradlew bootRun
```

## 기술 스택

- **Framework**: Spring Boot 3.5.5
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **Authentication**: JWT (Stateless)
- **AI Integration**: OpenAI GPT-4 API
- **Build Tool**: Gradle
- **Container**: Docker

## 핵심 기능

### 1. 인증 시스템
- JWT 기반 Stateless 인증
- 회원가입/로그인/토큰 갱신

### 2. 일일 기록 관리
- 4개 슬롯: 독서/상담/힐링/일기
- JSON 기반 유연한 컨텐츠 저장
- 완료율 자동 계산

### 3. AI 에세이 생성
- OpenAI GPT-4 연동
- 주간 기록 기반 자동 에세이 생성
- 폴백 템플릿 시스템

### 4. 커뮤니티
- 에세이 발행 (비공개/공유/공개)
- 좋아요/북마크 시스템
- 신고 및 관리 기능

## 프로젝트 구조

```
src/main/java/com/myessay/
├── config/          # 설정 클래스 (Security, OpenAI 등)
├── controller/      # REST API 컨트롤러
├── service/         # 비즈니스 로직
├── repository/      # 데이터 접근 계층
├── entity/          # JPA 엔티티
├── dto/             # 요청/응답 DTO
├── exception/       # 예외 처리
└── util/            # 유틸리티 클래스
```

## 주요 API 엔드포인트

### 인증
```
POST /api/auth/register    # 회원가입
POST /api/auth/login       # 로그인
POST /api/auth/refresh     # 토큰 갱신
GET  /api/auth/me          # 현재 사용자 정보
```

### 기록 관리
```
PUT  /api/records/{date}/{slotType}    # 슬롯 기록 저장
GET  /api/records/{date}               # 일일 기록 조회
GET  /api/records/week                 # 주간 기록 조회
```

### 에세이
```
POST /api/essays/generate              # AI 에세이 생성
GET  /api/essays/{id}                  # 에세이 조회
PUT  /api/essays/{id}                  # 에세이 수정
POST /api/essays/{id}/publish          # 에세이 발행
```

### 커뮤니티
```
GET  /api/feed/public                  # 공개 피드
GET  /api/share/{slug}                 # 공유 에세이 읽기
PUT  /api/essays/{id}/like             # 좋아요
PUT  /api/essays/{id}/bookmark         # 북마크
```

## 🗄 데이터베이스 스키마

### 핵심 테이블

**Users** - 사용자 정보
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    nickname VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Records** - 일일 기록 (JSON 컨텐츠)
```sql
CREATE TABLE records (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    record_date DATE NOT NULL,
    slot_type VARCHAR(20) CHECK (slot_type IN ('reading', 'consulting', 'healing', 'diary')),
    content JSONB NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    UNIQUE(user_id, record_date, slot_type)
);
```

**Essays** - AI 생성 에세이
```sql
CREATE TABLE essays (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    title VARCHAR(200),
    final_content TEXT NOT NULL,
    theme VARCHAR(50),
    publish_status VARCHAR(20) DEFAULT 'private',
    share_slug VARCHAR(100) UNIQUE,
    week_start DATE NOT NULL
);
```

## 🔧 환경 설정

### 필수 환경변수

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/myessay
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# JWT
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=86400

# OpenAI
OPENAI_API_KEY=sk-your-openai-api-key

# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=local
```

### application.yml 예시

```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:86400}

openai:
  api-key: ${OPENAI_API_KEY}
  model: gpt-4
  max-tokens: 2000

logging:
  level:
    com.myessay: DEBUG
    org.springframework.security: DEBUG
```

## Docker 배포

### Docker Compose

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/myessay
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: password
    depends_on:
      - postgres

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: myessay
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### 빌드 및 실행

```bash
# Docker 이미지 빌드
docker build -t myessay-backend .

# Docker Compose로 전체 스택 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app
```

## 🧪 테스트

```bash
# 단위 테스트 실행
./gradlew test

# 통합 테스트 포함
./gradlew integrationTest

# 테스트 커버리지 확인
./gradlew jacocoTestReport
```

## 모니터링

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### 메트릭 (Prometheus)
```bash
curl http://localhost:8080/actuator/prometheus
```

## 보안 정책

### Rate Limiting
- 전역: 1000 req/hour/user
- 로그인: 5 attempts/5min  
- AI 생성: 10 req/day/user

### 입력 검증
- Bean Validation (@Valid, @NotNull, @Size)
- 커스텀 비즈니스 규칙 검증

### 인증
- JWT Bearer Token (Stateless)
- 토큰 만료 시간: 24시간
- Refresh Token 지원

## 배포

### 스테이징 환경
```bash
# GitHub Actions 자동 배포
git push origin develop
```

### 프로덕션 환경
```bash
# 프로덕션 배포
git push origin main
```

## API 문서

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI 3.0 스펙: `http://localhost:8080/v3/api-docs`

## 기여하기

1. 이슈 등록 또는 기존 이슈 확인
2. 기능 브랜치 생성 (`git checkout -b feature/amazing-feature`)
3. 변경사항 커밋 (`git commit -m 'Add amazing feature'`)
4. 브랜치 푸시 (`git push origin feature/amazing-feature`)
5. Pull Request 생성

### 코딩 컨벤션

- Google Java Style Guide 준수
- 단위 테스트 커버리지 80% 이상
- API 문서 업데이트 필수

---

**나만의 에세이**로 당신의 일상이 특별한 이야기가 되길 바랍니다. ✨
