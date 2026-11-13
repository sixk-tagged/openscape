plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    implementation(projects.content.interfaces.equipment)
    implementation(projects.content.skills.herblore)
    integrationImplementation(projects.api.pluginCommons)
    integrationImplementation(projects.api.player)
}
