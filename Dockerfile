FROM java:8
ADD swagger-manage-server.jar app.jar
ENTRYPOINT ["java","-Xmx512m","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
