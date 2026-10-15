package com.matchminds.kids

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import com.amazon.device.iap.internal.model.*
import com.amazon.device.iap.model.*
import com.matchminds.kids.billing.AmazonBilling
import com.matchminds.kids.data.LocalState
import com.matchminds.kids.data.LocalStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Simulated SDK callbacks test cache behavior; no Amazon account or money is involved. */
@RunWith(AndroidJUnit4::class)
class BillingCallbacksTest {
    @get:Rule val compose=createAndroidComposeRule<MainActivity>()
    @Test fun accountSwitchRevokesCachedAccessAndFailedCallbacksNeverGrantIt() = runBlocking {
        val store=LocalStore(compose.activity)
        val scope=CoroutineScope(SupervisorJob()+Dispatchers.Default)
        val billing=AmazonBilling(compose.activity,scope)
        fun user(id:String)=UserDataResponseBuilder().setRequestId(RequestId())
            .setRequestStatus(UserDataResponse.RequestStatus.SUCCESSFUL)
            .setUserData(UserDataBuilder().setUserId(id).setMarketplace("US").build()).build()
        try {
            store.update {LocalState(premium=true,storeUser="fixture-owner-a")}
            billing.onUserDataResponse(user("fixture-owner-a"))
            withTimeout(5000) {store.state.first {it.storeUser=="fixture-owner-a"}}
            assertTrue(store.state.first().premium)

            billing.onUserDataResponse(user("fixture-owner-b"))
            val switched=withTimeout(5000) {store.state.first {it.storeUser=="fixture-owner-b"}}
            assertFalse("A different Amazon user must not inherit cached premium",switched.premium)

            billing.onPurchaseResponse(PurchaseResponseBuilder().setRequestId(RequestId())
                .setRequestStatus(PurchaseResponse.RequestStatus.FAILED).build())
            assertFalse(store.state.first().premium)
            assertFalse(billing.state.value.busy)

            billing.onPurchaseUpdatesResponse(PurchaseUpdatesResponseBuilder().setRequestId(RequestId())
                .setRequestStatus(PurchaseUpdatesResponse.RequestStatus.FAILED).build())
            assertFalse(store.state.first().premium)
            assertTrue(billing.state.value.message.contains("Could not restore"))

            store.update {it.copy(premium=true)}
            billing.onUserDataResponse(UserDataResponseBuilder().setRequestId(RequestId())
                .setRequestStatus(UserDataResponse.RequestStatus.FAILED).build())
            assertTrue("A failed account lookup must preserve prior verified offline access",store.state.first().premium)
        } finally {scope.cancel()}
    }
}
