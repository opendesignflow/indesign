pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven {
            name = "ODFI Releases"
            url = java.net.URI("https://www.opendesignflow.org/maven/repository/internal/")
        }
        maven {
            name = "ODFI Snapshots"
            url = java.net.URI("https://www.opendesignflow.org/maven/repository/snapshots/")
        }
    }


}


// Projects
//-------------
rootProject.name = "indesign"
include(":indesign-core")
include(":indesign-stdplatform")
