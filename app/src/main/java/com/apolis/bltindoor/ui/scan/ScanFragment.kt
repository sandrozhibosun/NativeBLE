package com.apolis.bltindoor.ui.scan

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.apolis.bltindoor.app.Const
import com.apolis.bltindoor.databinding.ScanFragmentBinding
import com.apolis.bltindoor.helper.DaggerAppComponent
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


class ScanFragment : Fragment(), DeviceGetListener, OnConnectCallListener {
    private lateinit var binding: ScanFragmentBinding

    @Inject//bluetooth adapter
    lateinit var bluetoothAdapter: BluetoothAdapter
    private var handler = Handler()
    private var mScanning = false

    val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (Const.ACTION_GATT_CONNECTED.equals(action)) {
                Toast.makeText(requireContext(), "Device Connected", Toast.LENGTH_SHORT).show()
            } else if (Const.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(requireContext(), "Device DisConnected", Toast.LENGTH_SHORT).show()
            } else if (Const.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Toast.makeText(requireContext(), "Discover Services", Toast.LENGTH_SHORT).show()
            } else if (Const.ACTION_DATA_AVAILABLE.equals(action)) {
                Toast.makeText(
                    requireContext(),
                    intent.getStringExtra(Const.EXTRA_DATA),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Const.ACTION_GATT_CONNECTED)
        intentFilter.addAction(Const.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(Const.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(Const.ACTION_DATA_AVAILABLE)
        return intentFilter
    }

    //set the request code for permission
    companion object {
        val Tag = ScanFragment::class.simpleName
        private const val REQUEST_CODE_OPEN_GPS = 1
        private const val REQUEST_CODE_PERMISSION_LOCATION = 2
        private const val SCAN_PERIOD: Long = 10000

    }


    //setup the viewmodel and adapter, set the call back listener
    private lateinit var viewModel: ScanViewModel
    private val viewAdapter = DeviceAdapter().apply {
        parentFragment = this@ScanFragment
        onConnectCallListener = this@ScanFragment
    }
    var bluetoothService: BlueToothLeService? = null
    var scanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            //notice,usually the scan device  name will be null
            //add to recycler view
            this@ScanFragment.onGet(result!!.device)
            retainInstance = true//prevent destory fragment on rotate, but now viewmodel is better
        }

    }

    //data binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ScanFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        return view

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ScanViewModel::class.java)
        // TODO: Use the ViewModel
        viewModel.deviceGetListener = this
        val component = DaggerAppComponent.create()
        component.inject(this)
        requireActivity().registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        init()

    }

    //set up the recycler view for blue tooth device.
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        binding.deviceRecyclerView.apply {
            adapter = viewAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        //set scan, before we start, check permission first.
        binding.btnScan.setOnClickListener {
            checkPermissions()
        }
        binding.btnStopScan.setOnClickListener {
            startLEScan(false)
        }

    }

    //first step of implementing ble
    //Scan for device by GAP protocol, here we can add a filter
    // From API 19 and up you can start the pairing by calling the mBluetoothDevice.createBond().
    // You don't need to be connected with the remote BLE device to start the pairing process.
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun startLEScan(enable: Boolean) {
        //we can declared filter and scan setting here, eg:
        /**
        val uuid =ParcelUuid(ServiceUUID)
        val filter=ScanFilter.Builder().setServiceUuid(uuid).build
        val filters=listof(filter)

        val settings=ScanSettings
        .Builder()
        .setScanMode(ScanSettings.Scan_Mode_LOW_LATENCY)
        .build()
         */


        when (enable) {//use handler here to set time out
            true -> {
                viewAdapter.clearScanDevice()
                //if we want to scan specific device , we can define UUID as arguement, like:
                //startLeScan(UUID[], BluetoothAdapter.LeScanCallback)
                handler.postDelayed({
                    mScanning = false
                    bluetoothAdapter.bluetoothLeScanner.stopScan(
                        scanCallback
                    )
                }, SCAN_PERIOD)//end postDelayed 10s time out
                mScanning = true
                bluetoothAdapter.bluetoothLeScanner.startScan(// filter defined here
                    scanCallback
                )

            }//end true
            else -> {
                mScanning = false
                bluetoothAdapter.bluetoothLeScanner.stopScan(
                    scanCallback
                )//end stop scan
            }

        }
    }

    fun onConnectDevice(bleDevice: BluetoothDevice) {

        //start this bind service, which is BlueToothLeService for connecting

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
        Intent(requireActivity(), BlueToothLeService::class.java)
            .also {
                Log.d(Tag, "start Intent")
                requireActivity().bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
            }

    }

    fun onDisconnectDevice(bleDevice: BluetoothDevice) {
        if (bluetoothService == null || bluetoothService!!.bluetoothDevice != bleDevice) {
            Toast.makeText(requireContext(), "this device didn't connected", Toast.LENGTH_SHORT)
                .show()
            return
        }
        bluetoothService!!.disconnect()
    }

    fun onDetail(bleDevice: BluetoothDevice) {
        if (bluetoothService == null || bluetoothService!!.bluetoothDevice != bleDevice) {

            Toast.makeText(requireContext(), "this device didn't connected", Toast.LENGTH_SHORT)
                .show()
            return
        } else viewModel.deviceCallbackListener!!.onDetailCallBack(
            "this is connected device",
            bleDevice
        )

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun checkPermissions() {


        // check the bluetoothAdapter is enabled or not, if not, print a toast.
        //without this permission we can't do bluetooth communication
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(requireContext(), "please open bluetooth", Toast.LENGTH_LONG).show()
            return
        }
        /**
        request for location permission. without this permission, there will be no result for scan,
        because BLE scan has relationship with location  notice: if android version lower than 9,
        could declare AccessCoarseLocation.
        if declare your application is only support  Ble, declare this on manifest., or set it dynamic in code:
        <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
         */

        val permissions = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionDeniedList: MutableList<String> = ArrayList()
        //check permission list
        for (permission in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), permission)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission)
            } else {
                permissionDeniedList.add(permission)
            }
        }
        //if has some permission denied, request permission from user
        if (!permissionDeniedList.isEmpty()) {
            val deniedPermissions = permissionDeniedList.toTypedArray()
            ActivityCompat.requestPermissions(
                requireActivity(),
                deniedPermissions,
                REQUEST_CODE_PERMISSION_LOCATION
            )
        }
    }

    //use to request permission by create dialog.
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onPermissionGranted(permission: String) {
        when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                val builder = AlertDialog.Builder(requireContext())
                builder
                    .setTitle("Need Permisstions")
                    .setMessage("please give the permission")
                    .setNegativeButton("cancel",
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialogue: DialogInterface?, p1: Int) {
                                dialogue?.dismiss()
                            }

                        })
                    .setPositiveButton("go to Setting",
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialogue: DialogInterface?, p1: Int) {
                                dialogue?.dismiss()
                                openAppSettings()
                            }
                        })
                    .show()
            } else {

                //after has permissions, set scan rules and start scan.
                startLEScan(true)
            }
        }
    }

    //if not get permission, and user want to open app setting, we can use this one.
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity?.packageName, null)
        intent.setData(uri)
        startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
    }

    //get the result of open app setting
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                startLEScan(true)
            }
        }
    }

    //check is Gps open
    private fun checkGPSIsOpen(): Boolean {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onGet(bleDevice: BluetoothDevice) {
//        deviceLiveData.observe(viewLifecycleOwner,{
//            viewAdapter.setDevice(it)
//        })
        viewAdapter.addDevice(bleDevice)
    }

    override fun onClear() {
        viewAdapter.clearScanDevice()
    }

    override fun onDetailClicked(bleDevice: BluetoothDevice) {
//        var bundle = Bundle()
//        bundle.putParcelable("device", bleDevice)
//
//        Navigation.findNavController(binding.root).navigate(
//            R.id.scanFragment_to_operationFragment,bundle
//        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * ble Advertising to broadcast data packages to all nearby devices without having to establish a connection first.
     * but ble advertising not support every device, use below method to check:
     * isLe2MPhySupported()
     * isLeCodedPhySupported()
     * isLeExtendedAdvertisingSupported()
     * isLePeriodicAdvertisingSupported()
     *
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun advertisingTest_1MPHY() {

        var currentAdvertisingSet: AdvertisingSet? = null

        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser

        val parameters = AdvertisingSetParameters.Builder()
            .setLegacyMode(true)//default is true
            .setConnectable(true)
            .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()
        val callback = object : AdvertisingSetCallback() {
            override fun onAdvertisingSetStarted(
                advertisingSet: AdvertisingSet,
                txPower: Int,
                status: Int
            ) {
                Log.d(
                    Tag, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: "
                            + status
                )
                currentAdvertisingSet = advertisingSet

            }

            override fun onAdvertisingDataSet(advertisingSet: AdvertisingSet?, status: Int) {
                Log.d(Tag, "onAdvertisingDataSet() :status:$status")
            }

            override fun onScanResponseDataSet(advertisingSet: AdvertisingSet?, status: Int) {
                Log.d(Tag, "onScanResponseDataSet(): status:$status")
            }

            override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet?) {
                Log.d(Tag, "onAdvertisingSetStopped():")
            }
        }
        advertiser.startAdvertisingSet(parameters, data, null, null, null, callback)

        // After onAdvertisingSetStarted callback is called, you can modify the
        // advertising data and scan response data:
        currentAdvertisingSet!!.setAdvertisingData(
            AdvertiseData.Builder().setIncludeDeviceName(true).setIncludeTxPowerLevel(true).build()
        )

        // Wait for onAdvertisingDataSet callback...
        currentAdvertisingSet!!.setScanResponseData(
            AdvertiseData.Builder().addServiceUuid(ParcelUuid(UUID.randomUUID())).build()
        )

        // Wait for onScanResponseDataSet callback...

        // When done with the advertising:
        advertiser.stopAdvertisingSet(callback)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun advertisingTest_2MPHY() {//2m phy is battery efficient

        var currentAdvertisingSet: AdvertisingSet? = null//had a gatt defined in it

        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        // when using BLE_2Mphy, must check support or not
        // Check if all features are supported
        if (!bluetoothAdapter.isLe2MPhySupported()) {
            Log.e(Tag, "2M PHY not supported!")
            return
        }
        if (!bluetoothAdapter.isLeExtendedAdvertisingSupported()) {
            Log.e(Tag, "LE Extended Advertising not supported!")
            return
        }
        var maxDataLength = bluetoothAdapter.getLeMaximumAdvertisingDataLength()

        val parameters = AdvertisingSetParameters.Builder()
            .setLegacyMode(false)
            .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
            .setPrimaryPhy(BluetoothDevice.PHY_LE_1M)
            .setSecondaryPhy(BluetoothDevice.PHY_LE_2M)

        var data = AdvertiseData.Builder().addServiceData(

            ParcelUuid(UUID.randomUUID()),
            "You should be able to fit large amounts of data up to maxDataLength. This goes up to 1650 bytes . For legacy advertising this would not work ".toByteArray()
        ).build()

        var callback = object : AdvertisingSetCallback() {
            override fun onAdvertisingSetStarted(
                advertisingSet: AdvertisingSet?,
                txPower: Int,
                status: Int
            ) {
                super.onAdvertisingSetStarted(advertisingSet, txPower, status)
                Log.i(
                    Tag, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: "
                            + status
                )
                currentAdvertisingSet = advertisingSet;
                Log.d(
                    Tag, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: "
                            + status
                )
                currentAdvertisingSet = advertisingSet
            }


            override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet?) {
                super.onAdvertisingSetStopped(advertisingSet)
                Log.d(Tag, "onAdvertisingSetStopped():")
            }


        }

        advertiser.startAdvertisingSet(parameters.build(), data, null, null, null, callback);
// After the set starts, you can modify the data and parameters of currentAdvertisingSet.
        var nextData=
            AdvertiseData.Builder().addServiceData(

                ParcelUuid(UUID.randomUUID()),
                "You should be able to fit large amounts of data up to maxDataLength. This goes up to 1650 bytes . For legacy advertising this would not work ".toByteArray()
            ).build()

        currentAdvertisingSet!!.setAdvertisingData(nextData

        )

        // Wait for onAdvertisingEnabled callback...
        currentAdvertisingSet!!.enableAdvertising(true, 0, 0);
        // Wait for onAdvertisingEnabled callback...

        // Or modify the parameters - i.e. lower the tx power
        currentAdvertisingSet!!.enableAdvertising(false, 0, 0);
        // Wait for onAdvertisingEnabled callback...
        currentAdvertisingSet!!.setAdvertisingParameters(
            parameters.setTxPowerLevel
                (AdvertisingSetParameters.TX_POWER_LOW).build()
        );
        // Wait for onAdvertisingParametersUpdated callback...
        currentAdvertisingSet!!.enableAdvertising(true, 0, 0);
        // Wait for onAdvertisingEnabled callback...

        // When done with the advertising:
        advertiser.stopAdvertisingSet(callback);

    }


}