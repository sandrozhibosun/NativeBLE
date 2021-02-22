package com.apolis.bltindoor.ui.scan

interface DeviceCallbackListener {
    fun onConnectCallback()
    fun onDisconnectCallBack()
    fun onDetailCallBack(message:String)
}