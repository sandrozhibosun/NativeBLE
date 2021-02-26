package com.apolis.bltindoor.helper.SampleGattAttributes

import android.text.TextUtils


class OutputStringUtil {
    fun transferForPrint(vararg bytes: Byte): String? {
        return if (bytes == null) null else transferForPrint(String(bytes))
    }


    // @param str
    //@return  str
    companion object {
        fun transferForPrint(str: String): String? {
            var str = str
            if (TextUtils.isEmpty(str)) return str
            str = str.replace('\r', ' ')
            str = str.replace('\n', ' ')
            if (str.endsWith(">")) {
                str = str.substring(0, str.length - 1)
            }
            return str
        }


        // convert to hex str
        // @param b byte
        //@return str

        private fun toHexStr(b: Byte): String? {
            var str = Integer.toHexString(0xFF and b.toInt())
            if (str.length == 1) str = "0$str"
            return str.toUpperCase()
        }

        // convert to hex str
        // @param b byte
        //@return str
        fun toHexString(vararg bytes: Byte): String? {
            if (bytes == null) return null
            val sb = StringBuilder()
            if (bytes.size < 20) {
                sb.append("[")
                for (i in 0 until bytes.size) {
                    sb.append(toHexStr(bytes[i])).append(",")
                }
                sb.append("]")
            } else {
                sb.append("[")
                for (i in 0..3) {
                    sb.append(toHexStr(bytes[i])).append(",")
                }
                sb.append("...")
                for (i in bytes.size - 5 until bytes.size) {
                    sb.append(toHexStr(bytes[i])).append(",")
                }
                sb.setLength(sb.length - 1)
                sb.append("]")
            }
            return sb.toString()
        }
        fun byteArrayToHexString(bytes: ByteArray):String?{
            val sb=StringBuilder()
            for(byte in bytes)
            {
                sb.append(toHexStr(byte))
            }
            return sb.toString()


        }
    }
}