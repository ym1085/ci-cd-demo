# CI-CD-Demo

> 😃해당 리포지토리는 Github Action을 사용하여 간단한 CI/CD 파이프라인을 구성하기 위한 리포지토리입니다.  
> 개인 실습용으로 생성 하였기에 잘못된 부분이 존재할 수 있습니다, 참고 해주세요.  
> 추가적으로 Github Action에 대한 부분은 생략하였으니 소스를 참고 해주세요!

# 01. 🔨 사용 스택

| 기술              | 버전            |
|-----------------|---------------|
| Java            | 17            |
| Spring Boot     | 3.2.2         |
| Docker          | 23.0.5        |
| AWS EC2         | Amazon Linux 2 |
| AWS Code Deploy | X             |

# 02. 🔥 흐름 정리

> 1. 간단한 REST API 생성  
> 2. Dockerfile 작성  
> 3. Github Action Script 작성(CI/CD)  
> 4. EC2 생성 후 배포 진행

# 03. TEST REST API 구성

```java
@Slf4j
@RestController
public class CiCdController {

    @Value("${app.version:1.0.0}")
    private String version;

    @GetMapping("/")
    public ResponseEntity<Response> init(
            @RequestHeader Map<String, String> headerMap,
            @RequestHeader("host") String host) {

        log.info("Hello CICD! version = {}", version);
        log.info("headerMap = {}, host = {}", headerMap, host);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Response.builder()
                        .version(version)
                        .host(host)
                        .build()
                );
    }

    @Data
    @Builder
    public static class Response {
        private final String version;
        private final String host;
    }
}
```

```json
{
  "version": "1.0.0",
  "host": "localhost:8080"
}
```

- 테스트를 위한 간단한 REST API 생성
- 다른건 없고 위와 같다 `app version` 과 `host` 정보를 반환한다

# 04. Dockerfile 관련 정리

- [Dockerfile 구성](./document/README.md)