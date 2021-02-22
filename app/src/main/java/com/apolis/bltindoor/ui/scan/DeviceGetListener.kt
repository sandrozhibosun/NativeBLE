package com.apolis.bltindoor.ui.scan

import androidx.lifecycle.LiveData
import com.clj.fastble.data.BleDevice

interface DeviceGetListener {
    fun onGet(bleDevice: BleDevice)
    fun onClear()
}