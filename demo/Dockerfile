# 1단계: 빌드 스테이지
FROM gradle:8.2.1-jdk17 AS builder

# 소스 복사 및 권한 설정
COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project

# 빌드 실행 (테스트 제외)
RUN gradle build -x test

# 2단계: 실행 스테이지
FROM eclipse-temurin:17-jdk

# 작업 디렉토리 설정
WORKDIR /app

# 1단계에서 빌드된 jar 파일 복사
COPY --from=builder /home/gradle/project/build/libs/*.jar /app/app.jar

# [중요] 프로젝트 리소스에 있는 인증서를 컨테이너 내부의 정해진 위치로 직접 복사
# 이렇게 하면 권한 문제나 파일 깨짐 없이 가장 확실하게 들어갑니다.
COPY src/main/resources/toss-cert.p12 /app/toss-cert.p12

# 컨테이너 실행 명령어
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
