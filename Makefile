.PHONY: build checkstyleMain checkstyleTest clean test report run sonar

checkstyleMain: ./gradlew checkstyleMain

clean: ./gradlew clean

build: clean ./gradlew installDist

test:./gradlew test

run: build ./build/install/app/bin/app

sonar: build ./gradlew build sonar --info

report:./gradlew jacocoTestReport