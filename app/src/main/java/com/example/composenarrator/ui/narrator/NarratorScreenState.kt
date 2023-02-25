package com.example.composenarrator.ui.narrator

data class NarratorScreenState(
    var text: String = "",
    var ttsEnabled: Boolean = false,
    var ttsRunning: Boolean = false,
    var ttsStartIndex: Int = 0,
    var ttsStopIndex: Int = 0,
    val ttsPitch: Float = 1f,
    val ttsSpeakRate: Float = 1f,
)