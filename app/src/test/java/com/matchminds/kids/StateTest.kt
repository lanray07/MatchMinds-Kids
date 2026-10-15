package com.matchminds.kids

import com.matchminds.core.*
import com.matchminds.kids.data.*
import org.junit.Assert.*
import org.junit.Test

class StateTest {
    @Test fun roundTripRestoresProfilesSettingsStarsAndAnUnfinishedBoard() {
        var b=Decks.create(Mode.NUMBERS,Difficulty.GROWING,2,44)
        val pair=b.cards.groupBy {it.pair}.values.first()
        b=b.flip(pair[0].id).flip(pair[1].id).resolve()
        b=b.flip(b.cards.first {it.id !in b.matched}.id)
        val s=LocalState(profiles=listOf(Profile(9,"Cub","panda",Difficulty.GROWING)),selected=9,
            settings=Settings(voice=false,reducedMotion=true,locale="en-US"),saved=SavedGame(9,1,b),scores=mapOf("9/1/NUMBERS/GROWING/1" to 3),pinFailures=3,pinLockUntil=12345)
        assertEquals(s.copy(saved=s.saved!!.copy(board=b.copy(paused=true))),StateJson.decode(StateJson.encode(s)))
    }
    @Test fun corruptOrPartialSavesFallBackToPlayableDefaults() {
        assertEquals(LocalState(),StateJson.decode("broken"));assertEquals(LocalState(),StateJson.decode("{}"))
        val source=StateJson.encode(LocalState())
        assertEquals(1,StateJson.decode(source.replace("\"selected\":1","\"selected\":999")).selected)
    }
    @Test fun completedLevelsAreScopedToTheProfileWorldModeAndDifficulty() {
        val s=LocalState(scores=mapOf("1/0/ANIMALS/LITTLE/1" to 3,"2/0/ANIMALS/LITTLE/1" to 2))
        assertEquals(3,s.stars);assertEquals(1,s.completed);assertEquals(2,s.nextLevel(Mode.ANIMALS,0));assertEquals(1,s.nextLevel(Mode.ANIMALS,1))
    }
    @Test fun pinUsesRandomSaltAndRejectsTheWrongValue() {
        val a=ParentPin.salt();val b=ParentPin.salt();assertNotEquals(a,b)
        val hash=ParentPin.hash("184729",a)
        assertNotEquals(hash,ParentPin.hash("184729",b));assertTrue(ParentPin.verify("184729",a,hash));assertFalse(ParentPin.verify("184720",a,hash))
    }
    @Test fun patternSaveRetainsOriginalDifficultyWhenParentChangesPreference() {
        val state=LocalState(profiles=listOf(Profile(1,"Cub","fox",Difficulty.CHAMPION)),saved=SavedGame(1,0,null,17,3,2,1,Difficulty.GROWING))
        val read=StateJson.decode(StateJson.encode(state))
        assertEquals(Difficulty.GROWING,read.saved!!.patternDifficulty)
        assertEquals(2,read.saved!!.patternRound)
        assertFalse(read.settings.voice)
    }
}
