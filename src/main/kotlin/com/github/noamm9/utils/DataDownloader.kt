package com.github.noamm9.utils

import com.github.noamm9.NoammAddons.MOD_ID
import com.github.noamm9.NoammAddons.MOD_NAME
import com.github.noamm9.utils.network.WebUtils
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.ZipInputStream
import kotlin.concurrent.thread
import kotlin.io.path.*

object DataDownloader {
    private const val DOWNLOAD_URL = "https://api.noammaddons.workers.dev/repo?type=zip"
    private const val HASH_URL = "https://api.noammaddons.workers.dev/repo?type=hash"
    val LOGGER = LoggerFactory.getLogger("$MOD_NAME - DataDownloader")

    private val modDataPath: Path = FabricLoader.getInstance().configDir.resolve(MOD_ID).resolve("data").also {
        if (! it.exists()) it.createDirectories()
    }

    fun downloadData() {
        thread(start = true, isDaemon = true, name = "$MOD_NAME - DataDownloader") {
            val versionFile = modDataPath.resolve("version.txt")

            try {
                LOGGER.info("Checking for remote data updates...")
                val remoteHash = runBlocking { WebUtils.getString(HASH_URL).getOrNull() } ?: return@thread LOGGER.error("Could not fetch remote version hash.")
                val localHash = if (versionFile.exists()) versionFile.readText().trim() else null

                if (remoteHash != localHash || ! modDataPath.exists()) {
                    LOGGER.info("Update required. Remote: $remoteHash, Local: ${localHash ?: "None"}")
                    update(versionFile, remoteHash)
                }
                else LOGGER.info("Data is up-to-date (Version: $localHash).")

            }
            catch (e: Exception) {
                LOGGER.error("Failed to check for data updates", e)
            }
        }
    }

    private fun update(versionFile: Path, newHash: String) {
        try {
            val tempZipFile = Files.createTempFile("data-download-", ".zip")

            URL(DOWNLOAD_URL).openStream().use { input ->
                Files.copy(input, tempZipFile, StandardCopyOption.REPLACE_EXISTING)
            }

            if (modDataPath.exists()) modDataPath.toFile().deleteRecursively()
            modDataPath.createDirectories()

            unzip(tempZipFile)

            versionFile.writeText(newHash)
            tempZipFile.deleteIfExists()

            LOGGER.info("Data update successful.")
        }
        catch (e: IOException) {
            LOGGER.error("Failed to update data files", e)
        }
    }

    private fun unzip(zipFilePath: Path) {
        ZipInputStream(zipFilePath.inputStream()).use { zis ->
            var rootDirName: String? = null

            while (true) {
                val entry = zis.nextEntry ?: break

                if (rootDirName == null) rootDirName = entry.name.substringBefore('/') + "/"

                val entryName = entry.name.removePrefix(rootDirName)
                if (entryName.isEmpty()) continue

                val targetPath = modDataPath.resolve(entryName)

                if (! targetPath.normalize().startsWith(modDataPath.normalize())) {
                    throw IOException("Zip entry attempted to write outside target: ${entry.name}")
                }

                if (entry.isDirectory) targetPath.createDirectories()
                else {
                    targetPath.parent?.createDirectories()
                    Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING)
                }
                zis.closeEntry()
            }
        }
    }

    inline fun <reified T> loadJson(fileName: String): T {
        return getReader(fileName).use { reader ->
            JsonUtils.gsonBuilder.fromJson(reader, object: TypeToken<T>() {}.type)
        }
    }

    fun getReader(fileName: String): BufferedReader {
        val localFile = modDataPath.resolve(fileName)
        return if (localFile.exists()) localFile.bufferedReader()
        else TODO("Implement remote file fetching")

        /*
        LOGGER.warn("Local file '$fileName' missing. Fetching from RAW URL.")
        val connection = URL(RAW_URL + fileName).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        return connection.inputStream.bufferedReader()*/
    }
}