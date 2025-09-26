# 🚀 Railway 배포 가이드

이 문서는 **SpringAI-openai-chatbot-CalendarTodo** 프로젝트를 Railway에 배포하는 방법을 설명합니다.

## 📋 사전 준비사항

1. **GitHub 계정** 및 이 프로젝트의 리포지토리
2. **Railway 계정** ([railway.app](https://railway.app) 회원가입)
3. **OpenAI API 키** (GitHub Secrets에 설정 완료)

## 🔧 Railway 배포 단계

### 1단계: Railway 프로젝트 생성

1. [Railway 대시보드](https://railway.app/dashboard) 접속
2. "New Project" 클릭
3. "Deploy from GitHub repo" 선택
4. 본 프로젝트 리포지토리 선택

### 2단계: 데이터베이스 추가

1. Railway 프로젝트 대시보드에서 "Add Service" 클릭
2. "Database" → "PostgreSQL" 선택
3. PostgreSQL 서비스가 생성되면 자동으로 `DATABASE_URL` 환경변수 생성됨

### 3단계: 환경변수 설정

Railway 프로젝트의 Variables 탭에서 다음 환경변수들을 설정:

**필수 환경변수:**
```bash
OPENAI_API_KEY=your_openai_api_key_here
APP_AI_ENABLED=true
```

**선택적 환경변수 (기본값 사용 가능):**
```bash
LOG_LEVEL=INFO                    # 로깅 레벨 (INFO/DEBUG/WARN)
JPA_SHOW_SQL=false               # SQL 쿼리 출력 여부
```

### 4단계: 배포 확인

1. Railway가 자동으로 빌드 및 배포 시작
2. 배포 완료 후 생성된 도메인 URL 확인
3. `/actuator/health` 엔드포인트로 헬스체크 확인

## 🌐 무료 배포 대안

### 1. **Railway** (추천)
- **장점**: PostgreSQL 포함, 자동 배포, 무료 티어
- **제한**: 월 500시간, $5 크레딧 (대부분의 개인 프로젝트에 충분)
- **도메인**: 자동 생성 (`*.railway.app`)

### 2. **Render**
- **장점**: PostgreSQL 포함, 자동 배포, 완전 무료 티어
- **단점**: 무료 티어는 15분 후 자동 슬립
- **설정**: `render.yaml` 파일 필요

### 3. **Heroku** (유료 전환)
- **참고**: 2022년 11월부터 무료 티어 제거

## 🐳 Docker 설명

**Docker**는 애플리케이션을 컨테이너라는 가상화된 환경에서 실행할 수 있게 해주는 플랫폼입니다.

### Docker의 장점:
- **환경 일관성**: 개발/테스트/프로덕션 환경이 동일
- **이식성**: 어떤 서버에서든 동일하게 실행
- **격리**: 다른 애플리케이션과 독립적 실행
- **효율성**: VM보다 가볍고 빠름

### 본 프로젝트에서 Docker 사용:
```bash
# Docker 이미지 빌드
docker build -t cal-todo-crud .

# 컨테이너 실행
docker run -p 8080:8080 \
  -e OPENAI_API_KEY=your_key \
  -e DATABASE_URL=your_db_url \
  cal-todo-crud
```

## 🔒 환경변수 보안

### GitHub Secrets 설정 (완료됨):
- `OPENAI_API_KEY` → GitHub 리포지토리 Secrets에 설정됨

### Railway Variables:
- GitHub Secrets에서 Railway Variables로 복사 필요
- Railway 대시보드에서 직접 설정

## 🚨 주의사항

1. **API 키 보안**: 절대 코드에 하드코딩하지 마세요
2. **데이터베이스**: Railway PostgreSQL은 자동으로 `DATABASE_URL` 생성
3. **포트**: Railway는 `PORT` 환경변수를 자동 설정 (8080이 기본값)
4. **로그 레벨**: 프로덕션에서는 `INFO` 레벨 권장

## 📊 배포 후 확인사항

- [ ] 애플리케이션 정상 실행 (`/`)
- [ ] 헬스체크 통과 (`/actuator/health`)
- [ ] 할 일 CRUD 기능 동작
- [ ] AI 챗봇 기능 동작
- [ ] 데이터베이스 연결 확인

## 🔧 트러블슈팅

### 자주 발생하는 문제:

1. **빌드 실패**
   ```bash
   # 로컬에서 테스트
   ./gradlew clean build
   ```

2. **데이터베이스 연결 실패**
   - `DATABASE_URL` 환경변수 확인
   - PostgreSQL 서비스 상태 확인

3. **OpenAI API 호출 실패**
   - `OPENAI_API_KEY` 환경변수 확인
   - API 키 유효성 확인

4. **메모리 부족**
   - Railway 무료 티어: 512MB RAM 제한
   - JVM 옵션 조정 필요시 `JAVA_OPTS` 환경변수 설정

## 📞 지원

문제 발생시:
1. Railway 로그 확인
2. GitHub Issues에 문제 보고
3. Railway 커뮤니티 포럼 참고