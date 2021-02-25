package com.apolis.bltindoor.ui.operation

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.apolis.bltindoor.R
import com.apolis.bltindoor.helper.DaggerAppComponent
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import javax.inject.Inject

class OperationFragment : Fragment() {


    companion object {
        fun newInstance() = OperationFragment()
    }

    private lateinit var viewModel: OperationViewModel

    @Inject
    lateinit var bleManager: BleManager
    lateinit var bleDevice: BleDevice
    lateinit var bluetoothGattService: BluetoothGattService
    lateinit var bluetoothGattCharacteristic: BluetoothGattCharacteristic
    lateinit var gatt: BluetoothGatt

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.operation_fragment, container, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OperationViewModel::class.java)
        // TODO: Use the ViewModel
        val component = DaggerAppComponent.create()
        component.inject(this)
    }

    override fun onResume() {
        super.onResume()
//        bleDevice = arguments?.get("device") as BleDevice
//        init()
    }

    private fun init() {
        gatt = BleManager.getInstance().getBluetoothGatt(bleDevice)
        bluetoothGattService = gatt.services[0]
        bluetoothGattCharacteristic = bluetoothGattService.characteristics[0]
        read()

    }
    private fun send(bytes:ByteArray){
        bleManager.write(
            bleDevice,
            bluetoothGattCharacteristic.service.uuid.toString(),
            bluetoothGattCharacteristic.uuid.toString(),
            bytes,
            object: BleWriteCallback(){
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                   Log.d("abc","onWirte Success")
                }

                override fun onWriteFailure(exception: BleException?) {
                    Log.d("abc","OnWrite Failure")
                }

            }
        )
    }
    private fun send(hex:String){
        bleManager.write(
            bleDevice,
            bluetoothGattCharacteristic.service.uuid.toString(),
            bluetoothGattCharacteristic.uuid.toString(),
            HexUtil.hexStringToBytes(hex),
            object: BleWriteCallback(){
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    Log.d("abc","onWirte Success")
                }

                override fun onWriteFailure(exception: BleException?) {
                    Log.d("abc","OnWrite Failure")
                }

            }
        )

    }
    private fun read(){
        bleManager.read(
            bleDevice,
            bluetoothGattCharacteristic.service.uuid.toString(),
            bluetoothGattCharacteristic.uuid.toString(),
            object :BleReadCallback(){
                override fun onReadSuccess(data: ByteArray?) {
                    Log.d("abc","onRead Success")
                   var string=data.toString()


                }

                override fun onReadFailure(exception: BleException?) {
                    Log.d("abc","onRead Failure")
                }

            }
        )


    }


}