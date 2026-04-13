package com.beastsaber.app.ui.screens.send

import android.net.Uri

/**
 * Parses the LAN pairing QR from the PC app:
 * `http://HOST:PORT/?token=SECRET` (or https).
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
    val baseUrl = "$scheme://$host:$port"
    return baseUrl to token
}
