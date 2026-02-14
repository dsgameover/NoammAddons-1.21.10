package com.github.noamm9.utils.network

import com.github.noamm9.NoammAddons.mc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

object WebUtils {
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    private const val TIMEOUT = 10_000
    val _json = Json { ignoreUnknownKeys = true }


    private fun prepareConnection(url: String): HttpURLConnection {
        if (mc.isSameThread) throw Exception("Cannot make network request on main thread")
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", USER_AGENT)
        connection.connectTimeout = TIMEOUT
        connection.readTimeout = TIMEOUT
        return connection
    }

    suspend fun getString(url: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = prepareConnection(url)
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            handleResponse(connection)
        }
    }

    suspend inline fun <reified T> get(url: String): Result<T> = runCatching {
        return getString(url).mapCatching {
            _json.decodeFromString<T>(it)
        }
    }

    suspend fun post(url: String, body: Any): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = prepareConnection(url)
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.doOutput = true

            connection.outputStream.use { os ->
                os.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            handleResponse(connection)
        }
    }

    suspend fun downloadBytes(url: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = prepareConnection(url)
            connection.requestMethod = "GET"

            val code = connection.responseCode
            val stream = if (code in 200 .. 299) connection.inputStream else connection.errorStream

            if (code !in 200 .. 299) throw IllegalStateException("HTTP $code")

            stream.use { it.readBytes() }
        }
    }

    private fun handleResponse(connection: HttpURLConnection): String {
        val code = connection.responseCode
        val stream = if (code in 200 .. 299) connection.inputStream
        else connection.errorStream ?: connection.inputStream
        val response = stream.bufferedReader().use(BufferedReader::readText)
        if (code !in 200 .. 299) throw IllegalStateException("HTTP $code: $response")
        return response
    }
}