package com.matchminds.kids.billing

import android.app.Activity
import com.amazon.device.iap.*
import com.amazon.device.iap.model.*
import com.matchminds.core.*
import com.matchminds.kids.BuildConfig
import com.matchminds.kids.data.LocalStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class BillingState(val price: String? = null, val busy: Boolean = false,
    val message: String = "Connect to Amazon to check availability.", val configured: Boolean = false)

/** No client-side secrets. Entitlements are granted only by the configured RVS proxy. */
class AmazonBilling(private val activity: Activity, private val scope: CoroutineScope) : PurchasingListener {
    private val store = LocalStore(activity)
    private val mutable = MutableStateFlow(BillingState())
    val state = mutable.asStateFlow()
    private var activeUser = ""
    private var restoredCount = 0
    private var restoreHadFailure = false
    private var timeout: Job? = null
    private val configured = BuildConfig.VERIFICATION_URL.startsWith("https://") && runCatching {
        activity.assets.open("AppstoreAuthenticationKey.pem").use { it.read() >= 0 }
    }.getOrDefault(false)
    init {
        mutable.update { it.copy(configured=configured, message=if(configured) "Ready to connect to Amazon." else "Purchases are unavailable in this build. Play the included starter adventures.") }
        if (configured) runCatching { PurchasingService.registerListener(activity.applicationContext,this) }.onFailure { fail("Amazon Appstore is unavailable on this device.") }
    }
    private fun fail(message: String) { timeout?.cancel(); mutable.update { it.copy(busy=false,message=message) } }
    private fun request(action: () -> Unit) {
        if (!configured || state.value.busy) return
        mutable.update { it.copy(busy=true,message="Connecting to Amazon…") }
        timeout?.cancel(); timeout=scope.launch { delay(25000); fail("Amazon did not respond. Check the connection and try again.") }
        runCatching(action).onFailure { fail("Amazon Appstore is unavailable. Please try again later.") }
    }
    fun refresh() = request { PurchasingService.getUserData(); PurchasingService.getProductData(setOf(Entitlements.SKU)) }
    fun purchase() {
        if (state.value.price==null) { refresh(); return }
        request { PurchasingService.purchase(Entitlements.SKU) }
    }
    fun restore() = request { restoredCount=0; restoreHadFailure=false; PurchasingService.getUserData(); PurchasingService.getPurchaseUpdates(true) }
    fun sync() { if(configured) runCatching { restoredCount=0; restoreHadFailure=false; PurchasingService.getUserData(); PurchasingService.getPurchaseUpdates(false) } }
    override fun onUserDataResponse(response: UserDataResponse) {
        if (response.requestStatus==UserDataResponse.RequestStatus.SUCCESSFUL) {
            activeUser=response.userData.userId
            scope.launch { store.update { s -> if(s.storeUser.isNotBlank() && s.storeUser!=activeUser) s.copy(premium=false,storeUser=activeUser) else s.copy(storeUser=activeUser) } }
        }
    }
    override fun onProductDataResponse(response: ProductDataResponse) {
        val product=response.productData?.get(Entitlements.SKU)
        timeout?.cancel()
        mutable.update { it.copy(busy=false,price=product?.price,message=if(product!=null) "One purchase. All adventures. No subscription." else "Premium is not available from Amazon right now.") }
    }
    override fun onPurchaseResponse(response: PurchaseResponse) {
        when(response.requestStatus) {
            PurchaseResponse.RequestStatus.SUCCESSFUL -> verify(response.receipt,response.userData.userId)
            PurchaseResponse.RequestStatus.ALREADY_PURCHASED -> { fail("Purchase already owned. Restoring…"); restore() }
            else -> fail("Purchase was not completed. Nothing has been unlocked.")
        }
    }
    override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) {
        if(response.requestStatus!=PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL) { fail("Could not restore purchases. Please try again."); return }
        val receipts=response.receipts.filter { it.sku==Entitlements.SKU }
        restoredCount+=receipts.size
        scope.launch {
            for(receipt in receipts) if(!verifyReceipt(receipt,response.userData.userId)) restoreHadFailure=true
            if(response.hasMore()) PurchasingService.getPurchaseUpdates(false)
            else fail(if(restoreHadFailure) "Some purchases could not be verified. Connect and restore again." else if(restoredCount==0) "Restore finished. No new premium purchase was found." else "Restore finished. Verified purchases have been applied.")
        }
    }
    private fun verify(receipt: Receipt, user: String) { scope.launch { verifyReceipt(receipt,user) } }
    private suspend fun verifyReceipt(receipt: Receipt, user: String): Boolean {
        if(receipt.sku!=Entitlements.SKU) return false
        try {
            val result = withContext(Dispatchers.IO) {
                val connection=URL(BuildConfig.VERIFICATION_URL).openConnection() as HttpsURLConnection
                try {
                    connection.requestMethod="POST"; connection.connectTimeout=12000; connection.readTimeout=12000
                    connection.doOutput=true; connection.setRequestProperty("Content-Type","application/json")
                    val body=JSONObject().put("userId",user).put("receiptId",receipt.receiptId).toString()
                    connection.outputStream.use { it.write(body.toByteArray()) }
                    check(connection.responseCode==200)
                    val json=connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
                    VerifiedReceipt(json.getString("sku"),json.getString("userId"),json.getString("receiptId"),json.getBoolean("cancelled"))
                } finally { connection.disconnect() }
            }
            check(Entitlements.accepts(result,user,receipt.receiptId))
            store.update { it.copy(premium=!result.cancelled,storeUser=user) }
            if(!result.cancelled) PurchasingService.notifyFulfillment(receipt.receiptId,FulfillmentResult.FULFILLED)
            fail(if(result.cancelled) "This purchase has been cancelled." else "Premium is ready. Enjoy your adventures!")
            return true
        } catch(_: Exception) { fail("Could not verify this purchase. Connect and restore purchases to try again."); return false }
    }
}
