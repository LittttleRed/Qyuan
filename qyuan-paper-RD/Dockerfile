# 构建阶段：使用Maven和OpenJDK 17
FROM maven:3.8-openjdk-17-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 构建应用（跳过测试）
RUN mvn clean package -DskipTests

# 运行阶段：使用JRE运行
FROM eclipse-temurin:17-jre

# 设置工作目录
WORKDIR /app

# 从构建阶段复制JAR文件
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口（Spring Boot默认8080）
EXPOSE 8080

# 设置JVM参数（可根据需要调整）
ENV JAVA_OPTS=""

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]