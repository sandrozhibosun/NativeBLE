package com.apolis.bltindoor.ui.scan

import android.bluetooth.BluetoothDevice


interface OnConnectCallListener {
    fun onDetailClicked(bleDevice:BluetoothDevice)
}