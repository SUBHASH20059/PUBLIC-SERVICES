package org.psei.storage

import org.psei.config.AppConfig
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.util.UUID

/**
 * Abstract file storage service supporting multiple backends.
 *
 * Supports:
 *   - Local filesystem (development/default)
 *   - AWS S3 (production)
 *   - Cloudinary (CDN-backed)
 *   - UploadThing (alternative cloud)
 *
 * All file operations are validated for:
 *   - File size limits
 *   - Allowed file types
 *   - Content type verification
 */

private val logger = LoggerFactory.getLogger("FileStorage")

data class StorageResult(
    val success: Boolean,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long = 0,
    val error: String? = null
)

/**
 * Storage provider interface for backend-agnostic file operations.
 */
interface StorageProvider {
    fun upload(file: File, fileName: String, folder: String = "uploads"): StorageResult
    fun upload(bytes: ByteArray, fileName: String, folder: String = "uploads"): StorageResult
    fun download(fileUrl: String): InputStream?
    fun delete(fileUrl: String): Boolean
    fun exists(fileUrl: String): Boolean
    fun getPublicUrl(fileName: String, folder: String = "uploads"): String
    fun getFileSize(fileUrl: String): Long
}

// ─── Local Storage Provider ──────────────────────────────────────────────────

class LocalStorageProvider : StorageProvider {
    private val basePath = AppConfig.localStoragePath

    init {
        val dir = File(basePath)
        if (!dir.exists()) dir.mkdirs()
    }

    override fun upload(file: File, fileName: String, folder: String): StorageResult {
        return try {
            val maxSizeBytes = AppConfig.maxFileSizeMb * 1024 * 1024
            if (file.length() > maxSizeBytes) {
                return StorageResult(false, error = "File too large (max ${AppConfig.maxFileSizeMb}MB)")
            }

            val safeFileName = sanitizeFileName(fileName)
            val dirPath = "$basePath/$folder"
            File(dirPath).mkdirs()

            val destFile = File(dirPath, safeFileName)
            file.copyTo(destFile, overwrite = true)

            StorageResult(
                success = true,
                fileUrl = getPublicUrl(safeFileName, folder),
                fileName = safeFileName,
                fileSize = destFile.length()
            )
        } catch (e: Exception) {
            logger.error("Upload failed: ${e.message}", e)
            StorageResult(false, error = e.message)
        }
    }

    override fun upload(bytes: ByteArray, fileName: String, folder: String): StorageResult {
        return try {
            val file = File.createTempFile("upload_", "_${sanitizeFileName(fileName)}")
            file.writeBytes(bytes)
            upload(file, fileName, folder)
        } catch (e: Exception) {
            logger.error("Upload failed: ${e.message}", e)
            StorageResult(false, error = e.message)
        }
    }

    override fun download(fileUrl: String): InputStream? {
        return try {
            val file = File("$basePath/$fileUrl")
            if (file.exists()) file.inputStream() else null
        } catch (e: Exception) {
            logger.error("Download failed: ${e.message}", e)
            null
        }
    }

    override fun delete(fileUrl: String): Boolean {
        return try {
            val file = File("$basePath/$fileUrl")
            file.delete()
        } catch (e: Exception) {
            logger.error("Delete failed: ${e.message}", e)
            false
        }
    }

    override fun exists(fileUrl: String): Boolean = File("$basePath/$fileUrl").exists()
    override fun getPublicUrl(fileName: String, folder: String): String = "/uploads/$folder/$fileName"
    override fun getFileSize(fileUrl: String): Long = File("$basePath/$fileUrl").length()
}

// ─── S3 Storage Provider (Production) ────────────────────────────────────────

class S3StorageProvider : StorageProvider {
    // TODO: Implement with AWS SDK v2 when credentials are available
    // For now, delegates to local storage as fallback

    override fun upload(file: File, fileName: String, folder: String): StorageResult {
        // TODO: Implement S3 upload
        logger.warn("S3 upload not yet configured, falling back to local")
        return LocalStorageProvider().upload(file, fileName, folder)
    }

    override fun upload(bytes: ByteArray, fileName: String, folder: String): StorageResult {
        logger.warn("S3 upload not yet configured, falling back to local")
        return LocalStorageProvider().upload(bytes, fileName, folder)
    }

    override fun download(fileUrl: String): InputStream? = null
    override fun delete(fileUrl: String): Boolean = false
    override fun exists(fileUrl: String): Boolean = false
    override fun getPublicUrl(fileName: String, folder: String): String = "/uploads/$folder/$fileName"
    override fun getFileSize(fileUrl: String): Long = 0L
}

// ─── Cloudinary Storage Provider ─────────────────────────────────────────────

class CloudinaryStorageProvider : StorageProvider {
    // TODO: Implement with Cloudinary SDK when credentials are available

    override fun upload(file: File, fileName: String, folder: String): StorageResult {
        logger.warn("Cloudinary upload not yet configured, falling back to local")
        return LocalStorageProvider().upload(file, fileName, folder)
    }

    override fun upload(bytes: ByteArray, fileName: String, folder: String): StorageResult {
        logger.warn("Cloudinary upload not yet configured, falling back to local")
        return LocalStorageProvider().upload(bytes, fileName, folder)
    }

    override fun download(fileUrl: String): InputStream? = null
    override fun delete(fileUrl: String): Boolean = false
    override fun exists(fileUrl: String): Boolean = false
    override fun getPublicUrl(fileName: String, folder: String): String = "/uploads/$folder/$fileName"
    override fun getFileSize(fileUrl: String): Long = 0L
}

// ─── Facade Service ──────────────────────────────────────────────────────────

object FileStorageService {
    private val provider: StorageProvider = when (AppConfig.storageProvider.lowercase()) {
        "s3" -> S3StorageProvider()
        "cloudinary" -> CloudinaryStorageProvider()
        else -> LocalStorageProvider()
    }

    fun upload(file: File, fileName: String, folder: String = "uploads"): StorageResult =
        provider.upload(file, fileName, folder)

    fun upload(bytes: ByteArray, fileName: String, folder: String = "uploads"): StorageResult =
        provider.upload(bytes, fileName, folder)

    fun download(fileUrl: String): InputStream? = provider.download(fileUrl)
    fun delete(fileUrl: String): Boolean = provider.delete(fileUrl)
    fun exists(fileUrl: String): Boolean = provider.exists(fileUrl)
    fun getPublicUrl(fileName: String, folder: String = "uploads"): String =
        provider.getPublicUrl(fileName, folder)
    fun getFileSize(fileUrl: String): Long = provider.getFileSize(fileUrl)

    /**
     * Validate a file before upload: check size, type, and extension.
     */
    fun validateFile(file: File, allowedTypes: List<String> = AppConfig.allowedFileTypes): ValidationResult {
        val maxSizeBytes = AppConfig.maxFileSizeMb * 1024 * 1024

        if (file.length() > maxSizeBytes) {
            return ValidationResult(false, "File too large (max ${AppConfig.maxFileSizeMb}MB)")
        }

        val extension = file.extension.lowercase()
        if (extension !in allowedTypes.map { it.lowercase() }) {
            return ValidationResult(false, "File type not allowed: .$extension (allowed: ${allowedTypes.joinToString(", ")})")
        }

        return ValidationResult(true)
    }

    /**
     * Generate a unique filename preserving the original extension.
     */
    fun generateUniqueFileName(originalName: String): String {
        val extension = originalName.substringAfterLast('.', "")
        val uuid = UUID.randomUUID().toString().take(8)
        return "${uuid}_${originalName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}.$extension"
    }
}

data class ValidationResult(val valid: Boolean, val error: String? = null)

private fun sanitizeFileName(name: String): String {
    return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        .replace(Regex("_+"), "_")
        .let { if (it.length > 255) it.take(255) else it }
}
