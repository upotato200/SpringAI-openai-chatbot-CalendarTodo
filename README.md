# Cal-Todo-CRUD

![Image](https://github.com/user-attachments/assets/bd37bf18-2ca4-4139-b50d-6bbc86bbd966)

## 개요
캘린더와 할 일 관리를 통합한 웹 애플리케이션입니다. Spring Boot 기반으로 개발되었으며, AI 챗봇 기능과 일정 요약 기능을 제공합니다.

## 주요 기능
- **캘린더 기반 할 일 관리**: 날짜별로 할 일을 추가, 수정, 삭제
- **AI 챗봇**: OpenAI GPT-4o-mini를 활용한 대화형 챗봇
- **일정 요약**: AI를 통한 일정 자동 요약 기능
- **다크모드**: 라이트/다크 테마 지원
- **반응형 디자인**: 모바일 및 데스크톱 환경 지원

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.6
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Build Tool**: Gradle

### Frontend
- **Template Engine**: Thymeleaf
- **CSS Framework**: Bootstrap 5.3.3
- **JavaScript Libraries**: SweetAlert2
- **Styling**: Custom CSS with Dark/Light theme support

### AI Integration
- **Spring AI**: OpenAI Spring Boot Starter
- **Model**: GPT-4o-mini

### Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Validation
- Spring Boot Starter Data JPA
- PostgreSQL Driver
- Lombok
- Spring DotEnv (환경변수 관리)

## 아키텍처
프로젝트는 Clean Architecture 패턴을 따릅니다:

```
src/main/java/com/best/caltodocrud/
├── api/                    # API Layer (Controllers, DTOs)
│   ├── chat/              # 챗봇 관련 API
│   ├── common/            # 공통 컨트롤러
│   ├── error/             # 전역 예외 처리
│   ├── summary/           # 요약 기능 API
│   └── todo/              # 할 일 관리 API
├── application/           # Application Layer (Use Cases)
│   ├── chat/
│   ├── summary/
│   └── todo/
├── config/                # Configuration
├── domain/                # Domain Layer (Entities, Exceptions)
└── infrastructure/        # Infrastructure Layer (Adapters)
    ├── ai/               # AI 서비스 구현
    └── persistence/      # 데이터 영속성
```

## 필수 요구사항
- Java 17+
- PostgreSQL
- OpenAI API Key

### 설치 및 실행

1. **프로젝트 클론**
```bash
git clone <repository-url>
cd Cal-Todo-CRUD
```

2. **데이터베이스 설정**
PostgreSQL에 `caltodo` 데이터베이스를 생성합니다.

3. **환경변수 설정**
`.env-example`을 참고하여 `.env` 파일을 생성하고 다음 값들을 설정합니다:
```env
SERVER_PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/caltodo
DB_USERNAME=your_username
DB_PASSWORD=your_password
APP_AI_ENABLED=true
OPENAI_API_KEY=your_openai_api_key
```

4. **애플리케이션 실행**
```bash
./gradlew bootRun
```

5. **접속**
브라우저에서 `http://localhost:8080`에 접속합니다.

## 주요 파일 구조

### API Endpoints
- `GET /` - 메인 페이지
- `GET /api/todos` - 할 일 목록 조회
- `POST /api/todos` - 할 일 생성
- `PUT /api/todos/{id}` - 할 일 수정
- `DELETE /api/todos/{id}` - 할 일 삭제
- `POST /api/chat` - 챗봇 대화
- `POST /api/summary` - 일정 요약

### 설정 파일
- `application.properties` - Spring Boot 설정
- `build.gradle` - 프로젝트 의존성 및 빌드 설정
- `.env-example` - 환경변수 템플릿

## 🔧 개발 환경 설정
- IDE: IntelliJ IDEA 권장
- Java SDK: 17
- Gradle: Wrapper 사용
