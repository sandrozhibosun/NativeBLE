package com.apolis.bltindoor.ui.operation

import android.bluetooth.BluetoothGattCharacteristic
import androidx.lifecycle.ViewModel
import com.apolis.bltindoor.helper.DaggerAppComponent

import javax.inject.Inject

class OperationViewModel  : ViewModel() {


    lateinit var characteristic: BluetoothGattCharacteristic


    init {

//        val component= DaggerAppComponent.create()
//        component.inject(this)

    }
    // TODO: Implement the ViewModel

}