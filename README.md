# Gradle Test Synchronization Plugin

A gradle plugin that provides a way to synchronize tests between different gradle modules.

Consider following project structure:

```
root
  build.gradle
  settings.gradle
  +-- module1
    build.gradle
  +-- module2
    build.gradle
```

Both modules have API tests that call some external service.
You want to build/tests these modules in parallel (with gradle property `org.gradle.parallel=true`),
but external API has a rate limit that causes tests to fail when run in parallel.  
This plugin aims to solve exactly this problem.

### Usage

TODO add usage examples after published plugin is approved

### Known limitations

- Currently, only JUnit 5 is supported
- Synchronization is possible only by using Junit tags (`org.junit.jupiter.api.Tag`).
- The plugin was tested with latest Gradle 8x, 7x, 6x versions (8.10.2, 7.6.1, 6.9.4). Any other version is not
  guaranteed to work.

If you have any issues or feature requests, please don't hesitate to create an issue.

### License

This project is distributed under the [MIT License](LICENSE).
