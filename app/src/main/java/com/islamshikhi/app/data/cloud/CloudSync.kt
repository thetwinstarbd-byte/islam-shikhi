package com.islamshikhi.app.data.cloud

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class CloudUser(val uid: String, val name: String?, val email: String?)

/** Firestore-এর এক ডকুমেন্টের সীমা ১ MiB; নিরাপদ থাকতে এর নিচে রাখা হয়। */
object CloudLimits {
    const val MAX_CHARS = 900_000
    fun fits(json: String): Boolean = json.length <= MAX_CHARS
}

/**
 * ঐচ্ছিক ক্লাউড: Google দিয়ে সাইন-ইন করে ব্যাকআপ (বুকমার্ক, নোট, আমল ইত্যাদি) Firebase-এ রাখা ও ফেরত আনা।
 * ব্যবহারকারী বাটন চাপলেই কাজ হয়; নিজে থেকে কিছু পাঠানো হয় না। Firebase সেট না করা থাকলে [isConfigured] false।
 * ডেটা থাকে users/{uid} ডকুমেন্টে; নিরাপত্তা-নিয়ম firebase/firestore.rules ফাইলে (শুধু নিজের ডকুমেন্ট)।
 */
object CloudSync {

    fun isConfigured(context: Context): Boolean = runCatching {
        FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
    }.getOrDefault(false)

    fun currentUser(context: Context): CloudUser? {
        if (!isConfigured(context)) return null
        val u = FirebaseAuth.getInstance().currentUser ?: return null
        return CloudUser(u.uid, u.displayName, u.email)
    }

    /** [activityContext] অবশ্যই Activity হতে হবে (Google-এর অ্যাকাউন্ট-বাছাই পর্দা দেখাতে)। */
    suspend fun signInWithGoogle(activityContext: Context): CloudUser {
        check(isConfigured(activityContext)) { "ক্লাউড চালু নেই" }
        val res = activityContext.resources
        val id = res.getIdentifier("default_web_client_id", "string", activityContext.packageName)
        check(id != 0) { "Google সাইন-ইনের কনফিগারেশন নেই" }
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(activityContext.getString(id))
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val credential = CredentialManager.create(activityContext).getCredential(activityContext, request).credential
        check(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            "অচেনা সাইন-ইন ফলাফল"
        }
        val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val result = FirebaseAuth.getInstance()
            .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).awaitIt()
        val u = result.user ?: error("সাইন-ইন সম্পন্ন হয়নি")
        return CloudUser(u.uid, u.displayName, u.email)
    }

    suspend fun signOut(context: Context) {
        if (!isConfigured(context)) return
        FirebaseAuth.getInstance().signOut()
        runCatching { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
    }

    private fun doc(uid: String) = FirebaseFirestore.getInstance().collection("users").document(uid)

    /** ব্যাকআপ-JSON ক্লাউডে রাখে (আগেরটি বদলে যায়)। */
    suspend fun upload(uid: String, json: String) {
        require(CloudLimits.fits(json)) { "তথ্য ক্লাউডে রাখার জন্য বেশি বড়" }
        doc(uid).set(mapOf("data" to json, "v" to 1, "updatedAt" to FieldValue.serverTimestamp())).awaitIt()
    }

    /** ক্লাউডে রাখা ব্যাকআপ-JSON; কিছু না থাকলে null। */
    suspend fun download(uid: String): String? {
        val snap = doc(uid).get().awaitIt()
        return if (snap.exists()) snap.getString("data") else null
    }

    /** ক্লাউড থেকে নিজের তথ্য ও অ্যাকাউন্ট দুটোই মুছে ফেলে। সাইন-ইন অনেক আগের হলে ব্যর্থ হতে পারে। */
    suspend fun deleteAccount(context: Context, uid: String) {
        doc(uid).delete().awaitIt()
        FirebaseAuth.getInstance().currentUser?.delete()?.awaitIt()
        runCatching { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
    }

    private suspend fun <T> Task<T>.awaitIt(): T = suspendCancellableCoroutine { c ->
        addOnSuccessListener { c.resume(it) }
        addOnFailureListener { c.resumeWithException(it) }
    }
}
