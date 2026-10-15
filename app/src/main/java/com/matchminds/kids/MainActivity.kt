package com.matchminds.kids

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matchminds.core.*
import com.matchminds.kids.billing.*
import com.matchminds.kids.data.*
import com.matchminds.kids.ui.*
import com.matchminds.kids.voice.VoiceGuide
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()
    private lateinit var voice: VoiceGuide
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voice=VoiceGuide(this)
        val billing=AmazonBilling(this,lifecycleScope)
        if(BuildConfig.DEBUG) {
            vm.qaPremium=intent.getBooleanExtra("qa_premium",false)
            val demo=intent.getStringExtra("qa_screen")
            if(demo!=null) lifecycleScope.launch {
                delay(1300)
                when(demo) {
                    "rewards" -> {
                        vm.start(Mode.ANIMALS,1,42)
                        delay(200)
                        vm.play.value.board?.cards?.groupBy {it.pair}?.values?.forEach { pair ->
                            vm.flip(pair[0].id);vm.flip(pair[1].id);delay(1850)
                        }
                        vm.navigate(Screen.REWARDS)
                    }
                    "welcome" -> vm.navigate(Screen.WELCOME)
                    else -> runCatching {
                        vm.start(Mode.valueOf(demo),2,42);delay(200)
                        vm.play.value.board?.cards?.groupBy {it.pair}?.values?.firstOrNull()?.let { pair -> vm.flip(pair[0].id);vm.flip(pair[1].id) }
                    }
                }
            }
        }
        lifecycle.addObserver(object: DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) { billing.sync() }
            override fun onStop(owner: LifecycleOwner) { vm.background(); voice.stopListening(); voice.stopAudio() }
        })
        setContent {
            MaterialTheme(colorScheme=lightColorScheme(primary=Teal,secondary=Orange,background=Cream,surface=Color.White,onSurface=Ink,onBackground=Ink)) {
                MatchMinds(vm,voice,billing)
            }
        }
    }
    override fun onDestroy() { voice.close(); super.onDestroy() }
}

@Composable private fun MatchMinds(vm: GameViewModel, voice: VoiceGuide, billing: AmazonBilling) {
    val local by vm.local.collectAsStateWithLifecycle()
    val play by vm.play.collectAsStateWithLifecycle()
    val bill by billing.state.collectAsStateWithLifecycle()
    var mic by remember { mutableStateOf(false) }
    var voiceStatus by remember { mutableStateOf("") }
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if(!granted) { vm.settings { it.copy(voice=false) }; voiceStatus="Microphone permission declined. Touch controls are ready." }
    }
    fun say(text: String) = voice.speak(text,local.settings.locale,local.settings.volume,local.settings.narration)
    fun back() {
        when(play.screen) {
            Screen.GAME -> { vm.pause(); vm.navigate(Screen.MODES) }
            Screen.COMPLETE -> vm.navigate(Screen.MODES)
            Screen.MODES -> vm.worlds()
            Screen.WORLDS -> vm.navigate(Screen.PROFILES)
            Screen.PROFILES,Screen.GATE,Screen.REWARDS,Screen.PROGRESS -> vm.navigate(Screen.WELCOME)
            Screen.PARENTS -> vm.worlds()
            Screen.PREMIUM,Screen.LANGUAGE,Screen.ACCESSIBILITY -> vm.navigate(Screen.PARENTS)
            else -> vm.navigate(Screen.WELCOME)
        }
    }
    BackHandler(play.screen != Screen.WELCOME && play.screen != Screen.SPLASH) { voice.stopListening(); mic=false; back() }
    LaunchedEffect(play.message,play.screen) {
        if(play.screen==Screen.GAME || play.screen==Screen.COMPLETE) say(play.message)
        if(play.screen==Screen.WELCOME) say("Welcome to MatchMinds!")
        if(local.settings.sounds && play.message.startsWith("Wonderful")) {
            android.media.MediaPlayer.create(vm.getApplication(),R.raw.chime)?.apply {
                setVolume(local.settings.volume,local.settings.volume); setOnCompletionListener { it.release() }; start()
            }
        }
    }
    LaunchedEffect(mic) { if(mic) { delay(6000); voice.stopListening(); mic=false; voiceStatus="Microphone stopped. Tap to try again." } }
    CompositionLocalProvider(LocalEnglishLocale provides local.settings.locale) { Surface(color=Cream, modifier=Modifier.fillMaxSize()) {
        if(play.screen==Screen.SPLASH) {
                var appeared by remember {mutableStateOf(false)}
                LaunchedEffect(Unit) {appeared=true}
                val entrance by animateFloatAsState(if(appeared) 1f else .85f,animationSpec=androidx.compose.animation.core.tween(if(local.settings.reducedMotion) 0 else 650),label="Milo welcome")
                Column(Modifier.fillMaxSize().alpha(entrance).scale(entrance),Arrangement.Center,Alignment.CenterHorizontally) { Milo(Modifier.size(200.dp)); Heading("MatchMinds Kids"); UiText("Little matches. Big discoveries.",color=Teal) }
        } else Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal=24.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically) {
                if(play.screen!=Screen.WELCOME) TextButton(onClick={back()}) { UiText("‹  Back",fontSize=17.sp) }
                Art("fox",Modifier.size(42.dp))
                Column(Modifier.padding(start=8.dp).weight(1f)) { UiText("MatchMinds",fontWeight=FontWeight.ExtraBold,fontSize=22.sp,color=Ink); UiText("K I D S  •  PLAY & DISCOVER",fontSize=9.sp,color=Teal,fontWeight=FontWeight.Bold) }
                if(play.screen !in setOf(Screen.PARENTS,Screen.GATE,Screen.PREMIUM,Screen.LANGUAGE,Screen.ACCESSIBILITY)) {
                    TextButton(onClick={if(play.screen==Screen.GAME) vm.pause();vm.navigate(Screen.REWARDS)}) { UiText("★ ${local.stars}",fontSize=20.sp,color=Teal) }
                    TextButton(onClick={if(play.screen==Screen.GAME) vm.pause();vm.navigate(Screen.GATE)}) { UiText("Parents",fontSize=16.sp) }
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.TopCenter) {
                when(play.screen) {
                    Screen.WELCOME -> Welcome(vm,local)
                    Screen.PROFILES -> Profiles(vm,local)
                    Screen.WORLDS -> Worlds(vm,local)
                    Screen.MODES -> Modes(vm,local,play)
                    Screen.GAME -> Game(vm,local,play,onRepeat={say(play.message)})
                    Screen.COMPLETE -> Complete(vm,local,play)
                    Screen.REWARDS -> Rewards(local)
                    Screen.PROGRESS -> Progress(local)
                    Screen.GATE -> ParentGate(vm,local)
                    Screen.PARENTS -> Parents(vm,local,voice.commandsAvailable,onVoice={enabled ->
                        if(enabled && !vm.premium) return@Parents
                        if(enabled && checkPermission(vm.getApplication(),Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) permission.launch(Manifest.permission.RECORD_AUDIO)
                        vm.settings { it.copy(voice=enabled) }
                    })
                    Screen.PREMIUM -> Premium(vm,billing,bill)
                    Screen.LANGUAGE -> Language(vm,local)
                    Screen.ACCESSIBILITY -> Accessibility(vm,local)
                    else -> Unit
                }
            }
            if(local.settings.voice && vm.premium && voice.commandsAvailable && play.screen in setOf(Screen.WELCOME,Screen.MODES,Screen.GAME,Screen.COMPLETE)) {
                Row(Modifier.fillMaxWidth().background(Pale).padding(horizontal=24.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically) {
                    UiText(voiceStatus.ifBlank { "Voice is optional. Tap, then say a command." },Modifier.weight(1f),fontSize=13.sp)
                    Button(onClick={
                        if(mic) { voice.stopListening(); mic=false } else if(checkPermission(vm.getApplication(),Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED) {
                            voice.listen(local.settings.locale,{ active,msg -> mic=active; voiceStatus=msg }) { command ->
                                when(command) {
                                    Command.START,Command.ANIMALS -> vm.start(Mode.ANIMALS)
                                    Command.AGAIN -> if(play.screen==Screen.COMPLETE) vm.start(play.mode,play.board?.level ?: play.patternLevel)
                                    Command.NEXT -> if(play.screen==Screen.COMPLETE) vm.start(play.mode,((play.board?.level ?: play.patternLevel)+1).coerceAtMost(12))
                                    Command.REPEAT -> say(play.message)
                                    Command.HELP -> { if(play.screen==Screen.GAME) vm.hint(); say("Find two cards that belong together.") }
                                    Command.PAUSE -> vm.pause()
                                    Command.BACK -> back()
                                }
                            }
                        } else { vm.settings { it.copy(voice=false) }; voiceStatus="A parent can enable the microphone in settings." }
                    },colors=ButtonDefaults.buttonColors(containerColor=if(mic) Color(0xFFAF3C38) else Teal)) { UiText(if(mic) "■ Stop microphone" else "● Tap to speak") }
                }
            }
        }
    }
}
}
private fun checkPermission(context: android.content.Context,permission: String) = context.checkSelfPermission(permission)
@Composable private fun Heading(text: String, modifier: Modifier = Modifier) = UiText(text,modifier,fontSize=32.sp,fontWeight=FontWeight.ExtraBold,color=Ink,lineHeight=38.sp)
@Composable private fun Sub(text: String, modifier: Modifier = Modifier) = UiText(text,modifier,fontSize=17.sp,color=Ink.copy(alpha=.75f),lineHeight=25.sp)
@Composable private fun Action(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(onClick,modifier.heightIn(min=56.dp),shape=RoundedCornerShape(18.dp),contentPadding=PaddingValues(horizontal=28.dp,vertical=14.dp)) { UiText(text,fontSize=18.sp,fontWeight=FontWeight.Bold) }
}
@Composable private fun Page(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.widthIn(max=1100.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=24.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
        Heading(title); Sub(subtitle); content(); Spacer(Modifier.height(16.dp))
    }
}
@Composable private fun Panel(modifier: Modifier = Modifier, colour: Color = Color.White, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(RoundedCornerShape(26.dp)).background(colour).border(1.dp,Ink.copy(alpha=.07f),RoundedCornerShape(26.dp)).padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
}
@Composable private fun Welcome(vm: GameViewModel, local: LocalState) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide=maxWidth>720.dp
        Column(Modifier.widthIn(max=1140.dp).align(Alignment.Center).verticalScroll(rememberScrollState()).padding(28.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                    UiText("A LITTLE PLAY. A LOT TO DISCOVER.",color=Teal,fontWeight=FontWeight.Bold,fontSize=12.sp,letterSpacing=1.sp)
                    UiText("Little matches.\nBig discoveries.",fontSize=if(wide) 56.sp else 38.sp,lineHeight=if(wide) 62.sp else 44.sp,fontWeight=FontWeight.ExtraBold,color=Ink)
                    Sub("Hello, little explorer! I’m Milo.\nLet’s find something wonderful together.")
                    Action("Let’s play  →") { vm.navigate(Screen.PROFILES) }
                    if(local.saved?.profile==local.selected) TextButton(onClick={vm.resumeSaved()}) { UiText("Continue your adventure",fontSize=17.sp) }
                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) { UiText("OFFLINE PLAY",fontSize=10.sp,color=Teal,fontWeight=FontWeight.Bold); UiText("•",color=Teal); UiText("NO ADS",fontSize=10.sp,color=Teal,fontWeight=FontWeight.Bold) }
                }
                if(wide) Box(Modifier.weight(1f).height(380.dp),contentAlignment=Alignment.Center) {
                    Box(Modifier.size(330.dp).clip(CircleShape).background(Color(0xFFE4EFD9)))
                    Art("star",Modifier.size(55.dp).align(Alignment.TopEnd).padding(5.dp))
                    Milo(Modifier.size(330.dp),"welcome")
                    Box(Modifier.align(Alignment.BottomStart).rotate(-10f).size(94.dp).clip(RoundedCornerShape(22.dp)).background(Color.White).padding(12.dp)) { Art("rabbit",Modifier.fillMaxSize()) }
                    Box(Modifier.align(Alignment.BottomEnd).rotate(9f).size(94.dp).clip(RoundedCornerShape(22.dp)).background(Color.White).padding(12.dp)) { Art("fox",Modifier.fillMaxSize()) }
                }
            }
            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Pale).padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
                Art("star",Modifier.size(40.dp)); UiText("A small adventure, at your own pace.",Modifier.weight(1f).padding(start=12.dp),fontSize=15.sp,color=Ink)
                TextButton(onClick={vm.navigate(Screen.PROGRESS)}) { UiText("My discoveries  →") }
            }
        }
    }
}
@Composable private fun Profiles(vm: GameViewModel,local: LocalState) {
    Page("Who’s exploring today?","Choose your little explorer. Everyone has their own discoveries.") {
        FlowRow(horizontalArrangement=Arrangement.spacedBy(18.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
            local.profiles.forEach { profile ->
                Panel(Modifier.width(220.dp).clickable { vm.selectProfile(profile.id) },Pale) {
                    Art(profile.avatar,Modifier.size(110.dp).align(Alignment.CenterHorizontally)); Heading(profile.name)
                    UiText(profile.difficulty.title,color=Teal); Action("That’s me!") { vm.selectProfile(profile.id) }
                }
            }
        }
        Sub("A grown-up can add up to four explorers in Parents.")
    }
}
private val worldNames=listOf("Woodland wonder","Ocean discoveries","Starlight garden")
private val worldDescriptions=listOf("A cosy place to begin","Make a splash with every match","A whole sky of little discoveries")
@Composable private fun Worlds(vm: GameViewModel,local: LocalState) {
    Page("Where shall we explore?","Hello, ${local.profile.name}. Pick a place for today’s adventure.") {
        FlowRow(horizontalArrangement=Arrangement.spacedBy(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            worldNames.forEachIndexed { i,name ->
                val available=i==0 || vm.premium
                Panel(Modifier.widthIn(max=320.dp).fillMaxWidth().then(if(available) Modifier.clickable { vm.world(i) } else Modifier),listOf(Pale,Color(0xFFE6F2F7),Color(0xFFF0EBF8))[i]) {
                    Art(listOf("fox","dolphin","star")[i],Modifier.size(115.dp).align(Alignment.CenterHorizontally))
                    UiText("WORLD 0${i+1}",color=Teal,fontWeight=FontWeight.Bold,fontSize=11.sp)
                    UiText(name,fontSize=24.sp,fontWeight=FontWeight.Bold,color=Ink)
                    Sub(worldDescriptions[i])
                    if(available) Action("Explore  →") { vm.world(i) } else UiText("More adventures",color=Ink)
                }
            }
        }
    }
}
@Composable private fun Modes(vm: GameViewModel,local: LocalState,play: PlayState) {
    Page(worldNames[play.world],"Choose a discovery • ${local.profile.difficulty.title}") {
        FlowRow(horizontalArrangement=Arrangement.spacedBy(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Mode.entries.forEach { mode ->
                val next=local.nextLevel(mode,play.world)
                val available=Access.allowed(vm.premium,mode,local.profile.difficulty,next)
                Panel(Modifier.widthIn(max=330.dp).fillMaxWidth().then(if(available) Modifier.clickable { vm.start(mode) } else Modifier),if(available) Color.White else Color(0xFFF1EEE7)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Art(mode.icon,Modifier.size(64.dp)); Column(Modifier.weight(1f).padding(start=12.dp)) { UiText(mode.title,fontSize=21.sp,fontWeight=FontWeight.Bold,color=Ink); UiText(mode.subtitle,fontSize=13.sp,color=Ink) }
                    }
                    if(available) Action("Play level $next  →") { vm.start(mode) } else {
                        UiText("More adventures",color=Ink.copy(alpha=.65f))
                        if(mode in listOf(Mode.ANIMALS,Mode.SHAPES) && local.profile.difficulty==Difficulty.LITTLE) TextButton(onClick={vm.start(mode,1)}) { UiText("Play starter level again") }
                    }
                }
            }
        }
        if(local.saved?.profile==local.selected) TextButton(onClick={vm.resumeSaved()}) { UiText("Continue saved adventure") }
    }
}
@Composable private fun Game(vm: GameViewModel,local: LocalState,p: PlayState,onRepeat: () -> Unit) {
    val paused=p.board?.paused==true || (p.mode==Mode.PATTERNS && p.patternPaused)
    BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal=20.dp)) {
        val wide=maxWidth>700.dp
        val cols=if(wide) if((p.board?.cards?.size ?: 0)>12) 5 else 4 else if((p.board?.cards?.size ?: 0)>12) 4 else 3
        Column(Modifier.widthIn(max=1100.dp).fillMaxSize().align(Alignment.TopCenter),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { UiText(p.mode.title,fontSize=26.sp,fontWeight=FontWeight.ExtraBold,color=Ink); UiText("${worldNames[p.world]}  •  Level ${p.board?.level ?: p.patternLevel}",fontSize=13.sp,color=Teal) }
                UiText(if(p.mode==Mode.PATTERNS) "${p.round} / 5" else "${(p.board?.matched?.size ?: 0)/2} / ${(p.board?.cards?.size ?: 0)/2} pairs",fontSize=18.sp,fontWeight=FontWeight.Bold,color=Teal)
                TextButton(onClick={vm.pause()}) { UiText("Ⅱ  Pause",fontSize=16.sp) }
            }
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Pale).padding(10.dp),verticalAlignment=Alignment.CenterVertically) {
                Milo(Modifier.size(if(wide) 72.dp else 48.dp),if(p.message.startsWith("Wonderful")) "celebrating" else "explaining")
                UiText(p.message,Modifier.weight(1f).padding(horizontal=10.dp).semantics { liveRegion=LiveRegionMode.Polite },fontSize=if(wide) 19.sp else 15.sp,color=Ink)
                TextButton(onClick=onRepeat) { UiText("Listen") }
            }
            if(paused) {
                Column(Modifier.weight(1f).fillMaxWidth(),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) {
                    Milo(Modifier.size(160.dp),"encouraging"); Heading(if(p.reminder) "Time for a little break?" else "Adventure paused")
                    Sub("Your discoveries are safe. Take your time."); Spacer(Modifier.height(18.dp)); Action("Keep playing") {vm.resume()}
                    TextButton(onClick={vm.worlds()}) { UiText("Finish for now") }
                }
            } else if(p.mode==Mode.PATTERNS) {
                val pattern=p.pattern
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                    Sub("Follow the repeating rule")
                    FlowRow(Modifier.padding(vertical=18.dp),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        pattern?.sequence?.forEach { face -> FaceArt(face,Modifier.size(if(wide) 80.dp else 52.dp).clip(RoundedCornerShape(15.dp)).background(Color.White).padding(7.dp)) }
                        Box(Modifier.size(if(wide) 80.dp else 52.dp).border(2.dp,Teal,RoundedCornerShape(15.dp)),contentAlignment=Alignment.Center) { UiText("?",fontSize=32.sp,color=Teal) }
                    }
                    UiText("What comes next?",fontSize=21.sp,fontWeight=FontWeight.Bold,color=Ink)
                    Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                        pattern?.options?.forEach { face -> Column(Modifier.width(if(wide) 120.dp else 82.dp).clip(RoundedCornerShape(20.dp)).background(Color.White).clickable(enabled=!p.patternBusy) { vm.answer(face) }.padding(10.dp).semantics { contentDescription="Choose ${face.label}" },horizontalAlignment=Alignment.CenterHorizontally) {
                            FaceArt(face,Modifier.size(if(wide) 90.dp else 60.dp)); UiText(face.label,fontSize=13.sp,color=Ink)
                        } }
                    }
                }
            } else {
                val cards=p.board?.cards ?: emptyList()
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                    val rows=(cards.size+cols-1)/cols
                    val cardHeight=((maxHeight-((rows-1)*10).dp)/rows).coerceIn(72.dp,165.dp)
                    LazyVerticalGrid(columns=GridCells.Fixed(cols),modifier=Modifier.widthIn(max=900.dp).fillMaxHeight().align(Alignment.Center),verticalArrangement=Arrangement.spacedBy(10.dp,Alignment.CenterVertically),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        items(cards,key={it.id}) { card ->
                            val matched=card.id in (p.board?.matched ?: emptySet())
                            val faceUp=matched || card.id in (p.board?.revealed ?: emptySet()) || card.id in p.hint
                            val scale by animateFloatAsState(if(faceUp) 1f else .97f,animationSpec=androidx.compose.animation.core.tween(if(local.settings.reducedMotion) 0 else 180),label="flip")
                            Column(Modifier.height(cardHeight).scale(scale).clip(RoundedCornerShape(20.dp)).background(if(faceUp) if(matched) Pale else Color.White else Teal)
                                .border(if(matched) 2.dp else 1.dp,if(matched) Teal.copy(alpha=.45f) else Ink.copy(alpha=.06f),RoundedCornerShape(20.dp))
                                .clickable(enabled=!matched && p.board?.resolving==false) { vm.flip(card.id) }
                                .semantics(mergeDescendants=true) {
                                    contentDescription=if(faceUp) "${card.face.label}${if(matched) ", matched" else ""}" else "Card ${cards.indexOf(card)+1}, face down"
                                    role=Role.Button
                                }.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                                if(faceUp) {
                                    FaceArt(card.face,Modifier.weight(1f).fillMaxWidth())
                                    if(card.face.art !in listOf("digit","letter")) UiText(card.face.label, fontSize=if(cardHeight<100.dp) 10.sp else 14.sp,color=Ink,textAlign=TextAlign.Center,maxLines=2)
                                    if(matched && cardHeight>100.dp) UiText("✓",fontSize=13.sp,color=Teal)
                                } else {
                                    Art("star",Modifier.size(if(cardHeight<100.dp) 30.dp else 48.dp))
                                    if(cardHeight>100.dp) UiText("discover",fontSize=11.sp,color=Color.White.copy(alpha=.85f),letterSpacing=1.sp)
                                }
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                    UiText("Take your time. Every try is a discovery.",Modifier.weight(1f),fontSize=12.sp,color=Ink)
                    TextButton(onClick={vm.hint()}) { UiText("A little hint  ✦",fontSize=16.sp) }
                }
            }
        }
    }
}
@Composable private fun Complete(vm: GameViewModel,local: LocalState,p: PlayState) {
    val level=p.board?.level ?: p.patternLevel
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        Milo(Modifier.size(190.dp),"completion")
        UiText("★ ".repeat(p.lastStars).trim(),fontSize=44.sp,color=Color(0xFF997000),letterSpacing=10.sp)
        Heading(if(level==12) "A world of discoveries!" else "You did it, ${local.profile.name}!")
        Sub("Level $level complete. Every little match is worth celebrating.")
        Spacer(Modifier.height(20.dp))
        if(level<12 && Access.allowed(vm.premium,p.mode,local.profile.difficulty,level+1)) Action("Next adventure  →") { vm.start(p.mode,level+1) }
        TextButton(onClick={vm.start(p.mode,level)}) { UiText("Play again",fontSize=18.sp) }
        TextButton(onClick={vm.worlds()}) { UiText("A lovely place to stop",fontSize=17.sp) }
        TextButton(onClick={vm.navigate(Screen.REWARDS)}) { UiText("My stickers") }
    }
}
@Composable private fun Rewards(local: LocalState) {
    Page("Your little collection","${local.stars} stars • Every discovery has a place here.") {
        FlowRow(horizontalArrangement=Arrangement.spacedBy(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            val badges=listOf(Triple("First discovery","fox",1),Triple("Curious cub","rabbit",3),Triple("Pattern pal","pattern",8),Triple("Star explorer","star",15),Triple("Ocean friend","dolphin",30),Triple("Milo’s memory buddy","lion",60))
            badges.forEach { (name,art,threshold) -> Panel(Modifier.width(230.dp),if(local.completed>=threshold) Pale else Color(0xFFF1EEE7)) {
                Art(art,Modifier.size(90.dp).align(Alignment.CenterHorizontally).alpha(if(local.completed>=threshold) 1f else .35f))
                UiText(name,fontSize=18.sp,fontWeight=FontWeight.Bold,color=Ink)
                UiText(if(local.completed>=threshold) "Discovered!" else "Discover $threshold levels",color=Teal,fontSize=14.sp)
            } }
        }
    }
}
@Composable private fun Progress(local: LocalState) {
    Page("${local.profile.name}’s discoveries","A gentle record of play, saved on this device.") {
        Row(horizontalArrangement=Arrangement.spacedBy(18.dp)) { Panel(Modifier.weight(1f),Pale) { Heading("${local.completed}"); UiText("levels discovered",color=Ink) }; Panel(Modifier.weight(1f),Color(0xFFFFEDD3)) { Heading("${local.stars}"); UiText("stars collected",color=Ink) } }
        Mode.entries.forEach { mode ->
            val entries=local.scores.filterKeys { it.startsWith("${local.selected}/") && it.contains("/${mode.name}/") }
            Panel(Modifier.fillMaxWidth()) { Row(verticalAlignment=Alignment.CenterVertically) { Art(mode.icon,Modifier.size(48.dp)); UiText(mode.title,Modifier.weight(1f).padding(start=16.dp),fontWeight=FontWeight.Bold,color=Ink); UiText("${entries.size} levels  •  ${entries.values.sum()} ★",color=Teal) } }
        }
        Sub("This record describes play. It is not a developmental assessment.")
    }
}
@Composable private fun ParentGate(vm: GameViewModel,local: LocalState) {
    val scope=rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }; var confirm by remember { mutableStateOf("") }
    var phrase by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }
    var failures by rememberSaveable { mutableIntStateOf(0) }; var cooldown by rememberSaveable { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    LaunchedEffect(cooldown) { if(cooldown>0) { delay(1000); cooldown-- } }
    LaunchedEffect(local.pinLockUntil) { cooldown=maxOf(cooldown,((local.pinLockUntil-System.currentTimeMillis())/1000).toInt().coerceAtLeast(0)) }
    val setup=local.pinHash.isBlank()
    Page(if(setup) "Grown-ups, let’s get set up" else "Grown-ups only","Please hand the device to a parent or carer.") {
        Panel(Modifier.widthIn(max=560.dp).fillMaxWidth()) {
            Sub(if(setup) "Choose a private six-digit PIN with at least three different digits. Keep it away from your child. It protects settings and purchases." else "Enter your private PIN to open the parent area.")
            OutlinedTextField(pin,{pin=it.filter(Char::isDigit).take(6)},label={UiText(if(setup) "Create six-digit PIN" else "Parent PIN")},visualTransformation=PasswordVisualTransformation(),keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.NumberPassword),singleLine=true)
            if(setup) {
                OutlinedTextField(confirm,{confirm=it.filter(Char::isDigit).take(6)},label={UiText("Repeat PIN")},visualTransformation=PasswordVisualTransformation(),keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=KeyboardType.NumberPassword),singleLine=true)
                Sub("To confirm you are setting this up for your family, type: I am the grown-up")
                OutlinedTextField(phrase,{phrase=it},label={UiText("Grown-up confirmation")},singleLine=true)
            }
            if(error.isNotBlank()) UiText(error,color=Color(0xFF9C3430))
            Button(enabled=!busy && cooldown==0 && pin.length==6,onClick={
                scope.launch {
                    busy=true
                    val ok=(!setup || phrase.trim()=="I am the grown-up") && vm.authenticate(pin,confirm)
                    if(!ok) { failures++; error=if(setup) "Check both PINs and the confirmation phrase." else "That PIN didn’t match. Please try again."; pin=""; if(failures>=3) cooldown=30 }
                    busy=false
                }
            },modifier=Modifier.heightIn(min=56.dp)) { UiText(if(cooldown>0) "Try again in ${cooldown}s" else if(busy) "Checking…" else "Open parent area",fontSize=17.sp) }
            UiText("No PIN recovery by email: no account is collected. Clearing app data resets all profiles and settings. Restore purchases afterwards.",fontSize=12.sp,color=Ink)
        }
    }
}
@Composable private fun Toggle(title: String,description: String,value: Boolean,enabled: Boolean = true,change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min=64.dp),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end=16.dp)) { UiText(title,fontSize=17.sp,fontWeight=FontWeight.Bold,color=Ink); UiText(description,fontSize=13.sp,color=Ink.copy(alpha=.75f)) }
        Switch(value,change,enabled=enabled,modifier=Modifier.semantics { contentDescription=title })
    }
}
@Composable private fun Parents(vm: GameViewModel,local: LocalState,voiceAvailable: Boolean,onVoice: (Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }; var avatar by remember { mutableStateOf("fox") }; var confirm by remember { mutableStateOf("") }
    Page("A little space for grown-ups","Profiles, play preferences and purchases. Everything is saved locally.") {
        Panel(Modifier.fillMaxWidth()) {
            UiText("Your explorers",fontSize=23.sp,fontWeight=FontWeight.Bold,color=Ink)
            local.profiles.forEach { profile -> Row(verticalAlignment=Alignment.CenterVertically) {
                Art(profile.avatar,Modifier.size(42.dp)); TextButton(onClick={vm.update {it.copy(selected=profile.id)}}) { UiText("${profile.name}${if(local.selected==profile.id) " • selected" else ""}") }
                if(local.profiles.size>1) TextButton(onClick={confirm="profile:${profile.id}"}) { UiText("Remove") }
            } }
            if(local.profiles.size<4) {
                OutlinedTextField(name,{name=it.take(16)},label={UiText("Nickname (optional)")},singleLine=true)
                Row { listOf("fox","rabbit","panda","penguin").forEach { a -> Box(Modifier.size(58.dp).clip(CircleShape).background(if(a==avatar) Pale else Color.Transparent).clickable {avatar=a}.padding(7.dp).semantics {contentDescription="Choose $a avatar"}) {Art(a,Modifier.fillMaxSize())} } }
                TextButton(onClick={vm.addProfile(name,avatar);name=""}) { UiText("Add explorer") }
            }
            UiText("Difficulty for ${local.profile.name}",fontWeight=FontWeight.Bold,color=Ink)
            Difficulty.entries.forEach { d -> Row(Modifier.fillMaxWidth().clickable {vm.profileDifficulty(d)},verticalAlignment=Alignment.CenterVertically) {
                RadioButton(local.profile.difficulty==d,{vm.profileDifficulty(d)}); Column {UiText(d.title,color=Ink);UiText("${d.guidance} • ${if(d==Difficulty.LITTLE) "4–6" else if(d==Difficulty.GROWING) "12" else "20"} cards",fontSize=12.sp,color=Ink)}
            } }
            UiText("Age bands are guidance. You can choose any difficulty. Advanced games require premium.",fontSize=12.sp,color=Ink)
        }
        Panel(Modifier.fillMaxWidth()) {
            UiText("Sound & voice",fontSize=23.sp,fontWeight=FontWeight.Bold,color=Ink)
            Toggle("Milo’s spoken guidance","Uses an installed offline voice or packaged English instructions.",local.settings.narration) {vm.settings {s -> s.copy(narration=it)}}
            UiText("Narration volume",color=Ink); Slider(local.settings.volume,{v -> vm.settings {it.copy(volume=v)}},modifier=Modifier.semantics {contentDescription="Narration volume"})
            Toggle("Game sounds","Gentle feedback tones.",local.settings.sounds) {vm.settings {s -> s.copy(sounds=it)}}
            Toggle("Optional voice commands",if(!vm.premium) "Available with premium on supported devices." else if(voiceAvailable) "Tap to speak. Uses the device’s on-device recogniser; no cloud recogniser is used." else "No on-device recogniser is installed. Touch and narration remain available.",local.settings.voice,vm.premium && voiceAvailable,onVoice)
            UiText("Commands: start game, animals, play again, next level, repeat, help, pause, go back. Recognition quality depends on the device. The app does not save recordings or transcripts.",fontSize=12.sp,color=Ink)
        }
        Panel(Modifier.fillMaxWidth()) {
            UiText("Play preferences",fontSize=23.sp,fontWeight=FontWeight.Bold,color=Ink)
            Action("Language") {vm.navigate(Screen.LANGUAGE)}
            Action("Accessibility") {vm.navigate(Screen.ACCESSIBILITY)}
            Toggle("Gentle session reminder","Suggest a break after 15 minutes. No daily streaks.",local.settings.reminderMinutes>0) {vm.settings {s -> s.copy(reminderMinutes=if(it) 15 else 0)}}
            Action("View progress") {vm.navigate(Screen.PROGRESS)}
            Action("Premium & restore purchases") {vm.navigate(Screen.PREMIUM)}
        }
        Panel(Modifier.fillMaxWidth()) {
            UiText("Privacy & local data",fontSize=23.sp,fontWeight=FontWeight.Bold,color=Ink)
            Sub("No accounts, ads, tracking, location or public sharing. Nicknames, avatars, settings and progress stay on this device; Android backup is disabled. Amazon handles payments. Purchase verification sends only the Amazon user and receipt IDs to the publisher’s verification server when configured.")
            UiText("Microphone input is off by default. On-device recognition is required; no network recognition fallback is used. Device speech components remain subject to the device provider’s terms. Touch controls work throughout.",color=Ink,fontSize=14.sp)
            TextButton(onClick={confirm="progress"}) {UiText("Delete all play progress")}
            TextButton(onClick={confirm="all"}) {UiText("Delete profiles, settings and progress")}
        }
        if(confirm.isNotBlank()) AlertDialog(onDismissRequest={confirm=""},title={UiText("Delete local data?")},text={UiText("This cannot be undone. Purchases stay with Amazon and can be restored.")},confirmButton={TextButton(onClick={when {confirm=="all" -> vm.eraseAll();confirm=="progress" -> vm.eraseProgress();confirm.startsWith("profile:") -> vm.removeProfile(confirm.substringAfter(':').toInt())};confirm=""}) {UiText("Delete")}},dismissButton={TextButton(onClick={confirm=""}) {UiText("Keep data")}})
    }
}
@Composable private fun Premium(vm: GameViewModel,billing: AmazonBilling,bill: BillingState) {
    Page("More little discoveries","One-time premium unlock • For parents and carers") {
        Panel(Modifier.fillMaxWidth(),Pale) {
            Milo(Modifier.size(130.dp),"celebrating")
            Heading(if(vm.premium) "All adventures are ready" else "A bigger world of play")
            Sub("Six game modes, all twelve levels per mode, three themed worlds, advanced difficulty, and additional stickers. Optional voice commands are available only with a compatible on-device recogniser.")
            UiText("No subscriptions. No advertising. Included games remain playable offline.",color=Teal,fontWeight=FontWeight.Bold)
            UiText(bill.message,color=Ink,modifier=Modifier.semantics {liveRegion=LiveRegionMode.Polite})
            if(!vm.premium) Button(enabled=bill.configured && !bill.busy && bill.price!=null,onClick={billing.purchase()},modifier=Modifier.heightIn(min=56.dp)) {UiText(bill.price?.let {"Unlock premium • $it"} ?: "Price unavailable")}
            Button(enabled=bill.configured && !bill.busy,onClick={billing.refresh()}) {UiText("Check Amazon price")}
            OutlinedButton(enabled=bill.configured && !bill.busy,onClick={billing.restore()}) {UiText("Restore purchases")}
            UiText("Purchases and restoration need a connection and the Amazon Appstore. Amazon will confirm payment. Keep your parent PIN private.",fontSize=13.sp,color=Ink)
        }
    }
}
@Composable private fun Language(vm: GameViewModel,local: LocalState) {
    Page("Language","Choose a reviewed interface and vocabulary set.") {
        listOf("en-GB" to "English (United Kingdom)","en-US" to "English (United States)").forEach { (tag,label) ->
            Row(Modifier.fillMaxWidth().clickable {vm.settings {it.copy(locale=tag)}},verticalAlignment=Alignment.CenterVertically) {RadioButton(local.settings.locale==tag,{vm.settings {it.copy(locale=tag)}});UiText(label,color=Ink)}
        }
        Sub("Other languages are in the translation review pipeline and are not offered as supported languages yet. Letter games use English letter names, not phonics lessons. Voice support is checked separately.")
    }
}
@Composable private fun Accessibility(vm: GameViewModel,local: LocalState) {
    Page("Comfortable play","Choose what feels right for your family.") {
        Panel(Modifier.fillMaxWidth()) {
            Toggle("Reduced motion","Remove card scaling and shorten reveal transitions.",local.settings.reducedMotion) {vm.settings {s -> s.copy(reducedMotion=it)}}
            Toggle("Spoken guidance","Every spoken instruction also appears as text.",local.settings.narration) {vm.settings {s -> s.copy(narration=it)}}
            Sub("Cards have screen reader labels. Matching uses shapes, illustrations, quantities and text, alongside colour. There are no forced timers. Use your device settings for larger text and screen reader controls.")
        }
    }
}
