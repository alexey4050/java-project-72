plugins {
    checkstyle
    application
    id("io.freefair.lombok") version "8.13.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.sonarqube") version "6.2.0.5505"
    jacoco
}

application {
    mainClass = "hexlet.code.App"
    applicationName = "app"
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("io.javalin:javalin:6.6.0")
    implementation("io.javalin:javalin-bundle:6.6.0")
    implementation("io.javalin:javalin-rendering:6.6.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")

    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("com.h2database:h2:2.3.232")
    implementation("org.postgresql:postgresql:42.7.3")

    compileOnly("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")

    implementation("gg.jte:jte:3.2.1")

    implementation("com.konghq:unirest-java:3.14.5")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")

    implementation("org.jsoup:jsoup:1.20.1")

}

sonar {
    properties {
        property("sonar.projectName", "app")
        property("sonar.projectKey", "alexey4050_java-project-72")
        property("sonar.organization", "alexey4050")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.login", System.getenv("SONAR_TOKEN") ?: "")
        property("sonar.java.binaries", "build/classes")
        property("sonar.java.coverage.jacoco.xmlReportPaths",
            "build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.junit.reportPaths", "build/test-results/test")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.verbose", "true")
    }
}

tasks.test {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}