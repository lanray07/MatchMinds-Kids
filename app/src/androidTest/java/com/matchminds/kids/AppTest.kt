package com.matchminds.kids

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.matchminds.core.*
import com.matchminds.kids.data.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import androidx.lifecycle.ViewModelProvider

@RunWith(AndroidJUnit4::class)
class AppTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Before fun resetTestProfile() {
        val vm=ViewModelProvider(compose.activity)[GameViewModel::class.java]
        compose.waitUntil(8000) {vm.play.value.screen!=Screen.SPLASH}
        runBlocking {LocalStore(compose.activity).update {LocalState(settings=Settings(locale="en-GB",narration=false,sounds=false,reducedMotion=true))}}
        compose.runOnIdle {vm.navigate(Screen.WELCOME)}
        compose.waitUntil(8000) {compose.onAllNodesWithText("Let’s play  →").fetchSemanticsNodes().isNotEmpty()}
    }
    @Test fun childCanReachAnOfflineGameAndPause() {
        compose.waitUntil(8000) { compose.onAllNodesWithText("Let’s play  →").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Let’s play  →").performClick()
        compose.onNodeWithText("That’s me!").performClick()
        compose.onAllNodesWithText("Explore  →")[0].performClick()
        compose.onAllNodesWithText("Play level 1  →")[0].performClick()
        compose.onNodeWithContentDescription("Card 1, face down").performClick()
        compose.onNodeWithContentDescription("Pause game").performClick()
        compose.onNodeWithText("Adventure paused").assertIsDisplayed()
        compose.onNodeWithText("Keep playing").performClick()
        compose.onNodeWithText("A little hint  ✦").assertIsDisplayed()
    }
    @Test fun persistenceRoundTripKeepsOpenAndMatchedCards() {
        var b=Decks.create(Mode.NUMBERS,Difficulty.GROWING,2,44)
        val pair=b.cards.groupBy {it.pair}.values.first()
        b=b.flip(pair[0].id).flip(pair[1].id).resolve()
        b=b.flip(b.cards.first {it.id !in b.matched}.id)
        val s=LocalState(saved=SavedGame(1,0,b),scores=mapOf("1/0/NUMBERS/GROWING/1" to 3))
        val read=StateJson.decode(StateJson.encode(s))
        assertEquals(b.copy(paused=true),read.saved!!.board)
        assertEquals(s.scores,read.scores)
        assertEquals(LocalState(),StateJson.decode("broken json"))
    }
    @Test fun pinHashesDoNotStoreThePin() {
        val salt=ParentPin.salt(); val hash=ParentPin.hash("184729",salt)
        assertFalse(hash.contains("184729"));assertTrue(ParentPin.verify("184729",salt,hash));assertFalse(ParentPin.verify("184720",salt,hash))
    }
    @Test fun parentSettingsCannotOpenWithoutAGate() {
        compose.waitUntil(8000) { compose.onAllNodesWithText("Parents").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Parents").performClick()
        compose.onNodeWithText("Grown-ups, let’s get set up").assertIsDisplayed()
        compose.onNodeWithText("Open parent area").assertIsNotEnabled()
    }
    @Test fun parentCanSetPrivatePinAndPersistFrench() {
        compose.onNodeWithText("Parents").performClick()
        val fields=compose.onAllNodes(hasSetTextAction())
        fields[0].performTextInput("184729")
        fields[1].performTextInput("184729")
        fields[2].performTextInput("I am the grown-up")
        compose.onNodeWithText("Open parent area").performClick()
        compose.waitUntil(10000) {compose.onAllNodesWithText("A little space for grown-ups").fetchSemanticsNodes().isNotEmpty()}
        compose.onNodeWithText("Language").performScrollTo().performClick()
        compose.onNodeWithText("Français").performScrollTo().performClick()
        compose.waitUntil(5000) {compose.onAllNodesWithText("Langue").fetchSemanticsNodes().isNotEmpty()}
        val stored=runBlocking {LocalStore(compose.activity).state.first()}
        assertEquals("fr",stored.settings.locale)
        assertTrue(ParentPin.verify("184729",stored.pinSalt,stored.pinHash))
        compose.activityRule.scenario.recreate()
        compose.waitUntil(10000) {compose.onAllNodesWithText("Réservé aux adultes").fetchSemanticsNodes().isNotEmpty()}
    }
}
