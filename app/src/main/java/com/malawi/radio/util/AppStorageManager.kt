package com.malawi.radio.util

import android.content.Context
import java.io.File

/**
 * Keeps temporary app-managed storage bounded. User data lives in DataStore / shared
 * preferences; this only trims disposable cache folders used by Android, ads,
 * WebView/network stacks, image loaders, and media components.
 */
object AppStorageManager {
    private const val MAX_CACHE_BYTES = 25L * 1024L * 1024L

    fun trimCache(context: Context, maxBytes: Long = MAX_CACHE_BYTES) {
        val cacheDirs = listOfNotNull(context.cacheDir, context.codeCacheDir, context.externalCacheDir)
            .distinctBy { it.absolutePath }
        cacheDirs.forEach { trimDirectory(it, maxBytes / cacheDirs.size.coerceAtLeast(1)) }
    }

    private fun trimDirectory(directory: File, maxBytes: Long) {
        if (!directory.exists() || !directory.isDirectory) return

        val files = directory.walkBottomUp()
            .filter { it.isFile }
            .map { CacheFile(it, it.length(), it.lastModified()) }
            .toList()

        var totalBytes = files.sumOf { it.sizeBytes }
        files.sortedBy { it.lastModified }.forEach { cacheFile ->
            if (totalBytes <= maxBytes) return
            if (cacheFile.file.delete()) {
                totalBytes -= cacheFile.sizeBytes
            }
        }

        directory.walkBottomUp()
            .filter { it.isDirectory && it != directory && it.listFiles().isNullOrEmpty() }
            .forEach { it.delete() }
    }

    private data class CacheFile(
        val file: File,
        val sizeBytes: Long,
        val lastModified: Long
    )
}
