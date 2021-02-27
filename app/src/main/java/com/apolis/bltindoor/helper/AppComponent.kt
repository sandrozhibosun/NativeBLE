package com.apolis.bltindoor.helper

import com.apolis.bltindoor.ui.operation.OperationFragment
import com.apolis.bltindoor.ui.operation.OperationViewModel
import com.apolis.bltindoor.ui.scan.ScanFragment
import com.apolis.bltindoor.ui.scan.ScanViewModel

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(BLEManagerModule::class))
interface AppComponent {
    fun inject(scanViewModel: ScanViewModel)
    fun inject(operationFragment: OperationFragment)
    fun inject(scanFragment: ScanFragment)
}