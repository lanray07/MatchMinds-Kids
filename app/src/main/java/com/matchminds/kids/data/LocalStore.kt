package com.matchminds.kids.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.matchminds.core.*
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.util.Base64

private val Context.store by preferencesDataStore("matchminds")
data class Profile(val id: Int, val name: String, val avatar: String, val difficulty: Difficulty = Difficulty.LITTLE)
data class Settings(val narration: Boolean = true, val volume: Float = .7f, val sounds: Boolean = true,
    val reducedMotion: Boolean = false, val voice: Boolean = false, val locale: String = "en-GB", val reminderMinutes: Int = 0)
data class SavedGame(val profile: Int, val world: Int, val board: Board?, val patternSeed: Int = 0,
    val patternLevel: Int = 1, val patternRound: Int = 0, val patternMistakes: Int = 0, val patternDifficulty: Difficulty = Difficulty.LITTLE)
data class LocalState(val profiles: List<Profile> = listOf(Profile(1, "Explorer", "fox")), val selected: Int = 1,
    val settings: Settings = Settings(), val scores: Map<String, Int> = emptyMap(), val saved: SavedGame? = null,
    val pinSalt: String = "", val pinHash: String = "", val premium: Boolean = false, val storeUser: String = "",
    val pinFailures: Int = 0, val pinLockUntil: Long = 0L) {
    val profile get() = profiles.first { it.id == selected }
    fun key(mode: Mode, world: Int, difficulty: Difficulty, level: Int) = "$selected/$world/${mode.name}/${difficulty.name}/$level"
    val stars get() = scores.filterKeys { it.startsWith("$selected/") }.values.sum()
    val completed get() = scores.keys.count { it.startsWith("$selected/") }
    fun nextLevel(mode: Mode, world: Int) = (1..12).firstOrNull { key(mode, world, profile.difficulty, it) !in scores } ?: 12
}
object StateJson {
    fun encode(s: LocalState): String = JSONObject().apply {
        put("profiles", JSONArray().apply { s.profiles.forEach { p -> put(JSONObject().put("id", p.id).put("name", p.name).put("avatar", p.avatar).put("difficulty", p.difficulty.name)) } })
        put("selected", s.selected); put("pinSalt", s.pinSalt); put("pinHash", s.pinHash)
        put("premium", s.premium); put("storeUser", s.storeUser)
        put("pinFailures", s.pinFailures); put("pinLockUntil", s.pinLockUntil)
        put("scores", JSONObject(s.scores))
        put("settings", JSONObject().apply {
            put("narration", s.settings.narration); put("volume", s.settings.volume.toDouble()); put("sounds", s.settings.sounds)
            put("motion", s.settings.reducedMotion); put("voice", s.settings.voice); put("locale", s.settings.locale); put("reminder", s.settings.reminderMinutes)
        })
        s.saved?.let { save -> put("saved", JSONObject().apply {
            put("profile", save.profile); put("world", save.world); put("seed", save.patternSeed); put("level", save.patternLevel); put("round", save.patternRound); put("mistakes", save.patternMistakes)
            put("patternDifficulty",save.patternDifficulty.name)
            save.board?.let { b -> put("board", JSONObject().apply {
                put("seed", b.seed); put("mode", b.mode.name); put("difficulty", b.difficulty.name); put("level", b.level)
                put("matched", JSONArray(b.matched.toList())); put("revealed", JSONArray(b.revealed.toList())); put("moves", b.moves); put("hints", b.hints)
            }) }
        }) }
    }.toString()
    fun decode(raw: String): LocalState = runCatching {
        val j = JSONObject(raw); val p = j.getJSONArray("profiles")
        val profiles = (0 until p.length()).map { p.getJSONObject(it).let { x -> Profile(x.getInt("id"), x.getString("name"), x.getString("avatar"), Difficulty.valueOf(x.getString("difficulty"))) } }
        require(profiles.isNotEmpty() && profiles.size <= 4)
        val selected = j.getInt("selected").takeIf { id -> profiles.any { it.id == id } } ?: profiles.first().id
        val settings = j.getJSONObject("settings")
        val scores = j.getJSONObject("scores").let { x -> x.keys().asSequence().associateWith { x.getInt(it).coerceIn(1, 3) } }
        val save = j.optJSONObject("saved")?.let { x ->
            val board = x.optJSONObject("board")?.let { b ->
                val generated = Decks.create(Mode.valueOf(b.getString("mode")), Difficulty.valueOf(b.getString("difficulty")), b.getInt("level"), b.getInt("seed"))
                fun ids(key: String): Set<Int> = b.getJSONArray(key).let { a -> (0 until a.length()).map { a.getInt(it) }.toSet() }
                val matched = ids("matched"); val revealed = ids("revealed")
                require((matched+revealed).all { id -> generated.cards.any { it.id == id } } && revealed.size <= 2 && revealed.none { it in matched })
                require(generated.cards.filter {it.id in matched}.groupBy {it.pair}.values.all {it.size==2})
                generated.copy(matched = matched, revealed = revealed, moves = b.getInt("moves"), hints = b.optInt("hints"), paused = true)
            }
            require(x.getInt("world") in 0..2 && x.getInt("level") in 1..12 && x.getInt("round") in 0..5)
            require(profiles.any {it.id==x.getInt("profile")})
            SavedGame(x.getInt("profile"), x.getInt("world"), board, x.getInt("seed"), x.getInt("level"), x.getInt("round"), x.optInt("mistakes"),Difficulty.valueOf(x.optString("patternDifficulty","LITTLE")))
        }
        LocalState(profiles, selected, Settings(settings.getBoolean("narration"), settings.getDouble("volume").toFloat().coerceIn(0f, 1f),
            settings.getBoolean("sounds"), settings.getBoolean("motion"), settings.getBoolean("voice"), settings.getString("locale"), settings.optInt("reminder")),
            scores, save, j.optString("pinSalt"), j.optString("pinHash"), j.optBoolean("premium"), j.optString("storeUser"),j.optInt("pinFailures"),j.optLong("pinLockUntil"))
    }.getOrDefault(LocalState())
}
class LocalStore(context: Context) {
    private val data = context.applicationContext.store
    private val key = stringPreferencesKey("state_v1")
    val state = data.data.map { StateJson.decode(it[key] ?: "{}") }
    suspend fun update(transform: (LocalState) -> LocalState) { data.edit { prefs -> prefs[key] = StateJson.encode(transform(StateJson.decode(prefs[key] ?: "{}"))) } }
}
object ParentPin {
    fun salt(): String = Base64.getEncoder().encodeToString(ByteArray(24).also { SecureRandom().nextBytes(it) })
    fun hash(pin: String, salt: String): String = Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(
        PBEKeySpec(pin.toCharArray(), Base64.getDecoder().decode(salt), 120000, 256)).encoded)
    fun verify(pin: String, salt: String, hash: String): Boolean = java.security.MessageDigest.isEqual(hash(pin, salt).toByteArray(), hash.toByteArray())
}
