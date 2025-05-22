# Этап сборки
FROM eclipse-temurin:21-jdk as builder

WORKDIR /app

# Копируем только необходимые для сборки файлы
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

# Собираем проект (включая зависимости)
RUN ./gradlew shadowJar --no-daemon

# Финальный образ
FROM eclipse-temurin:21-jre

WORKDIR /app

# Копируем только собранный JAR из этапа builder
COPY --from=builder /app/build/libs/app.jar .

# Открываем порт
EXPOSE 7070

# Запускаем приложение
CMD ["java", "-jar", "app.jar"]