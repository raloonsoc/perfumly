# syntax=docker/dockerfile:1

# --- Build stage ---
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /build

# Cache dependencies in their own layer, separate from source changes
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -B clean package -DskipTests \
    && mv target/*.jar target/app.jar

# --- Runtime stage ---
FROM eclipse-temurin:25-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=build --chown=spring:spring /build/target/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
