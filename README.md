# 🧶 Remi
> *Robust, and simple Java-based library to handle storage-related communications with different storage providers with Amazon S3, Google Cloud Storage, Azure Blob Storage,
> and the local filesystem support*
>
> <kbd><a href="https://github.com/Noelware/remi/releases/v0.5-beta">v0.5-beta</a></kbd>

**Remi** is a simple Java 17-based library to handle storage related communications with different storage providers. This is just an abstraction over
commonly used functions you might use (like `FilesystemStorageService.open("./path/to/thing")` to open a File's content). **Remi** is most commonly used with
[Hazel](https://noelware.org/hazel) to bring your files to the online web, [charted-server](https://charts.noelware.org) to host your Helm Charts together on the cloud,
and much more!

## Notice
**Remi** was made in Kotlin previously (before `v0.5-beta`'s release) and the Kotlin-related code is no longer compatible with the
new Java 17 API, which means you will have to migrate your code, which is fine! Here are the changes:

- The **MinIO** storage handler is no longer available with the v0.5-beta release, please migrate to the Amazon S3 package since it supports MinIO.
- All interfaces don't implement `suspend` functions anymore, all methods return synchronously by blocking the thread. This made it easier to port
  the whole Remi library easily.

## Supported Providers
- **Amazon S3**                                                 (via the `org.noelware.remi:remi-storage-s3` Maven coordinate)
- **Local Filesystem**                                          (via the `org.noelware.remi:remi-storage-fs` Maven coordinate)
- **Google Cloud Storage**                       [experimental] (via the `org.noelware.remi:remi-storage-gcs` Maven coordinate)
- **Azure Blob Storage**                         [experimental] (via the `org.noelware.remi:remi-storage-azure` Maven coordinate)

## Unsupported Providers
- **Oracle Cloud Infrastructure Object Storage**
- **Digital Ocean Spaces**
  - **Note**: You can use the S3 storage service since it has a S3-compatible API
- **Alibaba Cloud OSS Storage**
- **Tencent Cloud COS Storage**
- **OpenStack Object Storage**
- **Baidu Cloud BOS Storage**
- **Netease Cloud NOS Storage**

If you wish to provide community-built packages, you can do so with the [remi-storage-core](https://maven.noelware.org/-/org.noelware.remi/remi-storage-core) Maven package, which
you can read the documentation [here](https://remi.noelware.org) on how to build your own storage service.

## Usage
Refer to the [Installation](#installation) section on how to integrate **Remi** into your own toolchain (Gradle, Maven).

This example uses the local filesystem's storage service.

```java
import org.noelware.remi.support.filesystem.*;
import org.noelware.remi.core.*;

public class Program {
    public static void main(String[] args) {
        final FilesystemStorageService fsService = new FilesystemStorageService("./path/to/data/to/use");
        fsService.open("./file/to/load/in/given/directory.txt");
        // Opens as a `InputStream`.
    }
}
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

### Downloading from Noelware's Maven Repository
You can download the libraries yourself with `curl` or `wget` and link them with **javac**. You can download with the specific URL:

```http
GET https://maven.noelware.org/org/noelware/remi/remi-[package]/[version]/remi-[package]-[version]-sources.jar
```

## License
**Remi** is released with the [**MIT License**](https://github.com/Noelware/remi/blob/master/LICENSE) with love by Noelware. <3
