FROM eclipse-temurin:21-jre
WORKDIR /app
COPY langhuan-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9077
ENTRYPOINT ["java", "--add-modules", "jdk.incubator.vector", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]