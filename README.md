# PlanFlow

AI가 회의 자료를 기획서 → 기능 명세서 → 화면 기획서 → 와이어프레임 순서로 자동 생성해주고, 팀원들이 실시간으로 함께 검토·확정·재요청할 수 있는 협업 툴입니다.


- 프론트엔드: [`2026-spring-team1-FE`](https://github.com/xihxxn/2026-spring-team1-FE)

## 무엇을 만들었나

기획 회의가 끝나면 보통 그 내용을 정리해서 기획서를 쓰고, 기능 명세서를 뽑고, 화면 단위로 쪼개고, 와이어프레임을 그리는 과정을 거칩니다. 이 프로젝트는 이 흐름 전체를 AI가 순서대로 자동 생성하고, 각 단계 문서를 팀 리더가 검토 후 "확정"하면 다음 단계로 넘어가는 파이프라인으로 구현했습니다.

```
회의 자료(텍스트/STT)
      │
      ▼
 1. 기획서 (PLAN)         AI 생성 → 팀원 검토 → 리더 확정
      │
      ▼
 2. 기능 명세서 (FEATURE_SPEC)   AI 생성 → 팀원 검토 → 리더 확정
      │
      ▼
 3. 화면별 기획서 (SCREEN_SPEC)  AI 생성 → 팀원 검토 → 리더 확정
      │
      ▼
 4. 와이어프레임 (WIREFRAME)     화면별 AI 생성 → 재생성 요청/승인/거절
      │
      ▼
 Markdown 리포트로 최종 export
```

각 단계는 확정 전까지 프로젝트 멤버들이 WebSocket으로 실시간 편집 현황을 공유하며, 확정된 문서만 다음 단계 AI 생성의 입력으로 쓰입니다.

## 핵심 기능

- **회의 자료 업로드 및 STT 변환** — 오디오 파일을 S3에 저장하고 텍스트로 변환
- **AI 문서 자동 생성** — 회의 내용을 바탕으로 기획서/기능명세서/화면기획서를 Gemini로 순차 생성
- **실시간 협업** — WebSocket으로 문서 편집, AI 생성 완료, 단계 확정 이벤트를 실시간 공유

## 기술 스택

| 영역 | 스택 |
|---|---|
| Language / Framework | Java 21, Spring Boot 3.5 |
| 인증 | Spring Security, 세션 기반 인증(httpOnly 쿠키) |
| DB | PostgreSQL (운영), H2 (테스트) |
| ORM | Spring Data JPA / Hibernate |
| 실시간 통신 | Spring WebSocket |
| AI 연동 | Google Gemini API |
| 파일 저장 | AWS S3, AWS Transcribe (STT) |
| API 문서 | Springdoc OpenAPI(Swagger) |
| 배포 | AWS EC2, Nginx, Let's Encrypt(sslip.io), GitHub Actions CI/CD |
| 테스트 | JUnit5, Mockito, H2 |

## 팀 구성 및 담당 도메인


| 도메인 | 담당 | 설명 |
|---|---|---|
| Auth | [@wanimetro](https://github.com/wanimetro) | 회원가입/로그인/세션 인증 |
| Project | [@tthungjun](https://github.com/tthungjun) | 프로젝트/초대/멤버 관리 |
| Meeting | [@najunho04](https://github.com/najunho04) | 회의록/파일 업로드/STT 처리 |
| AI/Stage | [@xihxxn](https://github.com/xihxxn) | AI 문서 생성/단계 문서 관리 |
| **Wireframe** | **[@kwang-i-coder](https://github.com/kwang-i-coder)** | **와이어프레임 생성 및 재생성 요청 API** |
| Realtime/Export | [@Sjaize](https://github.com/Sjaize) | WebSocket 실시간 이벤트/Markdown Export |


## 실행 방법

### 요구 사항

- Java 21
- PostgreSQL (로컬 실행 시)
- Gemini API Key (없으면 로컬 환경에서는 자동으로 Mock AI 클라이언트가 동작합니다)

### 환경 변수

`.env.example`을 참고해 `.env` 파일을 작성합니다.

```bash
cp .env.example .env
```

### 로컬 실행

```bash
./gradlew bootRun
```

`spring.profiles.default`가 `local`인 환경에서는 Gemini API 키 없이도 `MockAiDocumentClient`가 고정된 응답을 반환하므로, 전체 플로우(생성 → 확정 → 재생성)를 API 키 없이 테스트할 수 있습니다.

### 테스트

```bash
./gradlew test
```

H2 인메모리 DB로 실행되며, 별도의 PostgreSQL 설정 없이 동작합니다.

## 배포 아키텍처

도메인 구매 없이 AWS EC2(백엔드) + Vercel(프론트엔드)로 배포했습니다.

- **백엔드**: EC2에 Nginx 리버스 프록시 + Spring Boot(systemd), sslip.io로 무료 HTTPS 적용
- **프론트엔드**: Vercel, `vercel.json` rewrite로 API 요청을 EC2로 프록시(동일 출처 처리)
- **CI/CD**: `main` 브랜치 push 시 GitHub Actions가 빌드 후 EC2에 자동 배포
- **WebSocket 인증**: 크로스도메인 환경에서 세션 쿠키가 전달되지 않는 브라우저 정책을 우회하기 위해, 로그인 시 발급되는 세션 토큰을 쿼리 파라미터로 추가 전달하는 방식을 병행
