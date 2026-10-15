package com.matchminds.core

import org.junit.Assert.*
import org.junit.Test

class GameTest {
    @Test fun everyGeneratedBoardIsSolvableAndCompletesExactlyOnce() {
        Mode.entries.filter { it != Mode.PATTERNS }.forEach { mode ->
            Difficulty.entries.forEach { difficulty -> (1..12).forEach { level -> (0..49).forEach { seed ->
                var board = Decks.create(mode, difficulty, level, seed)
                assertEquals(board, Decks.create(mode, difficulty, level, seed))
                assertEquals(board.cards.size, board.cards.map { it.id }.distinct().size)
                board.cards.groupBy { it.pair }.values.forEach { pair ->
                    assertEquals(2, pair.size)
                    board = board.flip(pair[0].id).flip(pair[1].id).resolve()
                }
                assertTrue(board.complete)
                assertEquals(board, board.flip(board.cards.first().id).resolve())
            } } }
        }
    }
    @Test fun invalidFlipsAndResolutionCannotCorruptState() {
        val board = Decks.create(Mode.ANIMALS, Difficulty.GROWING, 1, 42)
        val first = board.cards.first()
        val other = board.cards.first { it.pair != first.pair }
        val open = board.flip(first.id)
        assertEquals(open, open.flip(first.id))
        val two = open.flip(other.id)
        assertEquals(two, two.flip(board.cards.last().id))
        assertFalse(two.isMatch())
        assertEquals(emptySet<Int>(), two.resolve().matched)
        assertEquals(two.copy(paused = true), two.copy(paused = true).resolve())
        assertEquals(board, board.flip(-1))
    }
    @Test fun quantitiesAndLettersUseCorrectCorrespondence() {
        val board = Decks.create(Mode.NUMBERS, Difficulty.CHAMPION, 12, 9)
        board.cards.groupBy { it.pair }.values.forEach {
            assertEquals(it[0].face.quantity, it[1].face.quantity)
            assertTrue(it[0].face.quantity in 1..20)
            assertEquals(setOf("digit", "quantity"), it.map { c -> c.face.art }.toSet())
        }
    }
    @Test fun patternsFollowExplicitRulesAndHaveOneAnswer() {
        Difficulty.entries.forEach { d -> (1..12).forEach { l -> (0..99).forEach { s ->
            val p = Patterns.create(d, l, s)
            val expected = when (d) {
                Difficulty.LITTLE -> p.sequence[1]
                Difficulty.GROWING -> p.sequence[2]
                Difficulty.CHAMPION -> p.sequence[3]
            }
            assertEquals(expected, p.answer)
            assertEquals(1, p.options.count { it == p.answer })
        } } }
    }
    @Test fun voiceParserRejectsOpenEndedUtterances() {
        assertEquals(Command.START, Commands.parse(" Start game! ", "en-GB"))
        assertNull(Commands.parse("please buy premium", "en-GB"))
        assertNull(Commands.parse("start game", "fr"))
    }
    @Test fun accessRulesEnforcePremiumWithoutPurchaseSimulation() {
        assertTrue(Access.allowed(false, Mode.ANIMALS, Difficulty.LITTLE, 3))
        assertFalse(Access.allowed(false, Mode.ANIMALS, Difficulty.LITTLE, 4))
        assertFalse(Access.allowed(false, Mode.NUMBERS, Difficulty.LITTLE, 1))
        assertFalse(Access.allowed(false, Mode.SHAPES, Difficulty.CHAMPION, 1))
        assertTrue(Access.allowed(true, Mode.PATTERNS, Difficulty.CHAMPION, 12))
        val valid = VerifiedReceipt(Entitlements.SKU, "user", "receipt", false)
        assertTrue(Entitlements.accepts(valid, "user", "receipt"))
        assertFalse(Entitlements.accepts(valid.copy(sku = "wrong"), "user", "receipt"))
        assertFalse(Entitlements.accepts(valid, "other", "receipt"))
    }
}
