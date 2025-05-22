FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ["./gradlew", "shadowJar", "--no-daemon"]

RUN ["./gradlew", "--no-daemon", "build"]

ENV JAVA_OPTS="-Xmx512M -Xms512M"
EXPOSE 7070

CMD ["java", "-jar", "app.jar"]