package com.matchminds.kids

import android.os.Debug
import android.os.SystemClock
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.matchminds.core.*
import com.matchminds.kids.data.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Debug-only gameplay fixture: no purchase, store reset, or production profile mutation. */
@RunWith(AndroidJUnit4::class)
class ExtendedDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun sustainedGameplayAndAccessibleControls() {
        val args=InstrumentationRegistry.getArguments()
        assumeTrue(args.getString("runExtendedQa")=="true")
        check(BuildConfig.DEBUG)
        val duration=args.getString("durationMs")?.toLong() ?: 900000L
        require(duration in 60000L..1800000L)
        val vm=ViewModelProvider(compose.activity)[GameViewModel::class.java]
        compose.activityRule.scenario.onActivity { it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
        compose.waitUntil(10000) {vm.play.value.screen!=Screen.SPLASH}
        runBlocking {LocalStore(compose.activity).update {LocalState(settings=Settings(locale="en-GB",narration=false,sounds=false,reducedMotion=true))}}
        compose.runOnIdle {vm.qaPremium=true;vm.navigate(Screen.WELCOME)}
        val started=SystemClock.elapsedRealtime()
        var games=0;var clicks=0;var controls=0;var maxPss=0L
        val covered=mutableSetOf<String>()
        val samples=org.json.JSONArray()
        while(SystemClock.elapsedRealtime()-started<duration) {
            val mode=Mode.entries[games%Mode.entries.size]
            val difficulty=Difficulty.entries[(games/Mode.entries.size)%Difficulty.entries.size]
            compose.runOnIdle {vm.profileDifficulty(difficulty)}
            compose.waitUntil(5000) {vm.local.value.profile.difficulty==difficulty}
            compose.runOnIdle {vm.start(mode,1+games%12,1000+games)}
            compose.waitForIdle()
            assertEquals(Screen.GAME,vm.play.value.screen)
            val density=compose.activity.resources.displayMetrics.density
            for(node in compose.onAllNodes(hasClickAction().and(isEnabled())).fetchSemanticsNodes()) {
                val label=if(node.config.contains(SemanticsProperties.ContentDescription)) node.config[SemanticsProperties.ContentDescription].joinToString(" ")
                    else if(node.config.contains(SemanticsProperties.Text)) node.config[SemanticsProperties.Text].joinToString(" ") {it.text} else null
                assertFalse("Enabled control has no accessible name: ${node.id}",label.isNullOrBlank())
                val bounds=node.touchBoundsInRoot
                assertTrue("Touch target too narrow: $label (${bounds.width/density}dp)",bounds.width/density>=47f)
                assertTrue("Touch target too short: $label (${bounds.height/density}dp)",bounds.height/density>=47f)
                controls++
            }
            compose.onNodeWithContentDescription("Pause game").performClick();clicks++
            compose.onNodeWithText("Adventure paused").assertIsDisplayed()
            compose.onNodeWithText("Keep playing").performClick();clicks++
            if(mode==Mode.PATTERNS) {
                repeat(5) {
                    val before=vm.play.value.round
                    val answer=vm.play.value.pattern!!.answer
                    compose.onNodeWithContentDescription("Choose ${answer.label}").performClick();clicks++
                    compose.waitUntil(5000) {vm.play.value.round>before || vm.play.value.screen==Screen.COMPLETE}
                }
            } else {
                val board=vm.play.value.board!!
                for(pair in board.cards.groupBy {it.pair}.values) {
                    for(card in pair) {
                        val index=board.cards.indexOf(card)+1
                        compose.onNodeWithContentDescription("Card $index, face down").performScrollTo().performClick();clicks++
                    }
                    compose.waitUntil(5000) {vm.play.value.board?.resolving==false}
                }
            }
            compose.waitUntil(5000) {vm.play.value.screen==Screen.COMPLETE}
            compose.waitUntil(5000) {vm.local.value.saved==null}
            covered.add("${mode.name}/${difficulty.name}")
            games++
            if(games%6==0) {
                val pss=Debug.getPss();maxPss=maxOf(maxPss,pss)
                samples.put(JSONObject().put("elapsed_ms",SystemClock.elapsedRealtime()-started).put("games",games).put("pss_kb",pss))
            }
            File(compose.activity.cacheDir,"extended-device-verification.json").writeText(JSONObject()
                .put("passed",false).put("status","running").put("games_completed",games)
                .put("elapsed_ms",SystemClock.elapsedRealtime()-started).toString(2))
        }
        assertEquals("All six activities and three difficulties must be exercised",18,covered.size)
        val result=JSONObject().put("passed",true).put("elapsed_ms",SystemClock.elapsedRealtime()-started)
            .put("games_completed",games).put("ui_clicks",clicks).put("accessible_controls_checked",controls)
            .put("mode_difficulty_combinations",covered.size).put("max_sampled_pss_kb",maxPss)
            .put("memory_samples",samples).put("premium_fixture","Debug qaPremium; not purchase evidence")
            .put("scope","Automated accessible names, >=47dp measured touch bounds, pause/resume, sustained UI gameplay. Activity recreation is covered separately. Not human TalkBack or child usability approval.")
        File(compose.activity.cacheDir,"extended-device-verification.json").writeText(result.toString(2))
    }
}
