# 1단계: 빌드 스테이지
FROM gradle:8.2.1-jdk17 AS builder
# context가 demo이므로 현재 위치(.)의 모든 파일을 복사
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project

# Gradle 빌드 실행
RUN gradle build -x test

# 2단계: 실행 스테이지
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드된 jar 복사
COPY --from=builder /home/gradle/project/build/libs/*.jar /app/app.jar

# [중요] 토스 인증서 파일을 컨테이너 내부로 복사
# context가 demo이므로 src/main/resources에서 바로 가져옵니다.
COPY --from=builder /home/gradle/project/src/main/resources/toss-cert.p12 /app/toss-cert.p12

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
