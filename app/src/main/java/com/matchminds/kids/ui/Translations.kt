package com.matchminds.kids.ui

import android.content.Context
import org.json.JSONObject
import java.util.Locale

/** Human-readable, offline translation previews. User nicknames and store prices stay opaque. */
object Translations {
    val languages = linkedMapOf("en-GB" to "English (United Kingdom)", "en-US" to "English (United States)",
        "es" to "Español", "fr" to "Français", "de" to "Deutsch", "it" to "Italiano",
        "pt-BR" to "Português (Brasil)", "pt-PT" to "Português (Portugal)", "nl" to "Nederlands",
        "pl" to "Polski", "ar" to "العربية", "hi" to "हिन्दी")
    private var catalogs: Map<String,Map<String,String>> = emptyMap()
    private val slotPattern=Regex("\\{\\d+\\}")
    private var templates: Map<String,List<Template>> = emptyMap()
    fun deviceLocale(tag:String=Locale.getDefault().toLanguageTag()):String = when {
        languages.containsKey(tag) -> tag
        tag.startsWith("pt") -> "pt-PT"
        tag.startsWith("en-US") -> "en-US"
        languages.containsKey(tag.substringBefore('-')) -> tag.substringBefore('-')
        else -> "en-GB"
    }
    private data class Template(val source:String,val pattern:Regex,val target:String,val slots:List<String>)
    @Synchronized fun load(context: Context) {
        if(catalogs.isNotEmpty()) return
        install(languages.keys.associateWith { locale ->
            val json=JSONObject(context.assets.open("locales/$locale.json").bufferedReader().use {it.readText()})
            json.keys().asSequence().associateWith {json.getString(it)}
        })
    }
    internal fun install(values:Map<String,Map<String,String>>) {
        catalogs=values
        templates=values.mapValues { (_, entries) -> entries.filterKeys {it.contains(slotPattern)}.map { (source,target) ->
            val slotMatches=slotPattern.findAll(source).toList()
            var cursor=0
            val pattern=buildString {
                append("^")
                for (slot in slotMatches) {append(Regex.escape(source.substring(cursor,slot.range.first)));append("(.+?)");cursor=slot.range.last+1}
                append(Regex.escape(source.substring(cursor)));append("$")
            }
            Template(source,Regex(pattern),target,slotMatches.map {it.value})
        }.sortedByDescending {it.source.length} }
    }
    fun text(value:String,locale:String):String = resolve(value,locale,0)
    private fun resolve(value:String,locale:String,depth:Int):String {
        if(locale.startsWith("en")) return if(locale=="en-US") value.replace("colours","colors").replace("Colours","Colors").replace("colour","color").replace("cosy","cozy").replace("recogniser","recognizer") else value
        val entries=catalogs[locale] ?: return value
        entries[value]?.let {return it}
        if(depth>3) return value
        // Match whole templates, never replace arbitrary words inside a child's nickname.
        for(template in templates[locale].orEmpty()) {
            val match=template.pattern.matchEntire(value) ?: continue
            var result=template.target
            template.slots.forEachIndexed { index,slot ->
                val raw=match.groupValues[index+1]
                val label=template.source.startsWith("Can you find") || template.source.startsWith("Choose ") || template.source=="{0}, matched"
                val translated=if(label) resolve(raw,locale,depth+1) else raw
                result=result.replace(slot,if(locale=="ar") "\u2068$translated\u2069" else translated)
            }
            return result
        }
        if(value=="Ⅱ  Pause") return "Ⅱ  "+resolve("Pause",locale,depth+1)
        val colours=setOf("blue","coral","green","purple")
        val words=value.split(' ')
        if(words.size==2 && words[0] in colours) return resolve(words[1],locale,depth+1)+" · "+resolve(words[0],locale,depth+1)
        if(value.contains(" • ")) return value.split(" • ").joinToString(" • ") {resolve(it.trim(),locale,depth+1)}
        if(value.contains("  •  ")) return value.split("  •  ").joinToString(" • ") {resolve(it.trim(),locale,depth+1)}
        return value
    }
    fun format(source:String,locale:String,vararg args:Any):String {
        var result=catalogs[locale]?.get(source) ?: source
        args.forEachIndexed {index,arg -> result=result.replace("{$index}",arg.toString())}
        return result
    }
}
