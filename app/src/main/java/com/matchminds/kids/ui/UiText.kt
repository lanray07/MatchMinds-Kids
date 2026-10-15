package com.matchminds.kids.ui

import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.res.stringResource
import com.matchminds.kids.R

val LocalEnglishLocale = staticCompositionLocalOf { "en-GB" }
/** Only reviewed English variants are enabled. Machine translation candidates are never loaded. */
@Composable fun UiText(text: String,modifier: Modifier=Modifier,fontSize: TextUnit=TextUnit.Unspecified,
    fontWeight: FontWeight?=null,color: Color=Color.Unspecified,lineHeight: TextUnit=TextUnit.Unspecified,
    letterSpacing: TextUnit=TextUnit.Unspecified,maxLines: Int=Int.MAX_VALUE,textAlign: TextAlign?=null) {
    val res=when(text) {
        "Little matches. Big discoveries." -> R.string.tagline
        "Welcome to MatchMinds!" -> R.string.welcome
        "Find two cards that belong together." -> R.string.match_instruction
        "Wonderful! You found a match." -> R.string.match_success
        "Great effort! Try another card." -> R.string.try_again
        "You finished the level!" -> R.string.complete
        "Ready for another adventure?" -> R.string.ready
        else -> null
    }
    val resolved=if(res==null) text else stringResource(res)
    val display=if(LocalEnglishLocale.current=="en-US") resolved.replace("colours","colors").replace("Colours","Colors").replace("colour","color").replace("recogniser","recognizer").replace("recognisers","recognizers").replace("cosy","cozy") else resolved
    Text(display,modifier,color=color,fontSize=fontSize,fontWeight=fontWeight,lineHeight=lineHeight,letterSpacing=letterSpacing,maxLines=maxLines,textAlign=textAlign)
}
