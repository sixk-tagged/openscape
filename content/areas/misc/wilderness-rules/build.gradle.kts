plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    implementation(projects.api.scriptAdvanced)
    integrationImplementation(libs.rsprot.api)
    integrationImplementation(projects.api.player)
    integrationImplementation(projects.engine.annotations)
}
