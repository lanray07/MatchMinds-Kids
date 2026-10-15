package com.matchminds.kids

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.matchminds.core.*
import com.matchminds.kids.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

enum class Screen { SPLASH, WELCOME, PROFILES, WORLDS, MODES, GAME, COMPLETE, REWARDS, PROGRESS, GATE, PARENTS, PREMIUM, LANGUAGE, ACCESSIBILITY }
data class PlayState(val screen: Screen = Screen.SPLASH, val world: Int = 0, val mode: Mode = Mode.ANIMALS,
    val board: Board? = null, val pattern: PatternPuzzle? = null, val patternSeed: Int = 0, val patternLevel: Int = 1,
    val round: Int = 0, val mistakes: Int = 0, val patternBusy: Boolean = false, val patternPaused: Boolean = false,
    val message: String = "Find two cards that belong together.", val lastStars: Int = 3, val hint: Set<Int> = emptySet(),
    val parentSession: Boolean = false, val reminder: Boolean = false, val patternDifficulty: Difficulty = Difficulty.LITTLE)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val store = LocalStore(application)
    val local = store.state.stateIn(viewModelScope, SharingStarted.Eagerly, LocalState())
    private val mutable = MutableStateFlow(PlayState())
    val play = mutable.asStateFlow()
    private var resolveJob: Job? = null
    private var reminderJob: Job? = null
    var qaPremium = false
    val premium get() = local.value.premium || (BuildConfig.DEBUG && qaPremium)
    init { viewModelScope.launch { delay(850); navigate(Screen.WELCOME) } }
    fun navigate(screen: Screen) {
        val guarded = screen in setOf(Screen.PARENTS, Screen.PREMIUM, Screen.LANGUAGE, Screen.ACCESSIBILITY)
        mutable.update { it.copy(screen = if (guarded && !it.parentSession) Screen.GATE else screen) }
    }
    fun worlds() { mutable.update { it.copy(screen = Screen.WORLDS, parentSession = false) } }
    fun world(id: Int) { mutable.update { it.copy(world = id, screen = Screen.MODES) } }
    fun selectProfile(id: Int) { update { it.copy(selected = id) }; worlds() }
    fun update(transform: (LocalState) -> LocalState) { viewModelScope.launch { store.update(transform) } }
    fun settings(transform: (Settings) -> Settings) = update { it.copy(settings = transform(it.settings)) }
    fun profileDifficulty(d: Difficulty) = update { s -> s.copy(profiles = s.profiles.map { if (it.id == s.selected) it.copy(difficulty = d) else it }) }
    fun addProfile(name: String, avatar: String) = update { s -> if (s.profiles.size >= 4) s else {
        val id = (s.profiles.maxOfOrNull { it.id } ?: 0) + 1
        s.copy(profiles = s.profiles + Profile(id, name.trim().take(16).ifBlank { "Explorer $id" }, avatar), selected = id)
    } }
    fun removeProfile(id: Int) = update { s -> if (s.profiles.size == 1) s else s.copy(profiles = s.profiles.filter { it.id != id },
        selected = if (s.selected == id) s.profiles.first { it.id != id }.id else s.selected,
        scores = s.scores.filterKeys { !it.startsWith("$id/") }, saved = s.saved?.takeUnless { it.profile == id }) }
    fun eraseProgress() { resolveJob?.cancel(); mutable.update { it.copy(board = null, pattern = null) }; update { it.copy(scores = emptyMap(), saved = null) } }
    fun eraseAll() { resolveJob?.cancel(); mutable.value = PlayState(screen = Screen.WELCOME); update { LocalState(premium = it.premium, storeUser = it.storeUser) } }
    fun start(mode: Mode, level: Int = local.value.nextLevel(mode, play.value.world), seed: Int = Random.nextInt()) {
        val d = local.value.profile.difficulty
        if (!Access.allowed(premium, mode, d, level)) {
            mutable.update { it.copy(message = "Ask a grown-up to choose another adventure.") }; return
        }
        resolveJob?.cancel()
        mutable.update { old -> PlayState(screen = Screen.GAME, world = old.world, mode = mode,
            board = if (mode == Mode.PATTERNS) null else Decks.create(mode, d, level, seed),
            pattern = if (mode == Mode.PATTERNS) Patterns.create(d, level, seed) else null,
            patternSeed = seed, patternLevel = level, patternDifficulty = d,
            message = if (mode == Mode.PATTERNS) "Look at the pattern. What comes next?" else "Find two cards that belong together.") }
        save()
        if (local.value.settings.reminderMinutes > 0 && reminderJob?.isActive!=true) reminderJob = viewModelScope.launch {
            delay(local.value.settings.reminderMinutes * 60000L)
            if(local.value.settings.reminderMinutes>0 && play.value.screen==Screen.GAME) { pause(); mutable.update { it.copy(reminder = true) } }
        }
    }
    fun resumeSaved() {
        val save = local.value.saved?.takeIf { it.profile == local.value.selected } ?: return
        val mode = save.board?.mode ?: Mode.PATTERNS
        val d = save.board?.difficulty ?: save.patternDifficulty
        if (!Access.allowed(premium, mode, d, save.board?.level ?: save.patternLevel)) return
        mutable.value = PlayState(screen = Screen.GAME, world = save.world, mode = mode, board = save.board?.copy(paused = false),
            pattern = if (mode == Mode.PATTERNS) Patterns.create(d, save.patternLevel, save.patternSeed + save.patternRound) else null,
            patternSeed = save.patternSeed, patternLevel = save.patternLevel, round = save.patternRound, mistakes = save.patternMistakes, patternDifficulty = d)
        if (play.value.board?.complete == true || (mode == Mode.PATTERNS && save.patternRound >= 5)) finish() else resolveIfNeeded()
    }
    fun flip(id: Int) {
        val current = play.value.board ?: return
        val next = current.flip(id)
        if (next == current) return
        mutable.update { it.copy(board = next, hint = emptySet(), message = if (next.resolving) if (next.isMatch()) "Wonderful! You found a match." else "Great effort! Try another card." else "Can you find the other ${next.cards.first { c -> c.id == id }.face.label.lowercase()}?") }
        save(); resolveIfNeeded()
    }
    private fun resolveIfNeeded() {
        val b = play.value.board ?: return
        if (!b.resolving || b.paused) return
        resolveJob?.cancel(); resolveJob = viewModelScope.launch {
            delay(if (local.value.settings.reducedMotion) 450 else b.difficulty.revealMs)
            mutable.update { it.copy(board = it.board?.resolve()) }; save()
            if (play.value.board?.complete == true) finish()
        }
    }
    fun answer(face: Face) {
        val p = play.value.pattern ?: return
        if (play.value.patternBusy || play.value.patternPaused) return
        if (face != p.answer) { mutable.update { it.copy(mistakes = it.mistakes + 1, message = "Great effort! Look at how the pattern repeats.") }; save(); return }
        mutable.update { it.copy(patternBusy = true, message = "Wonderful! You found the pattern.") }
        resolveJob = viewModelScope.launch {
            delay(700)
            val next = play.value.round + 1
            if (next >= 5) { mutable.update { it.copy(round = next, patternBusy = false) }; finish() } else {
                mutable.update { it.copy(round = next, patternBusy = false, pattern = Patterns.create(it.patternDifficulty, it.patternLevel, it.patternSeed + next), message = "What comes next?") }; save()
            }
        }
    }
    private fun finish() {
        if (play.value.screen == Screen.COMPLETE) return
        val p = play.value
        val stars = p.board?.stars ?: if (p.mistakes == 0) 3 else if (p.mistakes < 5) 2 else 1
        val key = local.value.key(p.mode, p.world, p.board?.difficulty ?: p.patternDifficulty, p.board?.level ?: p.patternLevel)
        update { s -> s.copy(scores = s.scores + (key to maxOf(s.scores[key] ?: 0, stars)), saved = null) }
        mutable.update { it.copy(screen = Screen.COMPLETE, lastStars = stars, message = "You finished the level!") }
    }
    fun hint() {
        val b = play.value.board ?: return
        if (b.paused || b.resolving || b.complete) return
        mutable.update { it.copy(hint = b.hint(), board = b.copy(hints = b.hints + 1), message = "These two cards belong together.") }; save()
        viewModelScope.launch { delay(1600); mutable.update { it.copy(hint = emptySet()) } }
    }
    fun pause() {
        resolveJob?.cancel(); mutable.update { it.copy(board = it.board?.copy(paused = true), patternPaused = true, patternBusy = false, parentSession = false) }; save()
    }
    fun resume() { mutable.update { it.copy(board = it.board?.copy(paused = false), patternPaused = false, reminder = false) }; resolveIfNeeded() }
    fun background() { reminderJob?.cancel(); if (play.value.screen == Screen.GAME) pause(); mutable.update { it.copy(parentSession = false, screen = if (it.screen in setOf(Screen.PARENTS, Screen.PREMIUM, Screen.LANGUAGE, Screen.ACCESSIBILITY)) Screen.GATE else it.screen) } }
    private fun save() {
        val p = play.value
        if (p.screen != Screen.GAME) return
        val saved = SavedGame(local.value.selected, p.world, p.board, p.patternSeed, p.patternLevel, p.round, p.mistakes,p.patternDifficulty)
        update { it.copy(saved = saved) }
    }
    suspend fun authenticate(pin: String, confirmation: String): Boolean = withContext(Dispatchers.Default) {
        val s = local.value
        if(s.pinLockUntil > System.currentTimeMillis()) return@withContext false
        if (s.pinHash.isBlank()) {
            if (pin.length != 6 || pin != confirmation || pin.toSet().size < 3) return@withContext false
            val salt = ParentPin.salt(); val hash = ParentPin.hash(pin, salt)
            store.update { it.copy(pinSalt = salt, pinHash = hash) }
        } else if (!ParentPin.verify(pin, s.pinSalt, s.pinHash)) {
            store.update { it.copy(pinFailures=it.pinFailures+1,pinLockUntil=if(it.pinFailures>=2) System.currentTimeMillis()+30000 else 0) }
            return@withContext false
        }
        store.update { it.copy(pinFailures=0,pinLockUntil=0) }
        mutable.update { it.copy(parentSession = true, screen = Screen.PARENTS) }; true
    }
}
