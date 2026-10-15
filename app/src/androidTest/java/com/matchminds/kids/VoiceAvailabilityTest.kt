package com.matchminds.kids

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.matchminds.kids.ui.Translations
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class VoiceAvailabilityTest {
    @Test fun probeInstalledOfflineVoicesAndSynthesis() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val context=instrumentation.targetContext
        Translations.load(context)
        val initialized=CountDownLatch(1)
        var tts:TextToSpeech?=null
        var ready=false
        instrumentation.runOnMainSync {tts=TextToSpeech(context) {status -> ready=status==TextToSpeech.SUCCESS;initialized.countDown()}}
        initialized.await(15,TimeUnit.SECONDS)
        val report=JSONObject().put("initialized",ready).put("method","Installed offline voice metadata plus completed synthesis; pronunciation quality still needs human review")
        try {
            for(locale in Translations.languages.keys) {
                val voice=if(ready) tts?.voices?.filter {!it.isNetworkConnectionRequired && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features.orEmpty() && it.locale.language==Locale.forLanguageTag(locale).language}?.sortedByDescending {it.locale.country==Locale.forLanguageTag(locale).country}?.firstOrNull() else null
                val entry=JSONObject().put("offlineVoiceAvailable",voice!=null)
                if(voice!=null) {
                    val done=CountDownLatch(1)
                    var completed=false
                    instrumentation.runOnMainSync {
                        tts?.setOnUtteranceProgressListener(object:UtteranceProgressListener() {
                            override fun onStart(id:String?) {}
                            override fun onDone(id:String?) {completed=true;done.countDown()}
                            override fun onError(id:String?) {done.countDown()}
                        })
                        tts?.voice=voice
                        tts?.speak(Translations.text("Find two cards that belong together.",locale),TextToSpeech.QUEUE_FLUSH,Bundle().apply {putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME,.5f)},locale)
                    }
                    done.await(12,TimeUnit.SECONDS)
                    entry.put("synthesisCompleted",completed)
                }
                report.put(locale,entry)
            }
        } finally {instrumentation.runOnMainSync {tts?.stop();tts?.shutdown()}}
        File(context.cacheDir,"voice-availability.json").writeText(report.toString(2))
        android.util.Log.i("MatchMindsVoiceReport",report.toString())
    }
}
