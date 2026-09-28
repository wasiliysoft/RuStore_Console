package ru.wasiliysoft.rustoreconsole.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {
    companion object {
        private const val KEY_ALIAS = "vk_secure_token_key"
    }

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }

    // Шифруем строку и склеиваем IV (вектор инициализации) + зашифрованные байты в Base64 строку
    fun encrypt(rawString: String): String {
        if (rawString.isEmpty()) return ""
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val encryptedBytes = cipher.doFinal(rawString.toByteArray(Charsets.UTF_8))

        // IV нужен для расшифровки, сохраняем его вместе с данными
        val combinedBytes = cipher.iv + encryptedBytes
        return Base64.encodeToString(combinedBytes, Base64.DEFAULT)
    }

    // Достаем IV из начала строки, инициализируем Cipher и расшифровываем назад в строку
    fun decrypt(encryptedBase64: String): String {
        if (encryptedBase64.isEmpty()) return ""
        val combinedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)

        val iv = combinedBytes.copyOfRange(0, 12) // Для AES/GCM размер IV всегда 12 байт
        val encryptedBytes = combinedBytes.copyOfRange(12, combinedBytes.size)

        cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
    }


}