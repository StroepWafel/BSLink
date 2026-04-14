package com.beastsaber.app.ui.screens.send

import android.net.Uri

/**
 * Parses the pairing QR from the PC app:
 * `http://HOST:PORT/?token=SECRET`, `https://HOST/path/?token=SECRET` (relay), etc.
 * The returned base URL must include any path prefix before `/import` on POST.
 */
fun parseLanPairingQr(raw: String): Pair<String, String>? {
    val s = raw.trim()
    if (s.isEmpty()) return null
    val uri = Uri.parse(s)
    val scheme = uri.scheme?.lowercase() ?: return null
    if (scheme != "http" && scheme != "https") return null
    val host = uri.host ?: return null
    val port = when {
        uri.port != -1 -> uri.port
        scheme == "https" -> 443
        else -> 80
    }
    val token = uri.getQueryParameter("token")?.trim().orEmpty()
    if (token.isEmpty()) return null
    val rawPath = uri.path.orEmpty().trimEnd('/')
    val pathPart = when {
        rawPath.isEmpty() || rawPath == "/" -> ""
        else -> rawPath
    }
    val defaultPort = (scheme == "https" && port == 443) || (scheme == "http" && port == 80)
    val portPart = if (defaultPort && uri.port == -1) "" else ":$port"
    val baseUrl = buildString {
        append(scheme).append("://").append(host).append(portPart).append(pathPart)
    }
    return baseUrl to token
}
