plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    integrationImplementation(projects.api.death)
    integrationImplementation(projects.api.hitPlugin)
    integrationImplementation(projects.api.npc)
    integrationImplementation(projects.api.player)
}
