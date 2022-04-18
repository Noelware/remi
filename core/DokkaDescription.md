# Module org.noelware.remi.core
Represents the core module to build your own storage trailers. We currently support as a Maven dependency:

- Filesystem via **remi-support-fs**
- Amazon S3 via **remi-support-s3**

## Custom Implementation
```kotlin
import org.noelware.remi.core.StorageTrailer
import org.noelware.remi.core.Configuration
import org.noelware.remi.core.EmptyConfiguration

class MyStorageTrailer: StorageTrailer<Configuration> {
    override val config: Configuration = EmptyConfiguration
    override val name: String = "some storage name"
    
    // implement stuff here
}
```
