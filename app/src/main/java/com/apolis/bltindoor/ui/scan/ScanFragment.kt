package com.apolis.bltindoor.ui.scan

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.apolis.bltindoor.R
import com.apolis.bltindoor.databinding.ScanFragmentBinding
import com.apolis.bltindoor.helper.DaggerAppComponent
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import kotlinx.android.synthetic.main.scan_fragment.*
import javax.inject.Inject


class ScanFragment : Fragment(), DeviceGetListener, OnConnectCallListener {
    lateinit var binding: ScanFragmentBinding

//set the request code for permission
    companion object {
        fun newInstance() = ScanFragment()
        private const val REQUEST_CODE_OPEN_GPS = 1
        private const val REQUEST_CODE_PERMISSION_LOCATION = 2

    }


//setup the viewmodel and adapter, set the call back listener
    private lateinit var viewModel: ScanViewModel
    private val viewAdapter = DeviceAdapter().apply {
        parentFragment = this@ScanFragment
        onConnectCallListener= this@ScanFragment
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


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ScanViewModel::class.java)
        // TODO: Use the ViewModel
        viewModel.deviceGetListener = this
        init()

    }
//set up the recycler view for blue tooth device.
    private fun init() {
        binding.deviceRecyclerView.apply {
            adapter = viewAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        //set scan, before we start, check permission first.
        btn_scan.setOnClickListener {
            checkPermissions()
        }

    }

    fun checkPermissions() {


        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // check the bluetoothAdapter is enabled or not, if not, print a toast.
        //without this permission we can't do bluetooth communication
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(requireContext(), "please open bluetooth", Toast.LENGTH_LONG).show()
            return
        }
        //request for location permission, without this permission, there will be no result for scan,
        //because BLE scan has relationship with location  notice: if android version lower than 9,
        //could declare AccessCoarseLocation.
        // if declare your application is only support  Ble, declare this on manifest., or set it dynamic in code
        //<uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
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
    private fun onPermissionGranted(permission: String) {
        when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                var builder = AlertDialog.Builder(requireContext())
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
                viewModel.setScanRule()
                viewModel.startScan()
            }
        }
    }
//if not get permission, and user want to open app setting, we can use this one.
    private fun openAppSettings() {
        var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        var uri = Uri.fromParts("package", activity?.packageName, null)
        intent.setData(uri)
        startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
    }
//get the result of open app setting
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                viewModel.setScanRule()
                viewModel.startScan()
            }
        }
    }
//check is Gps open
    private fun checkGPSIsOpen(): Boolean {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onGet(bleDevice: BleDevice) {
//        deviceLiveData.observe(viewLifecycleOwner,{
//            viewAdapter.setDevice(it)
//        })
        viewAdapter.addDevice(bleDevice)
    }

    override fun onClear() {
        viewAdapter.clearScanDevice()
    }

    override fun onDetailClicked(bleDevice: BleDevice) {
//        var bundle = Bundle()
//        bundle.putParcelable("device", bleDevice)
//
//        Navigation.findNavController(binding.root).navigate(
//            R.id.scanFragment_to_operationFragment,bundle
//        )
    }
    //from start to here is for ask bluetooth and location permissions. now set scan rules and start scan


}