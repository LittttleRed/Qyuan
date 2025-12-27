## 构建阶段：使用 Maven 和 OpenJDK 17
FROM maven:3.8-openjdk-17-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制父 pom.xml 和当前模块及依赖模块的 pom.xml
COPY pom.xml /app/
COPY qyuan-social/pom.xml /app/qyuan-social/
COPY qyuan-common/pom.xml /app/qyuan-common/

# 复制源代码
COPY qyuan-social/src /app/qyuan-social/src
COPY qyuan-common/src /app/qyuan-common/src

# 构建应用（跳过测试），只打包 qyuan-social 模块
RUN mvn clean package -DskipTests -pl qyuan-social -am

## 运行阶段：使用 JRE 运行
FROM eclipse-temurin:17-jre

# 设置时区
ENV TZ=Asia/Shanghai

# 设置工作目录
WORKDIR /app

# 从构建阶段复制打包好的 JAR 文件
COPY --from=builder /app/qyuan-social/target/*.jar app.jar

# 暴露端口（qyuan-social 使用 9001 端口）
EXPOSE 9001

# JVM 参数（可在部署时通过环境变量覆盖）
ENV JAVA_OPTS=""

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
