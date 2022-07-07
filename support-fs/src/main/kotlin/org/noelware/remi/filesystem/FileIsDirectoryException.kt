package org.noelware.remi.filesystem

/**
 * Exception to throw if the file is a directory.
 */
class FileIsDirectoryException(path: String): RuntimeException("File [$path] was a directory and the operation doesn't support directories.")
