FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build

COPY backend/gradlew backend/gradlew.bat backend/settings.gradle.kts backend/build.gradle.kts ./
COPY backend/gradle ./gradle

RUN mkdir -p ../shared/proto
COPY shared/proto ../shared/proto
COPY backend/src ./src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN groupadd -r app && useradd -r -g app app && \
    apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

COPY --from=builder /build/build/libs/backend.jar app.jar

RUN chown app:app app.jar

USER app

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
    CMD curl -sf http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
