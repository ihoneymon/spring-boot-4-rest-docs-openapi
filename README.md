# Spring Boot 4 REST Docs + OpenAPI + Swagger UI

Spring Boot 4에서 테스트를 API 문서의 단일 소스로 삼는 예제 프로젝트입니다. 컨트롤러나 DTO에 Swagger/OpenAPI 애노테이션을 추가하지 않고 다음 결과물을 함께 만듭니다.

- Spring REST Docs 4 기반 정적 HTML 문서
- `restdocs-api-spec` 기반 OpenAPI 3.0.1 YAML
- 생성된 OpenAPI 명세를 읽는 Swagger UI

이 프로젝트는 컬리 기술 블로그의 [Spring REST Docs 가이드](https://helloworld.kurly.com/blog/spring-rest-docs-guide/)가 설명한 방식을 Spring Boot 4와 최신 호환 라이브러리에 맞춰 다시 구성한 예제입니다.

## 사용 버전

| 구성 요소 | 버전 |
|---|---:|
| Java | 25 |
| Spring Boot | 4.1.2 |
| Spring REST Docs | Spring Boot 4.1.2 의존성 관리 버전(4.x) |
| restdocs-api-spec | 0.20.2 |
| Swagger UI WebJar | 5.33.2 |
| Asciidoctor Gradle Plugin | 4.0.5 |
| Gradle Wrapper | 9.1.0 |

`restdocs-api-spec` 0.20.x부터 Spring Boot 4와 호환됩니다. 이 프로젝트는 Gradle Java Toolchain을 Java 25로 설정하므로 빌드와 실행에 JDK 25를 사용합니다.

## 빠르게 실행하기

```bash
sdk env
./gradlew clean build
java -jar build/libs/spring-boot-4-rest-docs-openapi-0.1.0.jar
```

SDKMAN을 사용하는 경우 [`.sdkmanrc`](.sdkmanrc)에 지정된 Java 25(`25.0.4-tem`)를 `sdk env`로 적용합니다. 해당 JDK가 설치되어 있지 않으면 먼저 `sdk env install`을 실행합니다. SDKMAN을 사용하지 않는 경우 JDK 25를 설치하고 `JAVA_HOME`과 `PATH`를 설정한 뒤 `sdk env`를 제외한 명령을 실행합니다.

애플리케이션 실행 후 다음 주소를 확인합니다.

| 결과물 | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| REST Docs HTML | http://localhost:8080/docs/index.html |
| OpenAPI YAML | http://localhost:8080/openapi/openapi3.yaml |

Swagger UI의 **Try it out** 기능으로 실행 중인 상품 API를 직접 호출할 수 있습니다.

## 문서만 생성하기

```bash
./gradlew clean docs
```

생성 위치는 다음과 같습니다.

```text
build/
├── generated-snippets/       # REST Docs 테스트가 만든 adoc, curl, HTTP 스니펫
├── docs/asciidoc/index.html  # Asciidoctor 정적 문서
└── api-spec/openapi3.yaml    # OpenAPI 3.0.1 명세
```

`bootJar`는 위 HTML과 YAML을 실행 가능 JAR의 정적 리소스에 포함합니다. 따라서 Swagger UI가 보는 명세와 REST Docs 문서는 테스트를 통과한 시점의 API 계약입니다.

## 구성 흐름

```text
MockMvc 문서화 테스트
      │
      ├── Spring REST Docs 스니펫 ──> Asciidoctor ──> REST Docs HTML
      │
      └── resource.json ─────────────> openapi3 ─────> OpenAPI YAML ──> Swagger UI
```

핵심은 [`ProductControllerDocsTest`](src/test/java/io/honeymon/example/apidocs/product/ProductControllerDocsTest.java)입니다. 각 요청과 응답의 필드, 경로 변수, 쿼리 파라미터를 테스트가 검증하면서 문서화합니다. 필드가 코드에서 변경됐지만 테스트의 문서 정의가 갱신되지 않으면 테스트가 실패합니다.

[`build.gradle`](build.gradle)은 다음 작업을 연결합니다.

- `test`: `build/generated-snippets`에 REST Docs 및 `resource.json` 스니펫 생성
- `asciidoctor`: 스니펫을 [`index.adoc`](src/docs/asciidoc/index.adoc)에 포함해 HTML 생성
- `openapi3`: 여러 `resource.json`을 하나의 OpenAPI YAML로 집계
- `bootJar`: HTML과 YAML을 JAR의 정적 리소스로 패키징
- `docs`: HTML과 OpenAPI 명세를 한 번에 생성하는 편의 작업

## 예제 API

```bash
# 상품 생성
curl -i -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"허니 버터","category":"FOOD","price":8500}'

# 상품 단건 조회
curl -i http://localhost:8080/api/products/1

# 카테고리별 상품 목록
curl -i 'http://localhost:8080/api/products?category=FOOD'
```

데이터는 예제의 초점을 문서화에 맞추기 위해 메모리에 저장하며 애플리케이션을 재시작하면 초기화됩니다.

## 기존 글의 구성과 달라진 점

- Spring Boot 2.x 대신 Spring Boot 4.1을 사용합니다.
- `javax.*`가 아닌 Jakarta 기반 API를 사용합니다.
- 제거된 `WebSecurityConfigurerAdapter` 방식은 사용하지 않습니다. 이 예제에는 Security 의존성 자체가 없습니다.
- Swagger UI 파일을 직접 복사·수정하지 않고 버전이 고정된 WebJar를 사용합니다.
- REST Docs용 `document`와 OpenAPI용 `document`를 두 번 호출하지 않고 `MockMvcRestDocumentationWrapper` 한 번으로 두 종류의 스니펫을 만듭니다.
- 생성된 문서와 명세를 `bootJar` 단계에서 패키징해 소스 디렉터리에 생성 파일을 남기지 않습니다.

## 운영 적용 시 고려사항

- Swagger UI의 **Try it out**은 실제 API를 호출합니다. 운영 환경 공개 여부와 인증·인가 정책을 별도로 결정하세요.
- 외부 도메인에서 API를 호출하게 할 때만 필요한 origin으로 CORS를 제한하세요. 문서 제공을 위해 CSRF나 보안 헤더 전체를 끄는 방식은 피해야 합니다.
- 문서 생성 테스트를 CI의 `build` 작업에 포함하고, 테스트를 건너뛴 오래된 명세가 배포되지 않게 하세요.
- 이 예제의 OpenAPI 생성기는 3.0.1을 출력합니다. OpenAPI 3.1/3.2 기능이 필수라면 생성 도구의 지원 범위를 먼저 확인하세요.

## 프로젝트 구조

```text
src/
├── docs/asciidoc/index.adoc
├── main/
│   ├── java/.../product/       # 애노테이션 없는 상품 API
│   └── resources/static/swagger-ui/index.html
└── test/java/.../ProductControllerDocsTest.java
```
