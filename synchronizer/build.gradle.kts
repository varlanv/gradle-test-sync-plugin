plugins {
    `java-library`
    alias(libs.plugins.internalConvention)
}

dependencies {
    compileOnly(libs.junit.platform.launcher)
    compileOnly(projects.constants)
    testImplementation(projects.constants)
    testImplementation(libs.junit.platform.launcher)
}
