package com.malawi.radio.util

import android.content.Context
import java.io.File

/**
 * Keeps temporary app-managed storage bounded. User data lives in DataStore / shared
 * preferences; this only trims disposable cache folders used by Android, ads,
 * WebView/network stacks, image loaders, and media components.
 *
 * Policy: on every app startup, if the cache already exceeds 20 MB, trim it down
 * to 5 MB. No session-time cap is enforced; Android's own process lifecycle
 * management handles memory pressure during a session.
 */
object AppStorageManager {

    private const val STARTUP_TRIM_THRESHOLD_BYTES = 20L * 1024L * 1024L
    private const val STARTUP_TARGET_CACHE_BYTES = 5L * 1024L * 1024L

    /**
     * Call on app startup, off the main thread. Checks total cache size across
     * cacheDir, codeCacheDir, and externalCacheDir, and trims to
     * STARTUP_TARGET_CACHE_BYTES if it exceeds STARTUP_TRIM_THRESHOLD_BYTES.
     */
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

        // Oldest files first. Use a plain for/break loop here, not forEach —
        // `return` inside a forEach lambda is a non-local return and would
        // exit trimDirectories() entirely, skipping the empty-directory
        // cleanup below every time trimming actually did something.
        for (cacheFile in files.sortedBy { it.lastModified }) {
            if (totalBytes <= maxBytes) break
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
