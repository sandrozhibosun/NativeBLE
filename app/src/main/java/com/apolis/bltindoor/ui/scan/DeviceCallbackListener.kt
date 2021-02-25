package com.apolis.bltindoor.ui.scan

import com.clj.fastble.data.BleDevice

interface DeviceCallbackListener {
    fun onConnectCallback()
    fun onDisconnectCallBack()
    fun onDetailCallBack(message:String,bleDevice: BleDevice?)
}