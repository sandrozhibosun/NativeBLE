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


    companion object {
        fun newInstance() = ScanFragment()
        private const val REQUEST_CODE_OPEN_GPS = 1
        private const val REQUEST_CODE_PERMISSION_LOCATION = 2

    }


//    private val mDeviceAdapter: DeviceAdapter? = null
//    @Inject
//    lateinit var bleManager:BleManager

    private lateinit var viewModel: ScanViewModel
    private val viewAdapter = DeviceAdapter().apply {
        parentFragment = this@ScanFragment
        onConnectCallListener= this@ScanFragment
    }

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

    fun init() {
        binding.deviceRecyclerView.apply {
            adapter = viewAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        //set scan
        btn_scan.setOnClickListener {
            checkPermissions()
        }

    }

    fun checkPermissions() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(requireContext(), "please open bluetooth", Toast.LENGTH_LONG).show()
            return
        }

        val permissions = arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionDeniedList: MutableList<String> = ArrayList()
        for (permission in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), permission)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission)
            } else {
                permissionDeniedList.add(permission)
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            val deniedPermissions = permissionDeniedList.toTypedArray()
            ActivityCompat.requestPermissions(
                requireActivity(),
                deniedPermissions,
                REQUEST_CODE_PERMISSION_LOCATION
            )
        }
    }

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
                viewModel.setScanRule()
                viewModel.startScan()
            }
        }
    }

    private fun openAppSettings() {
        var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        var uri = Uri.fromParts("package", activity?.packageName, null)
        intent.setData(uri)
        startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (checkGPSIsOpen()) {
                viewModel.setScanRule()
                viewModel.startScan()
            }
        }
    }

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
        var bundle = Bundle()
        bundle.putParcelable("device", bleDevice)


        Navigation.findNavController(binding.root).navigate(
            R.id.action_scanFragment_to_operationFragment
        )
    }
    //from start to here is for ask bluetooth and location permissions. now set scan rules and start scan


}