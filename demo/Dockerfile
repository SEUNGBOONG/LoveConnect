# 1단계: 빌드 스테이지
FROM gradle:8.2.1-jdk17 AS builder
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project

# Gradle 빌드 (application.properties와 p12가 src/main/resources에 있어야 함)
RUN gradle build -x test

# 2단계: 실행 스테이지
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드된 jar 복사
COPY --from=builder /home/gradle/project/build/libs/*.jar /app/app.jar

# [중요] 토스 인증서 파일을 application.properties에 설정한 경로(/app/toss-cert.p12)로 복사
COPY --from=builder /home/gradle/project/src/main/resources/toss-cert.p12 /app/toss-cert.p12

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
