package com.example.composenarrator.ui.narrator

sealed class NarratorEvent{
    object Start: NarratorEvent()
    object Stop: NarratorEvent()
    object Pause: NarratorEvent()
    object FastForward: NarratorEvent()
    object Rewind: NarratorEvent()
    data class Restart(val index: Int? = null): NarratorEvent()
    data class UpdateTtsPitch(val pitch: Float) : NarratorEvent()
    data class UpdateTtsSpeakRate(val speakRate: Float) : NarratorEvent()
}