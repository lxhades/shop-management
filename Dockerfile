FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/target/shop-management-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
