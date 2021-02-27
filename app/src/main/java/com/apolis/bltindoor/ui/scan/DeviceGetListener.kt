package com.apolis.bltindoor.ui.scan

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData


interface DeviceGetListener {
    fun onGet(bleDevice: BluetoothDevice)
    fun onClear()
}