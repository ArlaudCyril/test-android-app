package com.Lyber.dev.utils

import android.util.Base64
//import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


object EncryptionHelper {

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
//    fun encrypt(secretKey: SecretKey, data: String): String {
//        val cipher = Cipher.getInstance(TRANSFORMATION)
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
//        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
//
//        // Retrieve IV and prepend it to the encrypted data
//        val iv = cipher.iv
//        return Base64.getEncoder().encodeToString(iv + encryptedData)
//    }
//
//    fun decrypt(secretKey: SecretKey, encryptedData: String): String {
//        val cipher = Cipher.getInstance(TRANSFORMATION)
//        val decodedData = Base64.getDecoder().decode(encryptedData)
//
//        // Extract IV and encrypted text
//        val iv = decodedData.copyOfRange(0, IV_LENGTH_BYTE)
//        val encryptedText = decodedData.copyOfRange(IV_LENGTH_BYTE, decodedData.size)
//
//        val ivSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
//        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
//        val decryptedData = cipher.doFinal(encryptedText)
//        return String(decryptedData, Charsets.UTF_8)
//    }
    fun encrypt(secretKey: SecretKey, data: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

        // Retrieve IV and prepend it to the encrypted data
        val iv = cipher.iv
        return Base64.encodeToString(iv + encryptedData, Base64.DEFAULT)
    }

    fun decrypt(secretKey: SecretKey, encryptedData: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)

        // Extract IV and encrypted text
        val iv = decodedData.copyOfRange(0, IV_LENGTH_BYTE)
        val encryptedText = decodedData.copyOfRange(IV_LENGTH_BYTE, decodedData.size)

        val ivSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        val decryptedData = cipher.doFinal(encryptedText)
        return String(decryptedData, Charsets.UTF_8)
    }
}