plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    implementation(projects.content.other.lightSources)
    integrationImplementation(projects.api.player)
    integrationImplementation(projects.engine.annotations)
    integrationImplementation(projects.content.other.lightSources)
}
