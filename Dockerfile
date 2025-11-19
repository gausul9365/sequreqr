# ============
# BUILD PHASE
# ============
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ============
# RUN PHASE
# ============
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/secureqr-1.0.0.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
