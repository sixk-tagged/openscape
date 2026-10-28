plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    implementation(projects.content.skills.production)
    integrationImplementation(projects.api.pluginCommons)
    integrationImplementation(projects.api.player)
    integrationImplementation(projects.content.skills.production)
}
