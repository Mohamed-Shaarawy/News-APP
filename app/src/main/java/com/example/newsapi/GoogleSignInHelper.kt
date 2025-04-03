
    package com.example.newsapi

    import android.content.ContentValues.TAG
    import android.content.Context
    import android.util.Log
    import android.widget.Toast
    import androidx.core.os.unregisterForAllProfilingResults
    import androidx.credentials.CredentialManager
    import androidx.credentials.CustomCredential
    import androidx.credentials.GetCredentialRequest
    import androidx.credentials.GetCredentialResponse
    import androidx.credentials.PasswordCredential
    import androidx.credentials.PublicKeyCredential
    import androidx.credentials.exceptions.GetCredentialException
    import androidx.navigation.Navigation.findNavController
    import com.google.android.libraries.identity.googleid.GetGoogleIdOption
    import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
    import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import java.security.MessageDigest


    class GoogleSignInHelper(private val context: Context, private val onSignInSuccess: () -> Unit) {

        private val WEB_CLIENT_ID = "256229435753-8crrh1he599cf8tmfqu1697j61t3gena.apps.googleusercontent.com"
        private val rawNonce = "dummyNonce123456".toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(rawNonce)
        val hashedNonce = digest.fold(""){str, it -> str + "%02x".format(it)}
        var flag:Boolean = false
        // callback to be invoked on success
        // Initialize Google ID Option
        private val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .setNonce(hashedNonce)
            .build()

        // Initialize Credential Manager
         val credentialManager = CredentialManager.create(context)

        // Create the GetCredentialRequest
        private val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Coroutine scope to handle the credential request
        private val coroutineScope = CoroutineScope(Dispatchers.Main)

        fun signIn() {
            coroutineScope.launch {
                try {
                    // Perform the credential retrieval request
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
                    handleSignIn(result)
                    onSignInSuccess()
                } catch (e: GetCredentialException) {
                    Toast.makeText(context, "No Credentials on your device", Toast.LENGTH_SHORT).show()
                    handleFailure(e)
                }
            }
        }

        // Method to handle the result of the sign-in
        fun handleSignIn(result: GetCredentialResponse) {
            // Handle the successfully returned credential.
            val credential = result.credential

            when (credential) {
                // Passkey credential
                is PublicKeyCredential -> {
                    // validate and authenticate
                    val responseJson = credential.authenticationResponseJson
                }

                // Password credential
                is PasswordCredential -> {
                    // Send ID and password to your server to validate and authenticate.
                    val username = credential.id
                    val password = credential.password
                }

                // GoogleIdToken credential
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        try {
                            // Use googleIdTokenCredential and extract the ID to validate and
                            // authenticate on your server.
                            val googleIdTokenCredential = GoogleIdTokenCredential
                                .createFrom(credential.data)

                            val idTokenString = googleIdTokenCredential.idToken
                            // Validation instructions for Google ID token here (e.g., using GoogleIdTokenVerifier)
                        } catch (e: GoogleIdTokenParsingException) {
                            Log.e("GoogleSignIn", "Received an invalid google id token response", e)
                        }
                    } else {
                        // Catch any unrecognized custom credential type here.
                        Log.e("GoogleSignIn", "Unexpected type of credential")
                    }
                }

                else -> {
                    // Catch any unrecognized credential type here.
                    Log.e("GoogleSignIn", "Unexpected type of credential")
                }
            }
        }

        // Handle failures in the sign-in process
        fun handleFailure(e: GetCredentialException) {
            // Log or handle the exception in some way
            Log.e("GoogleSignIn", "Failed to retrieve credential", e)
        }
    }
