package com.matchminds.kids

import com.matchminds.kids.ui.Translations
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class TranslationsTest {
    @Before fun catalogs() {
        Translations.install(Translations.languages.keys.associateWith {locale ->
            val json=JSONObject(File("src/main/assets/locales/$locale.json").readText())
            json.keys().asSequence().associateWith {json.getString(it)}
        })
    }
    @Test fun allTargetsHaveWholeSentenceAndVocabularyTranslations() {
        for(locale in Translations.languages.keys.filterNot {it.startsWith("en")}) {
            assertNotEquals(locale,"Find two cards that belong together.",Translations.text("Find two cards that belong together.",locale))
            assertNotEquals(locale,"blue circle",Translations.text("blue circle",locale))
            assertFalse(Translations.text("Can you find the other blue circle?",locale).contains("blue circle"))
        }
    }
    @Test fun dynamicNamesAndStorePricesAreOpaque() {
        assertEquals("Bonjour, Cat. Choisis un lieu pour l'aventure du jour.",Translations.text("Hello, Cat. Pick a place for today’s adventure.","fr"))
        assertEquals("Débloquer Premium • 3,99 €",Translations.text("Unlock premium • 3,99 €","fr"))
        assertEquals("Niveau 12",Translations.text("Level 12","fr"))
    }
    @Test fun scriptAndLocaleFallbacksAreExplicit() {
        assertEquals("ar",Translations.deviceLocale("ar-SA"))
        assertEquals("hi",Translations.deviceLocale("hi-IN"))
        assertEquals("en-GB",Translations.deviceLocale("ja-JP"))
        assertTrue(Translations.text("Card 4, face down","ar").contains("\u20684\u2069"))
        assertEquals("Unknown literal",Translations.text("Unknown literal","fr"))
    }
    @Test fun everyTranslatedTemplatePreservesItsArguments() {
        val slots=Regex("\\{\\d+\\}")
        for(locale in Translations.languages.keys.filterNot {it.startsWith("en")}) {
            val json=JSONObject(File("src/main/assets/locales/$locale.json").readText())
            json.keys().forEach {key -> assertEquals("$locale: $key",slots.findAll(key).map {it.value}.sorted().toList(),slots.findAll(json.getString(key)).map {it.value}.sorted().toList()) }
        }
    }
}
