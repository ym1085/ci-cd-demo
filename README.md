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

# 02. 🔥 내용 정리

> 1. 간단한 REST API 생성  
> 2. Dockerfile 작성  
> 3. Github Action Script 작성(CI/CD)  
> 4. EC2 생성 후 배포 진행

## 02-1. TEST REST API 구성

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

## 02-2. Dockerfile 구성

```dockerfile
# 베이스 이미지
FROM openjdk:17-jdk-slim

# WORK DIR 지정
WORKDIR /apps

# 이미지 빌드 시 --build-args 로 넘길 인자
ARG JAR_FILE=build/libs/*.jar

# 이미지 혹은 파일을 도커 이미지의 파일 시스템으로 복사
COPY ${JAR_FILE} app.jar

# 노출 포트
EXPOSE 8080

# 이미지를 기반으로 컨테이너를 띄울 때 항상 실행되어야 하는 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> 🐳 Dockerfile은 Docker 상에서 작동시킬 컨테이너의 구성 정보를 기술하기 위한 청사진이라 할 수 있다.  
> 기본적으로 docker build 명령어 + Dockerfile을 통해 docker image를 생성한다

### FROM openjdk:17-jdk-slim
 
```dockerfile
FROM openjdk:17-jdk-slim
```

- 베이스 도커 이미지를 설정하는 경우 사용
- 즉, 어떤 이미지를 기반으로 사용할지 지정하는 것
- 
### WORKDIR /apps

```dockerfile
WORKDIR /apps
```

- 작업 디렉토리 지정 시 사용 
- 여기서는 /apps 경로를 작업 디렉토리로 지정한다

### ARG [변수명]=[값]

```dockerfile
ARG JAR_FILE=build/libs/*.jar
```

- build 되는 시점에만 사용되는 변수
- `Dockerfile에서만 사용 가능`
- ARG [key]=[value] 로 사용 가능
- docker build 시 --build-arg 옵션을 사용해 오버라이딩 가능
  - 기존 ARG 옵션 무시

### ENV

```dockerfile
ENV TEST AAA
ENV TEST2=BBB
```

- ENV는 Docker 컨테이너 안에서 사용되는 환경 변수
- ARG는 build 시점에만(Dockerfile) 사용이 되고, ENV는 컨테이너 안에서 사용
- ENV [key] [value] or ENV [key]=[value] 로 사용 가능

### COPY

```dockerfile
COPY ${JAR_FILE} app.jar
```

```dockerfile
COPY ./local_directory /container_directory
```

- 호스트 OS의 `파일` 또는 `디렉토리`를 컨테이너 안의 경로로 복사
- 호스트 OS에서 컨테이너 안으로 복사만 가능

### ADD

```dockerfile
ADD http://example.com/big.tar.xz /container_directory
```

- ADD도 COPY와 마찬가지로 호스트 OS의 `파일` 혹은 `디렉토리`를 컨테이너 안의 경로로 복사하는건 동일하다
- 하지만 ADD는 COPY와 다르게 아래 기능을 제공한다
  - 원격 URL에서 파일을 다운로드 받아서 -> 컨테이너 안으로 복사한다
  - 파일이 gzip과 같은 압축 파일인 경우 압축을 풀어서 -> 컨테이너 안으로 복사한다
  - ADD는 COPY처럼 명확하지 않기 때문에 권장되는 방식이 아니며 파일 복사만 한다면 COPY를 사용하자

### EXPOSE

```dockerfile
EXPOSE 8080
```

```docker
# docker 실행 예시
docker run --name ci-cd-demo -d -p 80:8080 -rm youngmin1085/ci-cd-demo:latest  
```

- EXPOSE 명령어를 통해 `외부에 공개할 예정이다` 라고 명시 가능
- 이렇게 EXPOSE로 명시만 한다고 되는 것이 아니라 `docker run -p` 를 사용을 해서 매핑 해야 한다
- docker run -p [호스트 포트]:[컨테이너 포트] 이런식으로 매핑해야 의미가 있음
- 위와 같이 실행하면 `랜덤 호스트 포트` 가 `컨테이너 포트` 에 자동으로 매핑 된다

### RUN

```dockerfile
RUN pip install -r requirements.txt
```

- RUN은 Dockerfile로부터 docker image를 build 하는 순간에만 실행 되는 명령어
- 주로 라이브러리 설치를 하는 부분에 활용

### CMD

```dockerfile
CMD ["echo", "Hello, Docker!"]
```

```docker
# 오버라이딩 된 인자가 출력이 된다
docker run my-image echo "Overriding CMD"
```

- 컨테이너가 생성 -> 최초 실행 될 때 -> 기본적으로 실행할 명령어 지정
- docker run 명령을 실행할 때, 실행 관련 추가 인자를 제공하면 해당 인자가 CMD로 지정한 명령어를 오버라이딩 

### ENTRYPOINT

```dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- 컨테이너가 생성 -> 최초 실행 될 때 -> 기본적으로 실행할 명령어 지정
- docker run 명령어의 인자로 오버라이딩 불가능

> 🤔CMD VS ENTRYPOINT?  
> 결국은 둘다 컨테이너 생성이 되고 최초 실행 시 실행어되어야 하는 명령어를 실행하는 것을 동일하다  
> 하지만 CMD는 실행 시 사용자가 넣은 인자(컨테이너 환경 변수)에 따라서 CMD 명령어가 달라질 수 있는 반면  
> ENTRYPOINT는 [  ] 안에 기입되어 있는 명령어가 절대 변경이 되지 않고 실행이 되어야 하는 경우 사용한다

### MAINTAINER

```dockerfile
MAINTAINER = "youngmin1085@gmail.com"
```

- 개발자의 이름 혹은 이메일의 메타데이터 지정
- Dockerfile v1.13.0 부터 MAINTAINER은 더 이상 사용되지 않는다

### LABEL

```dockerfile
LABEL email="youngmin1085@gmial.com"
LABEL name="youngminkim"
LABEL version="1.0"
LABEL description="My Spring Application?"
```

- 개발자의 이름 혹은 이메일의 메타데이터 지정, KEY - VALUE 형식 사용
- Dockerfile은 하나 이상의 LABEL을 가질 수 있음

### VOLUME

# 99. 참고 자료

- [[Docker + Github Action] Docker + Github Actions로 SpringBoot CI/CD 구축하기](https://e-room.tistory.com/171)
- [[Docker] 김징어의 Devlog:티스토리](https://kimjingo.tistory.com/240)
- [[Docker] ADD vs COPY in Dockerfile](https://blog.leocat.kr/notes/2017/01/07/docker-add-vs-copy)
- [[Docker] docker run -p 옵션과 Dockerfile의 EXPOSE와의 차이](https://imkh.dev/docker-expose-ports)
- [[Docker] Dockerfile, MAINTAINER 사용중단](https://accordions.co.kr/it_trend/dockerfile-maintainer-%EC%82%AC%EC%9A%A9%EC%A4%91%EB%8B%A8/)