FROM eclipse-temurin:17-jre

WORKDIR /app

# 将打包好的 jar 拷贝进镜像
COPY target/*.jar app.jar

ENV TZ=Asia/Shanghai

# Nacos & Spring 配置通过环境变量传入
ENV NACOS_IP=127.0.0.1 \
    NACOS_PORT=8848 \
    NACOS_NAMESPACE=public \
    SPRING_PROFILES_ACTIVE=dev

EXPOSE 9001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
