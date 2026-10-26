plugins {
    id("base-conventions")
    id("integration-test-suite")
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(libs.bundles.logging)
    implementation(libs.fastutil)
    implementation(libs.guice)
    implementation(libs.kotlin.coroutines.core)
    implementation(projects.api.db)
    implementation(projects.api.parsers.jackson)
    implementation(projects.api.parsers.json)
    implementation(projects.api.realm)
    implementation(projects.api.serverConfig)
    implementation(projects.engine.game)
    implementation(projects.engine.map)
    implementation(projects.engine.module)
    implementation(projects.server.services)
    integrationImplementation(libs.fastutil)
    integrationImplementation(libs.guice)
    integrationImplementation(libs.kotlin.coroutines.core)
    integrationImplementation(projects.api.config)
    integrationImplementation(projects.api.db)
    integrationImplementation(projects.engine.game)
    integrationImplementation(projects.engine.map)
    integrationImplementation(projects.engine.module)
    integrationImplementation(projects.server.services)
}
