package com.movil.paralelo.utils

object Constants {
    // 10.0.2.2 es el alias del host (localhost de tu PC) en el emulador de Android Studio.
    // Si ejecutas en un celular físico por USB/WiFi, cambia esto por la IP local de tu PC (ej: 192.168.1.50)
    var BASE_URL: String = "http://10.0.2.2:8000/"

    const val PREFS_NAME = "paralelo_movil_prefs"
    const val KEY_AUTH_TOKEN = "key_auth_token"
    const val KEY_USER_EMAIL = "key_user_email"
    const val KEY_USER_NAME = "key_user_name"
    const val KEY_USER_ID = "key_user_id"
}
