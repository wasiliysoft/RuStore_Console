package ru.wasiliysoft.rustoreconsole.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {

    private const val KEY_ALIAS = "vk_secure_token_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val GCM_IV_LENGTH_BYTES = 12

    // Один поток на весь процесс. Все операции с keystore идут строго последовательно.
    private val executor = Executors.newSingleThreadExecutor()

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    }

    /**
     * Шифрует строку. Блокирует вызывающий поток до завершения операции.
     * Возвращает Base64(IV + ciphertext) или null при ошибке.
     */
    fun encrypt(rawString: String): String? {
        if (rawString.isEmpty()) return ""

        val latch = CountDownLatch(1)
        val result = AtomicReference<String?>(null)

        executor.execute {
            try {
                result.set(doEncrypt(rawString))
            } catch (e: Throwable) {
                logError("encrypt failed", e)
            } finally {
                latch.countDown()
            }
        }

        latch.await()
        return result.get()
    }

    /**
     * Расшифровывает Base64(IV + ciphertext). Блокирует вызывающий поток.
     */
    fun decrypt(encryptedBase64: String): String? {
        if (encryptedBase64.isEmpty()) return ""

        val latch = CountDownLatch(1)
        val result = AtomicReference<String?>(null)

        executor.execute {
            try {
                result.set(doDecrypt(encryptedBase64))
            } catch (e: Throwable) {
                logError("decrypt failed", e)
            } finally {
                latch.countDown()
            }
        }

        latch.await()
        return result.get()
    }

    /**
     * Удаляет ключ. Используйте при KeyPermanentlyInvalidatedException
     * или для принудительного сброса.
     */
    fun resetKey(): Boolean {
        val latch = CountDownLatch(1)
        val result = AtomicReference(false)

        executor.execute {
            try {
                if (keyStore.containsAlias(KEY_ALIAS)) {
                    keyStore.deleteEntry(KEY_ALIAS)
                }
                result.set(true)
            } catch (e: Throwable) {
                logError("resetKey failed", e)
            } finally {
                latch.countDown()
            }
        }

        latch.await()
        return result.get()
    }

    // --- Внутренние методы. Выполняются ТОЛЬКО в executor. ---

    private fun doEncrypt(rawString: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        val encryptedBytes = cipher.doFinal(rawString.toByteArray(Charsets.UTF_8))
        val combinedBytes = cipher.iv + encryptedBytes

        return Base64.encodeToString(combinedBytes, Base64.DEFAULT)
    }

    private fun doDecrypt(encryptedBase64: String): String {
        val combinedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)

        require(combinedBytes.size > GCM_IV_LENGTH_BYTES) {
            "Invalid ciphertext: too short"
        }

        val iv = combinedBytes.copyOfRange(0, GCM_IV_LENGTH_BYTES)
        val encryptedBytes = combinedBytes.copyOfRange(
            GCM_IV_LENGTH_BYTES, combinedBytes.size
        )

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getKey(),
            GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        )

        return String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun logError(message: String, e: Throwable) {
        Log.e("CryptoManager", message, e)
    }
}