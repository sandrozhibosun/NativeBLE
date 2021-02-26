package com.apolis.bltindoor.helper

import com.apolis.bltindoor.ui.MyApplication
import com.clj.fastble.BleManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BLEManagerModule {

    @Provides
    @Singleton
    fun CreateBleManager(): BleManager {
        var bleManager = BleManager.getInstance()
        bleManager.init(MyApplication.getInstance())
        //configuration of ble manager
        bleManager.enableLog(true)
            .setMaxConnectCount(10)

            .setOperateTimeout(5000)
//also android has native bluetooth manager , and init like
// val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
// val bluetoothAdapter = bluetoothManager.adapter

        return bleManager
    }
}