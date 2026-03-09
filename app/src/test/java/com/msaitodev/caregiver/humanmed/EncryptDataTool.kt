package com.msaitodev.caregiver.humanmed

import com.msaitodev.core.common.util.CryptoUtils
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * master_data 内のプレーンな JSON を暗号化して assets に出力するツール。
 * ユニットテストとして実行することで、開発環境のファイルシステムを利用する。
 */
class EncryptDataTool {

    // プロジェクトルートからの相対パス
    private val masterDataDir = File("../master_data/quiz_data")
    private val outputAssetsDir = File("../app/src/main/assets/quiz_data")

    @Test
    fun encryptAllQuestions() {
        println("Encryption started...")
        println("Source: ${masterDataDir.absolutePath}")
        println("Destination: ${outputAssetsDir.absolutePath}")

        if (!masterDataDir.exists()) {
            throw IllegalStateException("Source directory not found: ${masterDataDir.absolutePath}")
        }

        // 出力先ディレクトリを一旦クリアして再作成（サブディレクトリ構造を維持）
        processDirectory(masterDataDir, outputAssetsDir)

        println("Encryption completed successfully.")
    }

    private fun processDirectory(source: File, destination: File) {
        if (!destination.exists()) {
            destination.mkdirs()
        }

        source.listFiles()?.forEach { file ->
            val targetFile = File(destination, file.name.replace(".json", ".bin"))
            
            if (file.isDirectory) {
                processDirectory(file, targetFile)
            } else if (file.name.endsWith(".json")) {
                encryptFile(file, targetFile)
            }
        }
    }

    private fun encryptFile(source: File, target: File) {
        println("Encrypting: ${source.name} -> ${target.name}")
        FileInputStream(source).use { input ->
            FileOutputStream(target).use { output ->
                CryptoUtils.encryptStream(input, output)
            }
        }
    }
}
