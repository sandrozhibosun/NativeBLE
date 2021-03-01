package com.apolis.bltindoor.ui.scan

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.apolis.bltindoor.R
import com.apolis.bltindoor.ui.MainActivity
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class ScanFragmentTest{

    @get:Rule
    val activityTestRule:ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Before
    fun init(){
//        val bundle=Bundle()
//        scenario= FragmentScenario.launchInContainer<ScanFragment>(ScanFragment::class.java,bundle,
//        object :FragmentFactory(){
//            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
//                return super.instantiate(classLoader, className)
//            }
////        })
//        activityTestRule.activity.runOnUiThread(
//            object :Runnable{
//                override fun run() {
//                    var scanFragment=startScanFragment()
//                }
//            }
//        )
        onView(withId(R.id.btn_scan))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))
//        val fragmentArgs = Bundle()
//        androidx.fragment.app.testing.launchFragmentInContainer<ScanFragment>(fragmentArgs)

    }

    fun startScanFragment():ScanFragment{
        val activity= activityTestRule.activity
       val transaction=activity.supportFragmentManager.beginTransaction()
        val scanFragment=ScanFragment()
        transaction.add(scanFragment,"scanFragment")
        transaction.commit()
        return scanFragment

    }

//    @Test
//    fun is_button_Functional(){
//
//    }

}