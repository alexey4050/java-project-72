FROM gradle:8.12.1-jdk21

WORKDIR /app

COPY app .

RUN ["./gradlew", "clean", "build"]

CMD ["java", "-jar", "app/build/libs/app-1.0-SNAPSHOT-all.jar"]