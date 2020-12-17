@file:JvmName("TestUtil")

package com.sprylab.xar

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.RandomStringUtils
import java.io.File
import java.net.URISyntaxException

fun getTempDirectory(): File = File(FileUtils.getTempDirectory(), RandomStringUtils.randomAlphabetic(10))

@Throws(URISyntaxException::class)
fun getClasspathResourceAsFile(name: String?): File? {
    val resource = Thread.currentThread().contextClassLoader.getResource(name) ?: return null
    return File(resource.toURI())
}
