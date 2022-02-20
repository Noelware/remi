# 🧶 Remi
> *Library to handling files for persistent storage with Google Cloud Storage and Amazon S3-compatible server, made in Kotlin!*

## Why is this built?
This was built in the mind of [Arisu](https://arisu.land) and [helm-server](https://charts.noelware.org) to store any piece of
data into different providers without repeating code and so on, and so forth. This is a simple library to take care of that!

We support any Amazon S3 compatible servers, Google Cloud Storage, and the local filesystem!

The library is split into five modules:

- [remi-bom](./bom) - Bill of Materials for Remi
- [remi-core](./core) - Core abstractions that is used for the storage units.
- [remi-support-fs](./support-fs) - Support for using the local filesystem
- [remi-support-gcs](./support-gcs) - Support for using Google Cloud Storage
- [remi-support-s3](./support-s3) - Support for using any compatible Amazon S3 service like Wasabi or MinIO

## Installation
> [:scroll: Documentation](https://docs.noelware.org/libraries/remi) | :eyes: v**0.0.1**

### Gradle
#### Kotlin DSL
```kotlin
repositories {
    // If you're using the Noel Gradle Utils package, you can use the
    // `noelware` extension
    maven {
        url = uri("https://maven.noelware.org")
    }
}

dependencies {
    // If you're using the Noel Gradle Utils package, you can use
    // the `noelware` extension to automatically prefix `org.noelware.<module>`
    // in the dependency declaration
    implementation("org.noelware.remi:remi-<module_name>:<version>")
}
```

### Groovy DSL
```groovy
repositories {
    maven {
        url "https://maven.noelware.org"
    }
}

dependencies {
    implementation "org.noelware.remi:remi-<module_name>:<version>"
}
```

### Maven
Declare the **Noelware** Maven repository under the `<repositories>` chain:

```xml
<repositories>
    <repository>
        <id>noelware-maven</id>
        <url>https://maven.noelware.org</url>
    </repository>
</repositories>
```

Now declare the dependency you want under the `<dependencies>` chain:

```xml
<dependencies>
    <dependency>
        <groupId>org.noelware.remi</groupId>
        <artifactId>remi-{{NAME}}</artifactId>
        <version>{{VERSION}}</version>
        <type>pom</type>
    </dependency>
</dependencies>
```

## License
**remi** is released under the [Apache 2.0](/LICENSE) License by **Noelware**, read the **LICENSE** file in the
[root repository](https://github.com/Noelware/remi/blob/master/LICENSE) for more information.
