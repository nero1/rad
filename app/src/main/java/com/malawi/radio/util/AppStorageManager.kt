package com.malawi.radio.util

import android.content.Context
import java.io.File

/**
 * Keeps temporary app-managed storage bounded. User data lives in DataStore / shared
 * preferences; this only trims disposable cache folders used by Android, ads,
 * WebView/network stacks, image loaders, and media components.
 */
object AppStorageManager {
    private const val SESSION_MAX_CACHE_BYTES = 100L * 1024L * 1024L
    private const val STARTUP_TRIM_THRESHOLD_BYTES = 20L * 1024L * 1024L
    private const val STARTUP_TARGET_CACHE_BYTES = 5L * 1024L * 1024L

    fun trimCache(context: Context, maxBytes: Long = SESSION_MAX_CACHE_BYTES) {
        trimDirectories(cacheDirs(context), maxBytes)
    }

    fun trimStartupCache(context: Context) {
        val cacheDirs = cacheDirs(context)
        if (cacheDirs.totalSizeBytes() > STARTUP_TRIM_THRESHOLD_BYTES) {
            trimDirectories(cacheDirs, STARTUP_TARGET_CACHE_BYTES)
        }
    }

    private fun cacheDirs(context: Context): List<File> =
        listOfNotNull(context.cacheDir, context.codeCacheDir, context.externalCacheDir)
            .distinctBy { it.absolutePath }

    private fun trimDirectories(directories: List<File>, maxBytes: Long) {
        val files = directories
            .flatMap { directory ->
                if (!directory.exists() || !directory.isDirectory) {
                    emptyList()
                } else {
                    directory.walkBottomUp()
                        .filter { it.isFile }
                        .map { CacheFile(it, it.length(), it.lastModified()) }
                        .toList()
                }
            }

        var totalBytes = files.sumOf { it.sizeBytes }
        files.sortedBy { it.lastModified }.forEach { cacheFile ->
            if (totalBytes <= maxBytes) return
            if (cacheFile.file.delete()) {
                totalBytes -= cacheFile.sizeBytes
            }
        }

        directories.forEach { it.deleteEmptyDirectories() }
    }

    private fun List<File>.totalSizeBytes(): Long =
        sumOf { directory ->
            if (!directory.exists() || !directory.isDirectory) {
                0L
            } else {
                directory.walkBottomUp()
                    .filter { it.isFile }
                    .sumOf { it.length() }
            }
        }

    private fun File.deleteEmptyDirectories() {
        if (!exists() || !isDirectory) return

        walkBottomUp()
            .filter { it.isDirectory && it != this && it.listFiles().isNullOrEmpty() }
            .forEach { it.delete() }
    }

    private data class CacheFile(
        val file: File,
        val sizeBytes: Long,
        val lastModified: Long
    )
}
