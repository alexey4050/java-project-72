.PHONY: build

checkstyleMain:
	./app/gradlew -p ./app checkstyleMain

checkstyleTest:
	./app/gradlew -p ./app checkstyleTest

clean:
	./app/gradlew -p ./app clean

build:
	./app/gradlew -p ./app installDist

test:
	./app/gradlew -p ./app build

report:
	./app/gradlew -p ./app jacocoTestReport

run:
	./app/build/install/app/bin/app