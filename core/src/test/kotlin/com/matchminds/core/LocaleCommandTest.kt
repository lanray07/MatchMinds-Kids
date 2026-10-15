package com.matchminds.core

import org.junit.Assert.*
import org.junit.Test

class LocaleCommandTest {
    @Test fun constrainedCommandsRemainLanguageSpecific() {
        for((locale,spoken) in mapOf("es" to "ayuda","fr" to "aide","de" to "hilfe","it" to "aiuto","pt-BR" to "ajuda","pt-PT" to "ajuda","nl" to "help","pl" to "pomoc","ar" to "مساعدة","hi" to "मदद")) {
            assertEquals(locale,Command.HELP,Commands.parse(spoken,locale))
            assertNull(Commands.parse("buy premium",locale))
        }
        assertNull(Commands.parse("start game","fr"))
        assertNull(Commands.parse("help","ja"))
    }
}
