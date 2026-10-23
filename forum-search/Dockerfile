# 运行镜像（先执行根目录 build-all.bat 完成打包：mvn clean package -Dmaven.test.skip=true）
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/forum-search-1.0-SNAPSHOT.jar app.jar
EXPOSE 8804
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
