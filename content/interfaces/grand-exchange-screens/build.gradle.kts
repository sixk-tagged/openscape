plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.grandExchange)
    implementation(projects.api.pluginCommons)
    implementation(projects.content.other.grandExchangeExtras)
    integrationImplementation(projects.api.grandExchange)
    integrationImplementation(projects.api.pluginCommons)
    integrationImplementation(projects.api.player)
    integrationImplementation(projects.content.other.grandExchangeExtras)
}
