# Module org.noelware.remi.gcs
This is the official Remi provider for using Google Cloud Storage as your storage backend.

Please note that using this provider that it is in very early stages of development! Please report any bugs
[here](https://github.com/Noelware/remi/issues)!

## Usage
```kotlin
import org.noelware.remi.gcs.GoogleCloudStorageProvider

suspend fun main(args: Array<String>) {
    // Create a provider using the credentials' path.
    val provider = GoogleCloudStorageProvider.fromFile("/path/to/credentials/file")
    
    provider.open("path/to/item") // => InputStream?
}
```
