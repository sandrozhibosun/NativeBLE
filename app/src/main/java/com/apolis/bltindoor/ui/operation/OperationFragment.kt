package com.apolis.bltindoor.ui.operation

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.apolis.bltindoor.R
import com.apolis.bltindoor.app.Const
import com.apolis.bltindoor.helper.DaggerAppComponent
import com.apolis.bltindoor.helper.SampleGattAttributes.OutputStringUtil
import com.apolis.bltindoor.ui.scan.BlueToothLeService

import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class OperationFragment : Fragment() {


    companion object {
        fun newInstance() = OperationFragment()
    }

    private lateinit var viewModel: OperationViewModel
    var bluetoothService: BlueToothLeService? = null

    /*
    1.Services: set of provided features and associated behaviors to interact with the peripheral. Each service contains a collection of characteristics.
    2.Characteristics: definition of the data divided into declaration and value. Using permission properties (read, write, notify, indicate) to get a value.
    3.Descriptor: an optional attribute nested in a characteristic that describes the specific value and how to access it.
    4.UUID: Universally Unique ID that are transmitted over the air so a peripheral can inform a central what services it provides.
    */
    val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (Const.ACTION_GATT_CONNECTED.equals(action)) {
                Toast.makeText(requireContext(), "Device Connected", Toast.LENGTH_SHORT).show()
            } else if (Const.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(requireContext(), "Device DisConnected", Toast.LENGTH_SHORT).show()
            }
            else if (Const.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(bluetoothService?.getSupportedGattServices())
            } else if (Const.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(Const.EXTRA_DATA));
            }
        }
    }

    val serviceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                bluetoothService = (service as BlueToothLeService.LocalBinder).getService()
                bluetoothService!!.bluetoothDevice = bleDevice
                //here call service.connect
                bluetoothService!!.connect()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                bluetoothService = null
            }
        }


    private fun displayGattServices(gattserves:List<BluetoothGattService?>?){


    }
    private fun displayData(data:String?){

    }

    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Const.ACTION_GATT_CONNECTED)
        intentFilter.addAction(Const.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(Const.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(Const.ACTION_DATA_AVAILABLE)
        return intentFilter
    }


    lateinit var bleDevice: BluetoothDevice
    lateinit var bluetoothGattService: BluetoothGattService

    //characteristic is where ble save data.
    //basically the bluetooth communication is read/write and subscribe on characteristic.
    lateinit var bluetoothGattCharacteristic: BluetoothGattCharacteristic
    // the uuid in gatt services will map specific attributes

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

        val component = DaggerAppComponent.create()
        component.inject(this)
        requireActivity().registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        Intent(requireActivity(), BlueToothLeService::class.java)
            .also {
                Log.d("abc", "start Intent")
                requireActivity().bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
            }
    }

    override fun onResume() {
        super.onResume()

        bleDevice = arguments?.get("device") as BluetoothDevice
        init()

    }
    //in google document, it used broadcast receiver to collect notifications.

    private fun init() {


        val byteArray = byteArrayOf(
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00
        )


    }



}