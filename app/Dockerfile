# Этап сборки
FROM eclipse-temurin:21-jdk as builder

WORKDIR /app

# 1. Сначала копируем только файлы для загрузки зависимостей
COPY ./gradlew .
COPY ./gradle ./gradle
COPY ./build.gradle.kts .
COPY ./settings.gradle.kts .

# 2. Скачиваем зависимости (кешируемый слой)
RUN ./gradlew --version

# 3. Копируем исходный код
COPY ./src ./src

# 4. Собираем проект
RUN ./gradlew shadowJar --no-daemon

# Финальный образ
FROM eclipse-temurin:21-jre

WORKDIR /app

# Копируем собранный JAR
COPY --from=builder /app/build/libs/app.jar .

EXPOSE 7070

CMD ["java", "-jar", "app.jar"]
