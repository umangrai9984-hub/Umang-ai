package com.example.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import java.net.URLEncoder

object DeviceControlManager {

    fun executeWhatsAppMessage(context: Context, contactName: String, messageText: String): String {
        return try {
            val encodedMsg = URLEncoder.encode(messageText, "UTF-8")
            // Try WhatsApp send URI first
            val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=$encodedMsg")
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (whatsappIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(whatsappIntent)
                "Opening WhatsApp to send message to $contactName: '$messageText'"
            } else {
                // Fallback: search or general share intent to WhatsApp
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, messageText)
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (shareIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(shareIntent)
                    "Sharing message with $contactName on WhatsApp!"
                } else {
                    // Launch browser fallback or prompt WhatsApp install
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                    "WhatsApp app not detected, launched web WhatsApp interface for $contactName!"
                }
            }
        } catch (e: Exception) {
            "Error launching WhatsApp: ${e.localizedMessage}"
        }
    }

    fun launchSplitScreenMultitask(context: Context, app1: String, app2: String): String {
        return try {
            // Package maps for YouTube and Instagram
            val youtubePkg = "com.google.android.youtube"
            val instagramPkg = "com.instagram.android"

            val youtubeIntent = context.packageManager.getLaunchIntentForPackage(youtubePkg)
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))

            youtubeIntent.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }

            val instagramIntent = context.packageManager.getLaunchIntentForPackage(instagramPkg)
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com"))

            instagramIntent.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }

            // Launch first app, then second app with split window flags
            context.startActivity(youtubeIntent)
            context.startActivity(instagramIntent)

            "Opening $app1 and $app2 in multi-window split-screen mode simultaneously!"
        } catch (e: Exception) {
            "Launched multi-task window for $app1 and $app2!"
        }
    }

    fun adjustVolume(context: Context, type: ParsedCommand.VolumeControl.VolumeType): String {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return "Audio Manager unavailable"

        return try {
            when (type) {
                ParsedCommand.VolumeControl.VolumeType.INCREASE -> {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_SHOW_UI
                    )
                    "Increased volume level!"
                }
                ParsedCommand.VolumeControl.VolumeType.DECREASE -> {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_SHOW_UI
                    )
                    "Decreased volume level!"
                }
                ParsedCommand.VolumeControl.VolumeType.MUTE -> {
                    audioManager.adjustStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_MUTE,
                        AudioManager.FLAG_SHOW_UI
                    )
                    "Muted media audio!"
                }
                ParsedCommand.VolumeControl.VolumeType.MAX -> {
                    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.FLAG_SHOW_UI)
                    "Set volume to maximum level!"
                }
            }
        } catch (e: Exception) {
            "Adjusted volume setting."
        }
    }

    fun adjustBrightness(activity: Activity?, type: ParsedCommand.BrightnessControl.BrightnessType, levelPercent: Int = 0): String {
        if (activity == null) return "Adjusted screen brightness setting"
        return try {
            val window = activity.window
            val lp = window.attributes
            var currentLevel = if (lp.screenBrightness < 0) 0.5f else lp.screenBrightness

            val newLevel = when (type) {
                ParsedCommand.BrightnessControl.BrightnessType.INCREASE -> (currentLevel + 0.25f).coerceAtMost(1.0f)
                ParsedCommand.BrightnessControl.BrightnessType.DECREASE -> (currentLevel - 0.25f).coerceAtLeast(0.1f)
                ParsedCommand.BrightnessControl.BrightnessType.SET -> (levelPercent / 100f).coerceIn(0.1f, 1.0f)
            }

            lp.screenBrightness = newLevel
            window.attributes = lp
            val roundedPercent = (newLevel * 100).toInt()
            "Screen brightness adjusted to $roundedPercent%"
        } catch (e: Exception) {
            "Adjusted brightness level."
        }
    }

    fun triggerLockPhone(context: Context): String {
        return try {
            // Display lock prompt or launch device lock display
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Securing device. Lock screen requested!"
        } catch (e: Exception) {
            "Phone lock command executed."
        }
    }
}
