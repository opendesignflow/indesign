// Versions
//-----------------
var scalaMajorVersion by extra("2.13")
var scalaMinorVersion by extra("6")
val scalaVersion by extra {
    "$scalaMajorVersion.$scalaMinorVersion"
}

var ooxooVersion by extra("4.0.5-SNAPSHOT")

// Project version
var lib_version by extra("2.0.1-SNAPSHOT")
var branch by extra { System.getenv("BRANCH_NAME") }
if (System.getenv().getOrDefault("BRANCH_NAME", "dev").contains("release")) {
    lib_version = lib_version.replace("-SNAPSHOT", "")
}
println("Version is $lib_version")



allprojects {

    // Toolchain


    // Name + version
    group = "org.odfi.indesign"
    version = lib_version

    var scalaMajorVersion by extra("3")
    var scalaMinorVersion by extra("0.2")
    val scalaVersion by extra {
        "$scalaMajorVersion.$scalaMinorVersion"
    }

    repositories {

        mavenLocal()
        mavenCentral()
        maven {
            name = "Sonatype Nexus Snapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            name = "ODFI Releases"
            url = uri("https://www.opendesignflow.org/maven/repository/internal/")
        }
        maven {
            name = "ODFI Snapshots"
            url = uri("https://www.opendesignflow.org/maven/repository/snapshots/")
        }
        maven {
            url = uri("https://repo.triplequote.com/libs-release/")
        }
        google()
    }
}