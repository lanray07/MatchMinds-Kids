package com.matchminds.kids

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.lifecycle.ViewModelProvider
import com.matchminds.core.Mode
import com.matchminds.kids.data.*
import com.matchminds.kids.ui.Translations
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.io.File

/** Host large-font checks supplement physical testing; they do not certify another Fire model. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34])
class AccessibilityCaptureTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test @Config(qualifiers="w600dp-h960dp-port-mdpi") fun portraitLargeText()=capture("tablet-portrait")
    @Test @Config(qualifiers="w960dp-h600dp-land-mdpi") fun landscapeLargeText()=capture("tablet-landscape")
    private fun capture(variant:String) {
        RuntimeEnvironment.setFontScale(1.5f)
        compose.activityRule.scenario.recreate()
        assertEquals(1.5f,compose.activity.resources.configuration.fontScale,0.001f)
        val vm=ViewModelProvider(compose.activity)[GameViewModel::class.java]
        val out=File("../artifacts/accessibility-renders/$variant").apply {mkdirs()}
        val overflow=JSONArray()
        var scenes=0
        fun record(locale:String,scene:String) {
            compose.waitForIdle()
            val nodes=compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult),useUnmergedTree=true).fetchSemanticsNodes()
            compose.runOnIdle {
                for(node in nodes) {
                    val results=mutableListOf<TextLayoutResult>()
                    node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
                    for(layout in results) if(layout.hasVisualOverflow) overflow.put(JSONObject().put("locale",locale).put("scene",scene).put("text",layout.layoutInput.text.text)
                        .put("layout_width",layout.size.width).put("layout_height",layout.size.height)
                        .put("last_line_bottom",if(layout.lineCount>0) layout.getLineBottom(layout.lineCount-1) else 0f)
                        .put("max_line_right",(0 until layout.lineCount).maxOfOrNull {layout.getLineRight(it)} ?: 0f)
                        .put("max_lines_exceeded",layout.multiParagraph.didExceedMaxLines))
                }
            }
            compose.onRoot().captureRoboImage(File(out,"$locale-$scene.png").path)
            scenes++
        }
        for(locale in Translations.languages.keys) {
            runBlocking {LocalStore(compose.activity).update {LocalState(settings=Settings(locale=locale,narration=false,sounds=false,reducedMotion=true))}}
            ShadowLooper.idleMainLooper(1500)
            compose.waitUntil(5000) {vm.local.value.settings.locale==locale}
            compose.runOnIdle {vm.navigate(Screen.WELCOME)}
            compose.onNodeWithText(Translations.text("Let’s play  →",locale)).performScrollTo().assertIsDisplayed()
            record(locale,"welcome")
            compose.runOnIdle {vm.start(Mode.ANIMALS,1,42)}
            compose.onNodeWithContentDescription(Translations.text("Pause game",locale)).assertIsDisplayed()
            record(locale,"game")
            compose.runOnIdle {vm.navigate(Screen.GATE)}
            record(locale,"parent-gate")
        }
        File(out,"audit.json").writeText(JSONObject().put("font_scale",1.5).put("scenes_rendered",scenes).put("text_layout_overflow",overflow).put("provenance","Robolectric API 34; not physical Fire device evidence").toString(2))
    }
}
