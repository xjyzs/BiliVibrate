package com.xjyzs.bilivibrate

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
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
        // 替换竖屏视频界面为横屏
        XposedHelpers.findAndHookMethod(
            Instrumentation::class.java,
            "execStartActivity",
            Context::class.java,
            IBinder::class.java,
            IBinder::class.java,
            Activity::class.java,
            Intent::class.java,
            Int::class.javaPrimitiveType,
            Bundle::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        val intent = param.args[4] as? Intent ?: return
                        val component = intent.component ?: return
                        val className = component.className
                        if (className == "com.bilibili.video.story.StoryVideoActivity") {
                            val newComponent = ComponentName(
                                "com.bilibili.app.in",
                                "com.bilibili.ship.theseus.detail.UnitedBizDetailsActivity"
                            )
                            intent.component = newComponent
                            intent.extras?.let { extras ->
                                if (extras.containsKey("id")) {
                                    val idValue = extras.get("id")
                                    intent.removeExtra("id")
                                    intent.putExtra("aid", idValue as String)
                                }
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        )
        var vibrator: Vibrator? = null
        // 长按
        XposedHelpers.findAndHookMethod(
            "com.bilibili.ship.theseus.united.player.TripleSpeedService",
            lpparam.classLoader, "n",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        param.result = null
                        val context = XposedHelpers.getObjectField(param.thisObject, "e") as Context
                        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        val effectClick =
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        vibrator!!.vibrate(effectClick)
                    } catch (_: Exception) {
                    }
                }
            })
        // 松手
        XposedBridge.hookAllMethods(
            XposedHelpers.findClass(
                "com.bilibili.ship.theseus.united.player.TripleSpeedService",
                lpparam.classLoader
            ), "h",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        val effectClick =
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        vibrator!!.vibrate(effectClick)
                    } catch (_: Exception) {
                    }
                }
            })
        // 直播间双击点赞
        XposedHelpers.findAndHookMethod(
            "com.bilibili.bililive.room.ui.doubleclicklike.LiveRoomDoubleClickView",
            lpparam.classLoader, "a3",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    try {
                        param.result = null
                        val context = XposedHelpers.callMethod(param.thisObject, "o0") as Context
                        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        XposedHelpers.setObjectField(param.thisObject, "q", vibrator)
                        val effectClick =
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        vibrator.vibrate(effectClick)
                    } catch (_: Exception) {
                    }
                }
            })
    }
}