package com.apolis.bltindoor.ui.scan

import com.clj.fastble.data.BleDevice

interface OnConnectCallListener {
    fun onDetailClicked(bleDevice: BleDevice)
}