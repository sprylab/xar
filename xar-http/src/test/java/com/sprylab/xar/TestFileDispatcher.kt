package com.sprylab.xar

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.buffer
import okio.source

class TestFileDispatcher : Dispatcher() {
    @Throws(InterruptedException::class)
    override fun dispatch(request: RecordedRequest): MockResponse {
        try {
            val file = getClasspathResourceAsFile(request.path.orEmpty().substring(1))
            if (file == null || !file.exists()) {
                return MockResponse().setResponseCode(404)
            }
            when (request.method) {
                "HEAD" -> return MockResponse().setHeader("Content-Length", file.length())
                "GET" -> {
                    val buffer = Buffer()
                    val range = request.getHeader("Range")
                    val source = file.source().buffer()
                    if (range != null) {
                        // Partial file
                        val matcher = HttpXarSourceTest.PATTERN_RANGE_HEADER.matcher(range)
                        if (matcher.matches()) {
                            val start = Integer.valueOf(matcher.group(1))
                            val end = Integer.valueOf(matcher.group(2))
                            source.skip(start.toLong())
                            source.readFully(buffer, (end - start + 1).toLong())
                        } else {
                            // Full file
                            source.readAll(buffer)
                        }
                    } else {
                        // Full file
                        source.readAll(buffer)
                    }
                    return MockResponse().setBody(buffer)
                }
                else -> return MockResponse().setResponseCode(400)
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
