plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    integrationImplementation(projects.api.pluginCommons)
    integrationImplementation(projects.api.deathPlugin)
    integrationImplementation(projects.api.hitPlugin)
    integrationImplementation(projects.api.player)
    integrationImplementation(projects.api.repo)
}
