# --- Stage 1: Build (Dùng Maven và Java 21) ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy file cấu hình và tải thư viện trước (để tận dụng Docker cache)
COPY pom.xml .
# Lệnh này giúp tải dependency mà không cần copy toàn bộ code (tiết kiệm thời gian build lại)
RUN mvn dependency:go-offline -B

# Copy toàn bộ source code và build
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Run (Dùng JRE 21 nhẹ nhất để chạy) ---
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy file .jar đã build từ Stage 1 sang Stage 2
COPY --from=build /app/target/*.jar app.jar

# Mở port 8080
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]