package com.matchminds.kids

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.matchminds.core.*
import com.matchminds.kids.data.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
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
}
