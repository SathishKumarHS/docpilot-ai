FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN groupadd -r app && useradd -r -g app app && \
    apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

COPY build/libs/backend.jar app.jar

RUN chown app:app app.jar

USER app

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
    CMD curl -sf http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
