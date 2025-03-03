package com.module.core.utils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/** SDK 26 - O*/
fun isSdk26() = isSdkO()

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun isSdkO() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

/** SDK 29 - Q*/
fun isSdk29() = isSdkQ()

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
fun isSdkQ() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

/** SDK 30 - R*/
fun isSdk30() = isSdkR()

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun isSdkR() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

/** SDK 31 - S*/
fun isSdk31() = isSdkS()

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun isSdkS() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** SDK 33 - TIRAMISU*/
fun isSdk33() = isSdkTIRAMISU()

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun isSdkTIRAMISU() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU


/** SDK 34 - UP_SIDE_DOWN_CAKE*/
fun isSdk34() = isSdkUpSideDownCake()

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun isSdkUpSideDownCake() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

/** SDK 35 - VANILLA_ICE_CREAM*/
fun isSdk35() = isSdkUpVanillaIceCream()

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun isSdkUpVanillaIceCream() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM