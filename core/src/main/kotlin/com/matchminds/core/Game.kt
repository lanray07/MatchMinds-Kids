package com.matchminds.core

import java.text.Normalizer
import java.util.Locale
import kotlin.random.Random

enum class Mode(val title: String, val subtitle: String, val icon: String) {
    ANIMALS("Animal friends", "Meet a new memory buddy", "fox"),
    SHAPES("Shapes & colours", "A little shape discovery", "star"),
    NUMBERS("Number garden", "Count, look and match", "number"),
    LETTERS("Letter meadow", "Big letters, little letters", "letter"),
    OBJECTS("Everyday wonders", "Discover familiar things", "book"),
    PATTERNS("Pattern paths", "What comes next?", "pattern")
}
enum class Difficulty(val title: String, val guidance: String, val pairs: Int, val revealMs: Long) {
    LITTLE("Little Explorer", "Around ages 3–4", 3, 1600),
    GROWING("Growing Thinker", "Around ages 5–6", 6, 1100),
    CHAMPION("Memory Champion", "Around ages 7–8", 10, 850)
}
data class Face(val art: String, val label: String, val quantity: Int = 0)
data class Card(val id: Int, val pair: String, val face: Face)
data class Board(
    val seed: Int, val mode: Mode, val difficulty: Difficulty, val level: Int,
    val cards: List<Card>, val revealed: Set<Int> = emptySet(), val matched: Set<Int> = emptySet(),
    val moves: Int = 0, val hints: Int = 0, val paused: Boolean = false
) {
    val resolving get() = revealed.size == 2
    val complete get() = matched.size == cards.size
    fun flip(id: Int): Board {
        if (paused || complete || resolving || id in revealed || id in matched || cards.none { it.id == id }) return this
        val next = revealed + id
        return copy(revealed = next, moves = moves + if (next.size == 2) 1 else 0)
    }
    fun isMatch(): Boolean = resolving && cards.filter { it.id in revealed }.map { it.pair }.distinct().size == 1
    fun resolve(): Board = if (!resolving || paused) this else copy(
        matched = if (isMatch()) matched + revealed else matched, revealed = emptySet()
    )
    fun hint(): Set<Int> = cards.filter { it.id !in matched }.groupBy { it.pair }.values.firstOrNull()?.map { it.id }?.toSet() ?: emptySet()
    val stars get() = if (hints == 0 && moves <= cards.size) 3 else if (moves <= cards.size * 2) 2 else 1
}

object Decks {
    val animals = listOf("lion", "elephant", "giraffe", "zebra", "monkey", "rabbit", "fox", "panda", "penguin", "dolphin", "cat", "dog")
    val shapes = listOf("circle", "square", "triangle", "rectangle", "star", "heart", "oval", "diamond")
    val objects = listOf("shoes", "cup", "spoon", "chair", "book", "backpack", "bicycle", "ball", "toothbrush", "clock")
    private val colours = listOf("blue", "coral", "green", "purple")
    fun create(mode: Mode, difficulty: Difficulty, level: Int, seed: Int): Board {
        require(level in 1..12)
        require(mode != Mode.PATTERNS)
        val random = Random(seed)
        val pool: List<Pair<Face, Face>> = when (mode) {
            Mode.ANIMALS -> animals.map { Face(it, it.replaceFirstChar(Char::uppercase)) }.map { it to it }
            Mode.SHAPES -> shapes.flatMap { shape -> colours.map { colour -> Face("$colour:$shape", "$colour $shape") } }.map { it to it }
            Mode.NUMBERS -> (1..if (difficulty == Difficulty.CHAMPION) 20 else 10).map {
                Face("digit", "$it", it) to Face("quantity", if(it==1) "1 star" else "$it stars", it)
            }
            Mode.LETTERS -> ('A'..'Z').map { Face("letter", "$it") to Face("letter", it.lowercase()) }
            Mode.OBJECTS -> objects.map { Face(it, it.replaceFirstChar(Char::uppercase)) }.map { it to it }
            else -> error("Patterns use their own rule engine")
        }
        val pairs = if (difficulty == Difficulty.LITTLE && level == 1) 2 else difficulty.pairs
        val cards = pool.shuffled(random).take(pairs).flatMapIndexed { index, (a, b) ->
            listOf(Card(index * 2, "$index", a), Card(index * 2 + 1, "$index", b))
        }.shuffled(random)
        return Board(seed, mode, difficulty, level, cards)
    }
}
data class PatternPuzzle(val sequence: List<Face>, val answer: Face, val options: List<Face>)
object Patterns {
    fun create(difficulty: Difficulty, level: Int, seed: Int): PatternPuzzle {
        val random = Random(seed + level)
        val pool = (if (level % 2 == 0) Decks.animals else Decks.shapes).shuffled(random).take(3).map { Face(it, it) }
        val rule = when (difficulty) {
            Difficulty.LITTLE -> listOf(0, 1)
            Difficulty.GROWING -> listOf(0, 0, 1)
            Difficulty.CHAMPION -> listOf(0, 1, 2, 1)
        }
        val length = if (difficulty == Difficulty.CHAMPION) 7 else 5
        return PatternPuzzle(List(length) { pool[rule[it % rule.size]] }, pool[rule[length % rule.size]], pool.shuffled(random))
    }
}
object Access {
    fun allowed(premium: Boolean, mode: Mode, difficulty: Difficulty, level: Int) =
        premium || (mode in listOf(Mode.ANIMALS, Mode.SHAPES) && difficulty == Difficulty.LITTLE && level <= 3)
}
enum class Command { START, ANIMALS, AGAIN, NEXT, REPEAT, HELP, PAUSE, BACK }
object Commands {
    private val english = mapOf("start game" to Command.START, "animals" to Command.ANIMALS,
        "play again" to Command.AGAIN, "next level" to Command.NEXT, "repeat" to Command.REPEAT,
        "help" to Command.HELP, "pause" to Command.PAUSE, "go back" to Command.BACK)
    fun parse(text: String, locale: String): Command? {
        if (!locale.startsWith("en")) return null
        val normal = Normalizer.normalize(text, Normalizer.Form.NFKC).lowercase(Locale.ROOT).trim().trimEnd('.', '!', '?')
        return english[normal]
    }
}
data class VerifiedReceipt(val sku: String, val userId: String, val receiptId: String, val cancelled: Boolean)
object Entitlements {
    const val SKU = "matchminds.premium.lifetime"
    fun accepts(receipt: VerifiedReceipt, expectedUser: String, expectedReceipt: String) =
        receipt.sku == SKU && receipt.userId == expectedUser && receipt.receiptId == expectedReceipt
}
