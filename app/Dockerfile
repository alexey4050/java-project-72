FROM gradle:8.12.1-jdk21

WORKDIR /app

COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .

RUN ./gradlew --no-daemon dependencies

COPY . .

RUN ./gradlew --no-daemon build

CMD ["java", "-jar", "app/build/libs/app-1.0-SNAPSHOT-all.jar"]