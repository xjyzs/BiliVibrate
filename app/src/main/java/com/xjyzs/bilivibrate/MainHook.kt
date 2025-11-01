package com.xjyzs.bilivibrate

import android.annotation.SuppressLint
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("MissingPermission")
class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (!lpparam.packageName.equals("com.bilibili.app.in")) {
            return
        }
        var vibrator: Vibrator?=null
        try { // 长按
            XposedHelpers.findAndHookMethod("com.bilibili.ship.theseus.united.player.TripleSpeedService",
                lpparam.classLoader, "n",
                object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.result = null
                    val context = XposedHelpers.getObjectField(param.thisObject, "e") as Context
                    vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    val effectClick = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    vibrator.vibrate(effectClick)
                }
            })
        } catch (_: Exception) {
        }
        try { // 松手
            XposedBridge.hookAllMethods(XposedHelpers.findClass(
                "com.bilibili.ship.theseus.united.player.TripleSpeedService",
                lpparam.classLoader
            ),"h",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val effectClick = VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        vibrator!!.vibrate(effectClick)
                    }
                })
        } catch (_: Exception) {
        }
    }
}