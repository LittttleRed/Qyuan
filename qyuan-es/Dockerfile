# 构建阶段：使用Maven和OpenJDK 17
FROM maven:3.8-openjdk-17-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制父pom.xml和当前模块的pom.xml
COPY pom.xml /app/
COPY qyuan-es/pom.xml /app/qyuan-es/
COPY qyuan-common/pom.xml /app/qyuan-common/

# 复制源代码
COPY qyuan-es/src /app/qyuan-es/src
COPY qyuan-common/src /app/qyuan-common/src

# 构建应用（跳过测试）
RUN mvn clean package -DskipTests -pl qyuan-es -am

# 运行阶段：使用JRE运行
FROM eclipse-temurin:17-jre

# 设置工作目录
WORKDIR /app

# 从构建阶段复制JAR文件
COPY --from=builder /app/qyuan-es/target/*.jar app.jar

# 暴露端口（qyuan-es使用9006端口）
EXPOSE 9006

# 设置JVM参数（可根据需要调整）
ENV JAVA_OPTS=""

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

