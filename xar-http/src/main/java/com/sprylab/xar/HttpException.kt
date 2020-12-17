package com.sprylab.xar

import okhttp3.Response
import java.io.IOException

class HttpException(message: String?, val response: Response) : IOException(message)
