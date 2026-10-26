plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    implementation(projects.api.death)
    implementation(projects.api.random)
    implementation(projects.content.other.specialWeapons)
    integrationImplementation(projects.api.pluginCommons)
    integrationImplementation(projects.api.player)
    integrationImplementation(projects.api.death)
    integrationImplementation(projects.api.hitPlugin)
    integrationImplementation(projects.api.random)
    integrationImplementation(projects.api.combat.combatCommons)
    integrationImplementation(projects.api.combat.combatFormulas)
    integrationImplementation(projects.api.combat.combatManager)
    integrationImplementation(projects.api.combat.combatWeapon)
    integrationImplementation(projects.api.weapons)
    integrationImplementation(projects.content.other.specialWeapons)
}
