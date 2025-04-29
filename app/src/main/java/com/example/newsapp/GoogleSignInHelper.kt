package com.example.newsapp

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class GoogleSignInHelper(private val context: Context, private val onSignInSuccess: () -> Unit) {

    private val WEB_CLIENT_ID = "591330869207-ndia5ut0sc3olb9bs9oj0sr3lg0l30dj.apps.googleusercontent.com"
    private val rawNonce = "dummyNonce123456".toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(rawNonce)
    val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }
    var flag: Boolean = false

    private val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(WEB_CLIENT_ID)
        .setAutoSelectEnabled(true)
        .setNonce(hashedNonce)
        .build()

    val credentialManager = CredentialManager.create(context)

    private val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun signIn() {
        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken
                Log.d(TAG, googleIdToken)
                Toast.makeText(context, "Signed in", Toast.LENGTH_LONG).show()
                flag = true

                val GoogleCred = GoogleAuthProvider.getCredential(googleIdToken, null)
                FirebaseAuth.getInstance().signInWithCredential(GoogleCred)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = FirebaseAuth.getInstance().currentUser

                            // Check if the user exists in Firestore
                            val db = FirebaseFirestore.getInstance()
                            val userDocRef = db.collection("users").document(firebaseUser!!.uid)

                            // Retrieve the user document to check if it already exists
                            userDocRef.get().addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    // The user doesn't exist, so we create a new user document in Firestore
                                    val bundle = result.credential.data

                                    val displayName = bundle.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_DISPLAY_NAME")
                                    val email = bundle.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID")
                                    val givenName = bundle.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_GIVEN_NAME")
                                    val familyName = bundle.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_FAMILY_NAME")
                                    val profilePictureUrl = bundle.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_PROFILE_PICTURE_URI")

                                    val userData = hashMapOf(
                                        "uid" to (firebaseUser?.uid ?: ""),
                                        "displayName" to displayName,
                                        "email" to email,
                                        "givenName" to givenName,
                                        "familyName" to familyName,
                                        "profilePictureUrl" to profilePictureUrl
                                    )

                                    // Save the user data to Firestore
                                    userDocRef.set(userData)
                                        .addOnSuccessListener {
                                            Log.e("Google sign-in success", "Signed-in successfully")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Error writing user to Firestore", e)
                                        }
                                } else {
                                    // User already exists, no need to save data again
                                    Log.d(TAG, "User already exists in Firestore, no need to save data.")
                                }
                            }.addOnFailureListener { e ->
                                Log.e(TAG, "Error checking user existence in Firestore", e)
                            }
                        } else {
                            Log.e(TAG, "Firebase authentication failed", task.exception)
                        }
                    }
                onSignInSuccess()

            } catch (e: GetCredentialException) {
                Toast.makeText(context, "No Credentials on your device", Toast.LENGTH_SHORT).show()
            }
        }
    }

}


