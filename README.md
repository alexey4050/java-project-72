### Hexlet tests and linter status:
[![Actions Status](https://github.com/alexey4050/java-project-72/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/alexey4050/java-project-72/actions)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alexey4050_java-project-72&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=alexey4050_java-project-72)
[![CI](https://github.com/alexey4050/java-project-72/actions/workflows/ci.yml/badge.svg)](https://github.com/alexey4050/java-project-72/actions/workflows/ci.yml)
[![Maintainability](https://qlty.sh/badges/61684631-6ee5-40f2-9350-c9c1dbc69496/maintainability.svg)](https://qlty.sh/gh/alexey4050/projects/java-project-72)
[![Code Coverage](https://qlty.sh/badges/61684631-6ee5-40f2-9350-c9c1dbc69496/test_coverage.svg)](https://qlty.sh/gh/alexey4050/projects/java-project-72)
[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=alexey4050_java-project-72)](https://sonarcloud.io/summary/new_code?id=alexey4050_java-project-72)

##  Java project 72

[![Java](https://img.shields.io/badge/Java-21-%23ED8B00.svg?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Gradle](https://img.shields.io/badge/Gradle-8.10-%2302303A.svg?logo=gradle&logoColor=white)](https://gradle.org/)
[![Javalin](https://img.shields.io/badge/Javalin-6.1.3-%23FF0000.svg?logo=java&logoColor=white)](https://javalin.io/)

### :rocket: Демо

Приложение доступно по адресу:

:point_right: [https://java-project-72-a3qj.onrender.com](https://java-project-72-a3qj.onrender.com)

### :package: Зависимости
- Java 21
- Gradle 8.10
- Javalin 6.1.3
- SLF4J 2.0.7

### :hammer_and_wrench: Установка и запуск
#### Локальная сборка
1. Клонируйте репозиторий:
```bash
git clone https://github.com/alexey4050/java-project-72.git
cd java-project-72
```
2. Соберите проект:

```
./gradlew installDist
```
3. Запустите приложение:

```
./build/install/app/bin/app
```
Приложение будет доступно по адресу: (http://localhost:7070)

## Презентация проекта "Анализатор страниц"
### Описание функционала
Проект представляет собой веб-приложение для анализа сайтов на SEO-пригодность. Основные возможности:
#### 1. Главная страница
![](file_presentation/Главная страница.png)

* Поле для ввода URL сайта для проверки
* Кнопка "Проверить" для запуска анализа
* Пример формата ввода URL

#### 2. Добавление сайта
![](file_presentation/Добавление сайта.png)
* После успешного добавления сайта отображается сообщение подтверждения
* Сайт добавляется в общий список с присвоением ID

#### 3. Детальная информация о сайте
![](file_presentation/Проверка сайта.png)
* Основная информация о сайте (ID, URL, дата создания)
* Возможность запустить проверку вручную
* Результаты последней проверки:
    * Код ответа сервера
    * Заголовок страницы (Title)
    * Заголовок H1
    * Мета-описание (Description)
    * Дата проверки