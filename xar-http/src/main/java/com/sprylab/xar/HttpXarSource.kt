package com.sprylab.xar

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource
import java.io.IOException

/**
 * Represents an eXtensible ARchiver using a URL.
 * Create a new [XarSource] from a `url`. It is recommended to create and manage a global, shared [OkHttpClient] instance for
 * optimal overall performance. If `okHttpClient` is `null`, a default, non-shared [OkHttpClient] is used.
 *
 * @param url          the URL referencing the archive file
 * @param okHttpClient the [OkHttpClient] to use or `null` to use the default one
 */
class HttpXarSource @JvmOverloads constructor(
    /**
     * @return the underlying URL
     */
    private val url: String,
    private val okHttpClient: OkHttpClient = OkHttpClient()
) : XarSource() {

    @Throws(IOException::class)
    override fun getRange(offset: Long, length: Long): BufferedSource {
        return executeRangeRequest(offset, length)
    }

    @Throws(XarException::class)
    override fun getSize(): Long {
        return executeContentLengthRequest()
    }

    override fun toString(): String = String.format("HttpXarSource{url='%s'}", url)

    @Throws(XarException::class)
    private fun executeRangeRequest(offset: Long, length: Long): BufferedSource = try {
        val request: Request = Request.Builder()
            .get()
            .header("Range", String.format("bytes=%d-%d", offset, offset + length - 1L))
            .url(url)
            .build()
        val call = okHttpClient.newCall(request)
        // Do not use "use" here as the Source of the response will be returned
        val response = call.execute()
        val responseBody = response.body
        if (response.isSuccessful && responseBody != null) {
            responseBody.source()
        } else {
            response.close()
            throw HttpException("Error executing request: ${response.code} ${response.message}", response)
        }
    } catch (e: IOException) {
        throw XarException("Error reading contents", e)
    }

    @Throws(XarException::class)
    private fun executeContentLengthRequest(): Long = try {
        val request: Request = Request.Builder().head().url(url).build()
        okHttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                response.header("Content-Length").orEmpty().toLong()
            } else {
                throw HttpException("Error executing request: ${response.code} ${response.message}", response)
            }
        }
    } catch (e: IOException) {
        throw XarException("Error reading content length", e)
    } catch (e: NumberFormatException) {
        throw XarException("Error reading content length", e)
    }
}
