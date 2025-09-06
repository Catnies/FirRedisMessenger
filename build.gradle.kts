plugins {
    id("java")
    id("maven-publish") // Maven Publish
}

group = "top.catnies"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly ("org.jetbrains:annotations:24.0.0") // Jetbrains
    compileOnly("org.projectlombok:lombok:1.18.34") // Lombok
    annotationProcessor("org.projectlombok:lombok:1.18.34") // Lombok

    implementation("io.lettuce:lettuce-core:6.5.3.RELEASE") // Lettuce
    implementation("com.google.code.gson:gson:2.8.9") // Gson
    implementation("com.google.guava:guava:30.1-jre") // Guava
}

// 发布到 Maven 仓库
publishing {
    repositories {
        maven {
            name = "Catnies"
            url = uri("https://repo.catnies.top/releases")
            credentials(PasswordCredentials::class)
            authentication { create<BasicAuthentication>("basic") }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "top.catnies"
            artifactId = "firredismessenger"
            version = "${rootProject.version}"
            from(components["java"])
        }
    }
}