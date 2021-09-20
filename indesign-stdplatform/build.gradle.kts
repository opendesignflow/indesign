val ooxooVersion : String by rootProject.extra

plugins {
    // Scala
    // Apply the java plugin to add support for Java
    id("scala")
    id("org.odfi.ooxoo") version ("4.0.1")
    id("org.openjfx.javafxplugin") version "0.0.10"

    // Publish
    id("maven-publish")
    id("java-library")

}

// Sources
//-------------------
sourceSets {
    main {
        scala {
            // Generated from ooxoo
            srcDir(File(getBuildDir(), "generated-sources/scala"))
        }
    }

}

// Java/Scala Settings
//----------------------

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

// Scala compilation options
tasks.withType<ScalaCompile>().configureEach {
    scalaCompileOptions.additionalParameters = listOf("-rewrite", "-source", "3.0-migration")
}

// Dependencies
//-------------------
javafx {
    version = "18-ea+2"
    modules(
        "javafx.controls",
        "javafx.fxml",
        "javafx.graphics",
        "javafx.media", "javafx.web", "javafx.swing"
    )
}


dependencies {


    // Core Project
    //-----------
    api(project(":indesign-core"))

    // Dependencies
    //------------

    listOf("aether-transport-file", "aether-util", "aether-transport-http", "aether-connector-basic").forEach {
        api("org.eclipse.aether:$it:1.1.0")
    }
    listOf("lucene-suggest", "lucene-queries", "lucene-queryparser").forEach {
        api("org.apache.lucene:$it:6.0.0")
    }

    api("org.apache.maven:maven-embedder:3.8.1")
    api("javax.mail:mail:1.4.7")

    // Test
    //----------------

    /*testImplementation "org.scala-lang.modules:scala-xml_$scala_major:2.0.0-M3".toString()
    testImplementation "org.scalatest:scalatest-funsuite_$scala_major:3.2.6".toString()
    testImplementation "org.scalatest:scalatest-shouldmatchers_$scala_major:3.2.6".toString()*/
}


publishing {
    publications {

        create<MavenPublication>("maven") {
            artifactId = "indesign-stdplatform"
            from(components["java"])

            pom {

                name.set("Indesign Standard Utilities")
                description.set("Indesign Core module")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("richnou")
                        name.set("Richnou")
                        email.set("leys.richard@gmail.com")
                    }
                }
            }
        }

    }
    repositories {
        maven {

            // change URLs to point to your repos, e.g. http://my.org/repo
            var releasesRepoUrl = uri("https://www.opendesignflow.org/maven/repository/internal/")
            var snapshotsRepoUrl = uri("https://www.opendesignflow.org/maven/repository/snapshots")

            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            // Credentials
            //-------------
            credentials {
                username = System.getenv("PUBLISH_USERNAME")
                password = System.getenv("PUBLISH_PASSWORD")
            }
        }
    }
}





