package com.matchminds.kids

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.matchminds.core.*
import com.matchminds.kids.data.*
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.io.File

/** Captures the real Activity and production composables under Robolectric Native Graphics.
 * These are host-rendered app screenshots, not physical Fire device captures. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34])
class StoreCaptureTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test @Config(qualifiers="w1280dp-h800dp-land-mdpi") fun tablet() = capture("tablet")
    @Test @Config(qualifiers="w360dp-h640dp-port-xhdpi") fun phone() = capture("phone")
    private fun capture(variant: String) {
        assumeTrue(System.getProperty("storeCapture")=="true")
        val vm=ViewModelProvider(compose.activity)[GameViewModel::class.java]
        runBlocking { LocalStore(compose.activity).update { LocalState(settings=Settings(narration=false,sounds=false,reducedMotion=true)) } }
        compose.runOnIdle { vm.qaPremium=true }
        ShadowLooper.idleMainLooper(2000)
        compose.runOnIdle { vm.navigate(Screen.WELCOME) }
        compose.waitForIdle()
        val out=File("../artifacts/screenshots/host-$variant").apply {mkdirs()}
        fun shoot(name: String) {
            compose.mainClock.advanceTimeBy(2500)
            compose.waitForIdle()
            val file=File(out,"$name-raw.png")
            compose.onRoot().captureRoboImage(file.absolutePath)
            check(file.exists() && file.length()>1000) { "Screenshot was not recorded: $file" }
        }
        shoot("01-hero")
        for((mode,name) in listOf(Mode.ANIMALS to "02-animals",Mode.SHAPES to "03-shapes",Mode.NUMBERS to "04-numbers",Mode.LETTERS to "05-letters")) {
            val seed=if(mode==Mode.SHAPES) (0..10000).first { candidate ->
                val faces=Decks.create(mode,Difficulty.LITTLE,2,candidate).cards.groupBy {it.pair}.values.take(2).map {it.first().face.art}
                "blue:circle" in faces && faces.any {it.endsWith(":star") && !it.startsWith("blue:")}
            } else 42
            compose.runOnIdle { vm.start(mode,2,seed) }
            compose.waitForIdle()
            val visiblePairs=vm.play.value.board!!.cards.groupBy {it.pair}.values.take(2)
            for(pair in visiblePairs) {
                compose.runOnIdle {vm.flip(pair[0].id);vm.flip(pair[1].id)}
                ShadowLooper.idleMainLooper(2000);compose.waitForIdle()
            }
            shoot(name)
        }
        compose.runOnIdle { vm.start(Mode.ANIMALS,2,42);vm.flip(vm.play.value.board!!.cards.first().id) }
        shoot("06-voice")
        compose.runOnIdle { vm.start(Mode.SHAPES,1,42);vm.flip(vm.play.value.board!!.cards.first().id) }
        shoot("07-offline")
        compose.runOnIdle { vm.start(Mode.ANIMALS,1,42) }
        compose.waitForIdle()
        val pairs=vm.play.value.board!!.cards.groupBy {it.pair}.values.toList()
        for(pair in pairs) {
            compose.runOnIdle {vm.flip(pair[0].id);vm.flip(pair[1].id)}
            ShadowLooper.idleMainLooper(2000)
            compose.mainClock.advanceTimeBy(2500)
            ShadowLooper.idleMainLooper(2000)
            compose.waitForIdle()
        }
        check(vm.play.value.screen==Screen.COMPLETE) { "Level did not complete: screen=${vm.play.value.screen}, board=${vm.play.value.board}" }
        compose.waitUntil(10000) {ShadowLooper.idleMainLooper(100);vm.local.value.completed>0}
        compose.runOnIdle { vm.navigate(Screen.REWARDS) }
        shoot("08-progress")
    }
}
