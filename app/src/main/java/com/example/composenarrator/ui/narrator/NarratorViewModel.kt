package com.example.composenarrator.ui.narrator

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.composenarrator.data.Sentence
import com.example.composenarrator.data.sampleText
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.BreakIterator
import java.util.*


@HiltViewModel
class NarratorViewModel(private val app: Application): AndroidViewModel(app) {

    var state by mutableStateOf(NarratorScreenState(sampleText))
    private set

    private var startOffset = 0
    private var currentSentenceIndex = 0

    private val sentences by lazy { getSentenceRange() }
    private var textToSpeech: TextToSpeech? = TextToSpeech(app) { status ->
        if (status == TextToSpeech.SUCCESS) {
            setupTts()
        }
    }

    private fun setupTts(){
        textToSpeech?.language = Locale.getDefault()
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(p0: String?) {
                state = state.copy(ttsRunning = true, ttsEnabled = false)
            }

            override fun onDone(p0: String?) {
                startOffset = 0
                state = state.copy(ttsRunning = false, ttsEnabled = true, ttsStartIndex = 0, ttsStopIndex = 0)
            }

            override fun onError(p0: String?) {
                //
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                state = state.copy(ttsRunning = false)
                Log.d("TTS", "Stop called")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                state = state.copy(ttsRunning = false)
                super.onError(utteranceId, errorCode)
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    val startIndex = start + startOffset
                    currentSentenceIndex = sentences.indexOfFirst { startIndex < it.endIndex }
                    state = state.copy(ttsStartIndex = sentences[currentSentenceIndex].startIndex, ttsStopIndex = sentences[currentSentenceIndex].endIndex)

            }
        })
    }

    fun onEvent(event: NarratorEvent) {
        when(event){
            NarratorEvent.Stop -> {
                startOffset = 0
                state = state.copy(ttsStartIndex = 0, ttsStopIndex = 0)
                textToSpeech?.stop()
            }
            is NarratorEvent.Start -> {
                speakText()
            }
            NarratorEvent.Pause -> {
                startOffset = state.ttsStartIndex
                textToSpeech?.stop()
            }
            is NarratorEvent.Restart -> {
                    startOffset = event.index ?: state.ttsStartIndex
                    speakText()
            }
            is NarratorEvent.UpdateTtsPitch -> {
                state = state.copy(ttsPitch = event.pitch)
            }
            is NarratorEvent.UpdateTtsSpeakRate ->{
                state = state.copy(ttsSpeakRate = event.speakRate)
            }
            NarratorEvent.FastForward ->{
                sentences.getOrNull(currentSentenceIndex+1)?.let{
                    startOffset = it.startIndex
                    speakText()
                }
            }
            NarratorEvent.Rewind -> {
                sentences.getOrNull(currentSentenceIndex-1)?.let{
                    startOffset = it.startIndex
                    speakText()
                }

            }
        }
    }

    private fun speakText() {
        state = state.copy(ttsEnabled = true)
        textToSpeech?.speak(state.text.substring(startOffset, state.text.lastIndex), TextToSpeech.QUEUE_FLUSH, null, "ID" )
    }

    private fun getSentenceRange(): List<Sentence>{
        val list = mutableListOf<Sentence>()
        val iterator: BreakIterator = BreakIterator.getSentenceInstance(Locale.getDefault())
        iterator.setText(state.text)
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            list.add(Sentence(start, end))
            start = end
            end = iterator.next()
        }
        return list
    }

    override fun onCleared() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

}

