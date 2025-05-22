FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN ["./gradlew", "shadowJar", "--no-daemon"]

COPY config config
COPY src src

RUN ["./gradlew", "--no-daemon", "build"]

ENV JAVA_OPTS="-Xmx512M -Xms512M"
EXPOSE 7070

CMD ["java", "-jar", "app.jar"]