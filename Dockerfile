FROM gradle:8.9-jdk17 AS builder

WORKDIR /build
COPY . .

RUN gradle bootJar --no-daemon --no-build-cache

FROM eclipse-temurin:17-jdk-jammy

ENV TZ=Asia/Seoul

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
