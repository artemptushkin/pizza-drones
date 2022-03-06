pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = "pizza-drones"

include("drone-client", "tower-server")
