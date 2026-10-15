package com.matchminds.kids

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.matchminds.kids.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileInputStream

/** Never runs against a production endpoint or a device outside Amazon sandbox mode. */
@RunWith(AndroidJUnit4::class)
class SandboxPurchaseTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun appTesterPurchaseAndRestoreRequireCloudVerification() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("runSandboxIap")=="true")
        check(BuildConfig.DEBUG && BuildConfig.VERIFICATION_URL=="https://matchminds-kids-verifier.vercel.app/api/sandbox")
        val automation=InstrumentationRegistry.getInstrumentation().uiAutomation
        val mode=automation.executeShellCommand("getprop debug.amazon.sandboxmode").use {fd ->FileInputStream(fd.fileDescriptor).bufferedReader().readText().trim()}
        check(mode=="debug") {"Amazon sandbox mode is required; no purchase was started"}
        val vm=ViewModelProvider(compose.activity)[GameViewModel::class.java]
        compose.waitUntil(8000) {vm.play.value.screen!=Screen.SPLASH}
        runBlocking {LocalStore(compose.activity).update {LocalState(settings=Settings(locale="en-GB",narration=false,sounds=false))}}
        assertTrue(runBlocking {vm.authenticate("184729","184729")})
        compose.runOnIdle {vm.navigate(Screen.PREMIUM)}
        compose.waitForIdle()
        compose.onNodeWithText("Check Amazon price").performScrollTo().performClick()
        compose.waitUntil(30000) {compose.activity.billing.state.value.price!=null}
        val price=compose.activity.billing.state.value.price!!
        compose.onNodeWithText("Unlock premium • $price").performScrollTo().performClick()
        try {
            compose.waitUntil(180000) {vm.local.value.premium}
        } catch (failure: Throwable) {
            throw AssertionError("Sandbox purchase did not unlock premium: ${compose.activity.billing.state.value.message}", failure)
        }
        assertTrue(vm.local.value.premium)
        // Simulate loss of the local entitlement cache. Restore must verify the actual App Tester receipt again.
        runBlocking {LocalStore(compose.activity).update {it.copy(premium=false,storeUser="")}}
        compose.waitUntil(5000) {!vm.local.value.premium}
        compose.onNodeWithText("Restore purchases").performScrollTo().performClick()
        compose.waitUntil(60000) {vm.local.value.premium && !compose.activity.billing.state.value.busy}
        assertTrue(vm.local.value.premium)
        File(compose.activity.cacheDir,"sandbox-verification.json").writeText("""{"sandbox":true,"purchase_verified":true,"restore_verified":true,"endpoint":"https://matchminds-kids-verifier.vercel.app/api/sandbox","real_money_charged":false}""")
        android.util.Log.i("MatchMindsBillingReport","Sandbox purchase and restoration both verified by RVS; no real payment")
    }
}
