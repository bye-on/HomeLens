# 1단계: Maven 빌드
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 의존성 먼저 캐싱
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 실제 소스 복사 후 빌드
COPY src ./src

# 테스트는 보통 빌드에서 스킵
RUN mvn -B package -DskipTests

# 2단계: JRE만 담은 런타임 이미지
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# target 에 생성된 jar 복사
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 80

ENTRYPOINT ["java", "-jar", "app.jar"]
