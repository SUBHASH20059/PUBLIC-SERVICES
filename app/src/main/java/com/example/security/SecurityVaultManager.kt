package com.example.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecurityVaultManager {
    private const val AES_ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH = 128
    private const val IV_LENGTH = 12

    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    fun encrypt(data: String, secretKey: SecretKey): Pair<String, String> {
        val cipher = Cipher.getInstance(AES_ALGORITHM)
        val iv = ByteArray(IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val gcmParameterSpec = GCMParameterSpec(TAG_LENGTH, iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT) to Base64.encodeToString(iv, Base64.DEFAULT)
    }

    fun decrypt(encryptedData: String, iv: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(AES_ALGORITHM)
        val gcmParameterSpec = GCMParameterSpec(TAG_LENGTH, Base64.decode(iv, Base64.DEFAULT))
        
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
        
        return String(decryptedBytes)
    }
}
