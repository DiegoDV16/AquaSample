# Backend AquaSample (Spring Boot + H2)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew -p backend installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/backend/build/install/aquasample-backend/ ./
EXPOSE 8080
CMD ["sh", "-c", "bin/aquasample-backend --server.port=${PORT:-8080}"]
