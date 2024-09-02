package com.web3auth.sfaexample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.web3auth.singlefactorauth.SingleFactorAuth
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SingleFactorAuthArgs
import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork

class MainActivity : AppCompatActivity() {

    private lateinit var btnTorusKey: Button
    private lateinit var tv: TextView
    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var sfaParams: SingleFactorAuthArgs
    lateinit var loginParams: LoginParams
    var TEST_VERIFIER = "torus-test-health"
    var TORUS_TEST_EMAIL = "hello@tor.us"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTorusKey = findViewById(R.id.btnTorusKey)
        tv = findViewById(R.id.tv)

        btnTorusKey.setOnClickListener {
            getTorusKey()
        }
        val idToken = JwtUtils.generateIdToken(TORUS_TEST_EMAIL)
        sfaParams =
            SingleFactorAuthArgs(Web3AuthNetwork.SAPPHIRE_MAINNET, "YOUR_CLIENT_ID", null,0)
        singleFactorAuth = SingleFactorAuth(sfaParams, this)
        loginParams = LoginParams(TEST_VERIFIER, TORUS_TEST_EMAIL, idToken)

        // Consider exposing getSessionId() to avoid this try..catch in all implementations of this SDK
        try {
            // Already has done login and has sessionId stored.
            singleFactorAuth.initialize(this.applicationContext)
        } catch (ex: java.lang.Exception) {
            // Try login and create new session which will then be stored
            singleFactorAuth.getKey(loginParams, this)
            singleFactorAuth.initialize(this.applicationContext)
        }
    }

    private fun getTorusKey() {
        val idToken = JwtUtils.generateIdToken(TORUS_TEST_EMAIL)
        loginParams = LoginParams(TEST_VERIFIER, TORUS_TEST_EMAIL, idToken)
        val TorusSFAKey =
            singleFactorAuth.getKey(loginParams, this.applicationContext)
        if (TorusSFAKey != null) {
            val text = "Private Key: ${TorusSFAKey.getPrivateKey()}"
            tv.text = text
        }
    }
}