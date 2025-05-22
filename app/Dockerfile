FROM gradle:7.4.0-jdk17

WORKDIR /app

COPY /app .

RUN RUN ./gradlew --no-daemon build

CMD ["java", "-jar", "build/libs/HexletJavalin-1.0-SNAPSHOT-all.jar"]