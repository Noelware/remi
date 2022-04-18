# Module org.noelware.remi.filesystem
This module adds the storage trailer implementation for using the local disk the application is running on.

## Examples
```kotlin
suspend fun main(args: Array<String>) {
    val storage = FilesystemStorageTrailer(args[0])
    storage.listAll()
    // => List<org.noelware.remi.core.Object>
    
    storage.open("./owo.txt")
    // => InputStream?
}
```
