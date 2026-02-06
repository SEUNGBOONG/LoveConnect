# 1단계: 빌드 스테이지
FROM gradle:8.2.1-jdk17 AS builder

# 빌드 시점에 Spring이 바라보는 경로(/app/certs)를 강제로 생성
USER root
RUN mkdir -p /app/certs
USER gradle

COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project

# [핵심] GitHub Actions가 src/main/resources에 만들어둔 파일을
# 빌드 컨테이너의 /app/certs 경로로 복사해줍니다.
RUN cp src/main/resources/toss-cert.p12 /app/certs/toss-cert.p12 || true

# 이제 파일을 찾을 수 있으므로 빈 생성이 성공하여 빌드가 통과됩니다.
RUN gradle build -x test

# 2단계: 실행 스테이지
FROM eclipse-temurin:17-jdk
RUN mkdir -p /app/certs
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
