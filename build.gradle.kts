plugins {
    id("java")
    id("maven-publish")
}

group = "me.noahvdaa"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "schemashift"

            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "bytecodespace"

            val releasesRepoUrl = uri("https://repo.bytecode.space/repository/maven-releases/")
            val snapshotsRepoUrl = uri("https://repo.bytecode.space/repository/maven-snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            credentials(PasswordCredentials::class)
        }
    }
}
