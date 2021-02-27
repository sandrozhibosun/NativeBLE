package com.apolis.bltindoor.ui.scan

import android.bluetooth.BluetoothDevice


interface DeviceCallbackListener {
    fun onConnectCallback()
    fun onDisconnectCallBack()
    fun onDetailCallBack(message:String,bleDevice:BluetoothDevice?)
}