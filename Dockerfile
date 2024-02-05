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