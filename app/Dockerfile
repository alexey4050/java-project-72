FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN ./gradlew shadowJar

RUN cp /app/build/libs/app.jar /app/app.jar

EXPOSE 7070

CMD ["java", "-jar", "build/libs/app-1.0-SNAPSHOT-all.jar"]