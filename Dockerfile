# Backend AquaSample (Spring Boot + H2) para Render
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY backend backend

RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew \
    && ./gradlew -p backend installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd --system --uid 1001 aquasample \
    && mkdir -p /app/data /app/uploads \
    && chown -R aquasample:aquasample /app

COPY --from=build --chown=aquasample:aquasample \
    /app/backend/build/install/aquasample-backend/ ./

USER aquasample

ENV PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

EXPOSE 8080

CMD ["sh", "-c", "bin/aquasample-backend --server.port=${PORT:-8080}"]
