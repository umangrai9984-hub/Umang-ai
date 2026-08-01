package com.example.service

sealed class ParsedCommand {
    data class WhatsAppMessage(val recipient: String, val message: String) : ParsedCommand()
    data class SplitScreenApps(val app1: String, val app2: String) : ParsedCommand()
    data class BrightnessControl(val type: BrightnessType, val level: Int = 0) : ParsedCommand() {
        enum class BrightnessType { INCREASE, DECREASE, SET }
    }
    data class VolumeControl(val type: VolumeType, val level: Int = 0) : ParsedCommand() {
        enum class VolumeType { INCREASE, DECREASE, MUTE, MAX }
    }
    object LockPhone : ParsedCommand()
    data class ScrollAction(val direction: ScrollDirection) : ParsedCommand() {
        enum class ScrollDirection { UP, DOWN }
    }
    object OwnerInfo : ParsedCommand()
    data class GeneralQuery(val prompt: String) : ParsedCommand()
}

object VoiceCommandParser {

    fun parse(text: String): ParsedCommand {
        val lower = text.trim().lowercase()

        // Owner query handling
        if (lower.contains("owner") || lower.contains("created you") || lower.contains("who made you") ||
            lower.contains("who is umang rai") || lower.contains("your developer") || lower.contains("your master") || lower.contains("who is your creator")) {
            return ParsedCommand.OwnerInfo
        }

        // WhatsApp Command
        if (lower.contains("whatsapp") || (lower.contains("message") && (lower.contains("rohit") || lower.contains("to")))) {
            var contact = "Rohit"
            var message = "Hello from Umang AI!"

            // Extract contact name
            val whatsappRegex = Regex("(?:send|message)\\s+([a-zA-Z0-9_]+)\\s+(?:in|on|via)?\\s*whatsapp", RegexOption.IGNORE_CASE)
            val match1 = whatsappRegex.find(lower)
            if (match1 != null) {
                contact = match1.groupValues[1].capitalizeFirst()
            } else if (lower.contains("rohit")) {
                contact = "Rohit"
            }

            // Extract message payload if specified
            if (lower.contains("saying") || lower.contains("message") || lower.contains("that")) {
                val parts = lower.split(Regex("saying|message|that|text"))
                if (parts.size > 1 && parts[1].isNotBlank()) {
                    message = parts[1].trim().capitalizeFirst()
                }
            }

            return ParsedCommand.WhatsAppMessage(recipient = contact, message = message)
        }

        // Multitasking / Split Screen
        if (lower.contains("youtube") && lower.contains("instagram")) {
            return ParsedCommand.SplitScreenApps(app1 = "YouTube", app2 = "Instagram")
        }
        if ((lower.contains("split screen") || lower.contains("multitask") || lower.contains("open together")) && lower.contains("and")) {
            val parts = lower.replace("split screen", "").replace("open", "").split("and")
            val app1 = parts.getOrNull(0)?.trim()?.capitalizeFirst() ?: "YouTube"
            val app2 = parts.getOrNull(1)?.trim()?.capitalizeFirst() ?: "Instagram"
            return ParsedCommand.SplitScreenApps(app1 = app1, app2 = app2)
        }

        // Brightness Control
        if (lower.contains("brightness") || lower.contains("brighten") || lower.contains("dim screen")) {
            return when {
                lower.contains("increase") || lower.contains("up") || lower.contains("brighten") || lower.contains("more") ->
                    ParsedCommand.BrightnessControl(ParsedCommand.BrightnessControl.BrightnessType.INCREASE)
                lower.contains("decrease") || lower.contains("down") || lower.contains("dim") || lower.contains("less") ->
                    ParsedCommand.BrightnessControl(ParsedCommand.BrightnessControl.BrightnessType.DECREASE)
                lower.contains("%") -> {
                    val percent = Regex("(\\d+)%").find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 70
                    ParsedCommand.BrightnessControl(ParsedCommand.BrightnessControl.BrightnessType.SET, percent)
                }
                else -> ParsedCommand.BrightnessControl(ParsedCommand.BrightnessControl.BrightnessType.INCREASE)
            }
        }

        // Volume Control
        if (lower.contains("volume") || lower.contains("mute") || lower.contains("sound")) {
            return when {
                lower.contains("mute") -> ParsedCommand.VolumeControl(ParsedCommand.VolumeControl.VolumeType.MUTE)
                lower.contains("max") || lower.contains("full") -> ParsedCommand.VolumeControl(ParsedCommand.VolumeControl.VolumeType.MAX)
                lower.contains("increase") || lower.contains("up") || lower.contains("raise") || lower.contains("louder") ->
                    ParsedCommand.VolumeControl(ParsedCommand.VolumeControl.VolumeType.INCREASE)
                lower.contains("decrease") || lower.contains("down") || lower.contains("lower") || lower.contains("quiet") ->
                    ParsedCommand.VolumeControl(ParsedCommand.VolumeControl.VolumeType.DECREASE)
                else -> ParsedCommand.VolumeControl(ParsedCommand.VolumeControl.VolumeType.INCREASE)
            }
        }

        // Lock Phone
        if (lower.contains("lock phone") || lower.contains("lock screen") || lower.contains("turn off screen")) {
            return ParsedCommand.LockPhone
        }

        // Scroll Actions
        if (lower.contains("scroll up") || lower.contains("swipe up") || lower.contains("page up")) {
            return ParsedCommand.ScrollAction(ParsedCommand.ScrollAction.ScrollDirection.UP)
        }
        if (lower.contains("scroll down") || lower.contains("swipe down") || lower.contains("page down")) {
            return ParsedCommand.ScrollAction(ParsedCommand.ScrollAction.ScrollDirection.DOWN)
        }

        // Default to General AI Query
        return ParsedCommand.GeneralQuery(text)
    }

    private fun String.capitalizeFirst(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
