package com.dimts.gmcblChatBot.utils

import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class Utils {

    companion object {

        fun getEncryptionNextBus(jsonData: String?): String {
            var encryptedData = ""

            try {
                val _crypt = CriptoHelper()
                val key = CriptoHelper.SHA256NextBus()
                val iv = CriptoHelper.generateRandomIV(16) // 16 bytes = 128

                val output = _crypt.encrypt(jsonData, key, iv)
                encryptedData = output.trim { it <= ' ' } + iv.trim { it <= ' ' }
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return encryptedData
        }

        fun getDecryptionNextBus(EncryptData: String): String? {
            var decryptedData: String? = ""

            try {
                val _crypt = CriptoHelper()
                val key = CriptoHelper.SHA256NextBus()

                val iv = EncryptData.substring(EncryptData.length - 16, EncryptData.length)
                val SubData = EncryptData.substring(0, EncryptData.length - 16)
                decryptedData = _crypt.decrypt(SubData, key, iv)
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: NoSuchPaddingException) {
                e.printStackTrace()
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            } catch (e: InvalidAlgorithmParameterException) {
                e.printStackTrace()
            } catch (e: IllegalBlockSizeException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return decryptedData
        }
    }
}