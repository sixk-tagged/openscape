plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(libs.rsprot.api)
    implementation(projects.api.pluginCommons)
    integrationImplementation(projects.api.deathPlugin)
}
