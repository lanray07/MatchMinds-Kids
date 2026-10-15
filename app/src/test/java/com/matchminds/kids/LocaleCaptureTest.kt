package com.matchminds.kids

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.matchminds.core.*
import com.matchminds.kids.data.*
import com.matchminds.kids.ui.Translations
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.io.File

/** Host rendering proves integration and script shaping, not native review or Fire compatibility. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34],qualifiers="w800dp-h1280dp-port-mdpi")
class LocaleCaptureTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun everyLanguageRendersRealWelcomeAndGameplay() {
        val vm=ViewModelProvider(compose.activity)[GameViewModel::class.java]
        val out=File("../artifacts/localization-renders").apply {mkdirs()}
        for(locale in Translations.languages.keys) {
            runBlocking { LocalStore(compose.activity).update {LocalState(settings=Settings(locale=locale,narration=false,sounds=false,reducedMotion=true))} }
            ShadowLooper.idleMainLooper(1500)
            compose.waitUntil(5000) {vm.local.value.settings.locale==locale}
            compose.runOnIdle {vm.navigate(Screen.WELCOME)}
            compose.waitForIdle()
            compose.onNodeWithText(Translations.text("Let’s play  →",locale)).assertExists()
            compose.onRoot().captureRoboImage(File(out,"$locale-welcome.png").path)
            compose.runOnIdle {vm.start(Mode.ANIMALS,1,42)}
            compose.waitForIdle()
            compose.onNodeWithText(Translations.text("Animal friends",locale)).assertExists()
            compose.onRoot().captureRoboImage(File(out,"$locale-game.png").path)
            compose.runOnIdle {vm.start(Mode.SHAPES,1,42); vm.flip(vm.play.value.board!!.cards.first().id)}
            compose.waitForIdle()
            compose.onNodeWithText(Translations.text("Shapes & colours",locale)).assertExists()
            compose.onRoot().captureRoboImage(File(out,"$locale-shapes.png").path)
        }
    }
}
