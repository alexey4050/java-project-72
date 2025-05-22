FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ./gradlew --no-daemon dependencies

RUN ./gradlew --no-daemon build

EXPOSE 7070

CMD ["java", "-jar", "build/libs/app-1.0-SNAPSHOT-all.jar"]