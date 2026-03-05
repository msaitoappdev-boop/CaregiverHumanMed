package com.msaitodev.core.common.util

import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 問題データの暗号化・復号を担当するユーティリティ。
 * AES (AES/CBC/PKCS5Padding) を使用。
 */
object CryptoUtils {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_ALGORITHM = "AES"
    private const val IV_SIZE = 16

    // TODO: リリース時はより安全な方法で管理することを検討
    // 16, 24, 32 bytes for AES-128, 192, 256
    private val FIXED_KEY = "3q_trainer_secret_key_2024".toByteArray().copyOf(16)

    /**
     * 入力ストリームを復号して返す。
     */
    fun decryptStream(inputStream: InputStream): InputStream {
        val iv = ByteArray(IV_SIZE)
        inputStream.read(iv)
        val ivSpec = IvParameterSpec(iv)
        val keySpec = SecretKeySpec(FIXED_KEY, KEY_ALGORITHM)

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        return CipherInputStream(inputStream, cipher)
    }

    /**
     * プレーンな入力を暗号化して出力ストリームに書き込む（ビルド時/ツール用）。
     */
    fun encryptStream(inputStream: InputStream, outputStream: OutputStream) {
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)
        outputStream.write(iv)

        val ivSpec = IvParameterSpec(iv)
        val keySpec = SecretKeySpec(FIXED_KEY, KEY_ALGORITHM)

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        val cos = CipherOutputStream(outputStream, cipher)
        inputStream.copyTo(cos)
        cos.close()
    }
}
