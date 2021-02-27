package com.apolis.bltindoor.helper

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.apolis.bltindoor.ui.MyApplication

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BLEManagerModule {
    @Provides
    @Singleton
    fun provideBlueToothManger():BluetoothManager{
        val bluetoothManager=MyApplication
            .getInstance()
            .getSystemService(Context.BLUETOOTH_SERVICE)
                as BluetoothManager
        return bluetoothManager
    }
    @Provides
    @Singleton
    fun provideBlueToothAdapter():BluetoothAdapter{
        val bluetoothManager=MyApplication
            .getInstance()
            .getSystemService(Context.BLUETOOTH_SERVICE)
        as BluetoothManager
        return bluetoothManager.adapter
    }
}