plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    implementation(projects.api.specials)
    implementation(projects.api.combat.combatCommons)
    implementation(projects.api.combat.combatFormulas)
    implementation(projects.api.combat.combatWeapon)
    integrationImplementation(projects.api.pluginCommons)
    integrationImplementation(projects.api.specials)
    integrationImplementation(projects.api.player)
    integrationImplementation(projects.api.hitPlugin)
    integrationImplementation(projects.api.combat.combatCommons)
    integrationImplementation(projects.api.combat.combatFormulas)
    integrationImplementation(projects.api.combat.combatWeapon)
}
