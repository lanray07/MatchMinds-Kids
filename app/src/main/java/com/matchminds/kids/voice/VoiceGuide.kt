package com.matchminds.kids.voice

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.*
import android.speech.tts.TextToSpeech
import com.matchminds.core.*
import com.matchminds.kids.BuildConfig
import com.matchminds.kids.R
import java.util.Locale

/** Uses installed offline voices only. No audio files or recogniser transcripts are persisted. */
class VoiceGuide(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var ready = false
    private var player: MediaPlayer? = null
    private var recognizer: SpeechRecognizer? = null
    var listening = false; private set
    val commandsAvailable get() = BuildConfig.VOICE_APPROVED && Build.VERSION.SDK_INT >= 31 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        ready = true; tts?.setSpeechRate(.88f); tts?.setPitch(1.08f)
    }
    fun speak(text: String, locale: String, volume: Float, enabled: Boolean) {
        if (!enabled || volume <= 0f) return
        stopAudio()
        val lang = Locale.forLanguageTag(locale)
        val voice = if (ready) tts?.voices?.filter { !it.isNetworkConnectionRequired && it.locale.language == lang.language }
            ?.sortedByDescending { if (it.locale.country == lang.country) 1 else 0 }?.firstOrNull() else null
        if (voice != null) {
            tts?.voice = voice
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle().apply { putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume) }, "milo")
        } else {
            val raw = when {
                text.startsWith("Welcome") -> R.raw.welcome
                text.startsWith("Wonderful") -> R.raw.match
                text.startsWith("Great effort") -> R.raw.try_again
                text.startsWith("You finished") -> R.raw.complete
                text.contains("pattern",ignoreCase=true) || text.startsWith("What comes next") -> R.raw.patterns
                else -> R.raw.instructions
            }
            player = MediaPlayer.create(context,raw)?.apply { setVolume(volume,volume); setOnCompletionListener { it.release(); player=null }; start() }
        }
    }
    fun listen(locale: String, onState: (Boolean, String) -> Unit, onCommand: (Command) -> Unit) {
        if (!commandsAvailable || listening) return
        stopAudio(); stopListening()
        if (Build.VERSION.SDK_INT >= 31) recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        val r = recognizer ?: return
        fun end(message: String) { listening=false; onState(false,message); r.cancel() }
        r.setRecognitionListener(object: RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { listening=true; onState(true,"Listening on this device…") }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { listening=false; onState(false,"Processing on this device…") }
            override fun onError(error: Int) { end("Voice unavailable. Use the buttons to keep playing.") }
            override fun onResults(results: Bundle?) {
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                val command = spoken?.let { Commands.parse(it,locale) }
                end(if(command==null) "Try: help, pause, repeat or go back." else "Command heard.")
                command?.let(onCommand)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        listening=true; onState(true,"Listening on this device…")
        runCatching { r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE,locale); putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1); putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false)
        }) }.onFailure { end("Voice unavailable. Use touch controls.") }
    }
    fun stopListening() { recognizer?.cancel(); recognizer?.destroy(); recognizer=null; listening=false }
    fun stopAudio() { tts?.stop(); player?.release(); player=null }
    fun close() { stopListening(); stopAudio(); tts?.shutdown(); tts=null }
}
