> [!NOTE]
>
> Java version of Remi is under maintenance mode and if you wish to maintain the library, then contact Noel on Twitter ([@auguuwu](https://twitter.com/auguuwu)).
> This library will be unmaintained and archived by December 15th, 2023 if no maintainers want to maintain it.
>
> **NEW (11/12/2025)**: Remi will now be maintained once again! Starting in early 2026, Remi will be revamped to follow up standards in Java now.

### 🧶 Remi | Java
#### *Robust, simple Java 21 library to handle object storage communication*

**Remi** is a simple and robust Java 21 library to handle object storage communication with a unified API. Originally, this library was unmaintained from December 2023 to November 2025, where it is being revamped and probably faster than ever.

> [!WARNING]
> **Remi** is still experimental software! Things can break and API stability is not guaranteed.

## Supported Providers
| Name                         | Maven Coordinate                   |
| :--------------------------- | :--------------------------------- |
| Amazon S3                    | `org.noelware.remi:storage-s3`     |
| Local                        | `org.noelware.remi:storage-fs`     |
| Google Cloud Storage         | `org.noelware.remi:storage-gcs`    |
| Microsoft Azure Blob Storage | `org.noelware.remi:storage-azure`  |
| MongoDB GridFS               | `org.noelware.remi:storage-gridfs` |

If you wish, you can also provide your own community-built package with the [`org.noelware.remi:storage-core`](https://maven.noelware.org/-/org.noelware.remi/storage-core) Maven package, which you can read the documentation on [`docs.noelware.org`](https://docs.noelware.org/libraries/java/remi) for more information.

## Usage
Refer to the [Installation](#installation) section on how to integrate **Remi** into your own toolchain (Gradle, Maven, Bazel).

This example uses the local filesystem's storage service.

```java
/* TODO: this */
```

## Installation
### Gradle [Kotlin DSL]
```kotlin
repositories {
    maven("https://maven.noelware.org")
    mavenCentral()
}

dependencies {
    implementation("org.noelware.remi:remi-[package]:[version]")
}
```

### Gradle [Groovy DSL]
```groovy
repositories {
    maven "https://maven.noelware.org"
    mavenCentral()
}

dependencies {
    implementation "org.noelware.remi:remi-[package]:[version]"
}
```

### Maven
```xml
<repositories>
    <repository>
        <url>https://maven.noelware.org</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <group>org.noelware.remi</group>
        <artifactId>remi-[package]</artifactId>
        <version>[version]</version>
        <type>pom</type>
    </dependency>
</dependencies>
```

### Bazel
#### `MODULE.bazel`
```python
bazel_dep(name = "remi", version = "<version>")
git_override(
  module_name = "remi",
  repository = "https://github.com/Noelware/remi.git",
  commit = "<pinned commit here>"
)
```

### Downloading from Noelware's Maven Repository
You can download the libraries yourself with `curl` or `wget` and link them with **javac**. You can download with the specific URL:

```http
GET https://maven.noelware.org/repo/org/noelware/remi/remi-[package]/[version]/remi-[package]-[version]-sources.jar
```

## License
**Remi** is released with the [**MIT License**](https://github.com/Noelware/remi/blob/master/LICENSE) with love by Noelware. <3
