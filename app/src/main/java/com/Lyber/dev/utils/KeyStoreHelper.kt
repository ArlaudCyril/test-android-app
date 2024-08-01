package com.Lyber.dev.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object KeystoreHelper {

    private const val KEY_ALIAS = "my_secret_key_alias"

    private fun getKeyStore(): KeyStore {
        return KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    }

    fun generateAndStoreKey(context: Context) {
        val keyStore = getKeyStore()
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    fun getSecretKey(): SecretKey {
        val keyStore = getKeyStore()
        val key = keyStore.getKey(KEY_ALIAS, null)
        return key as SecretKey
    }

}