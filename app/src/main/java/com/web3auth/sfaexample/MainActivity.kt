package com.web3auth.sfaexample

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
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

        sfaParams =
            SingleFactorAuthArgs(Web3AuthNetwork.SAPPHIRE_MAINNET, "YOUR_CLIENT_ID", null,0)
        singleFactorAuth = SingleFactorAuth(sfaParams, this)
        singleFactorAuth.initialize()
        singleFactorAuth.getKey(loginParams)
    }

    private fun getTorusKey() {
        val idToken = JwtUtils.generateIdToken(TORUS_TEST_EMAIL)
        loginParams = LoginParams(TEST_VERIFIER, TORUS_TEST_EMAIL, idToken)
        val TorusSFAKey =
            singleFactorAuth.getKey(loginParams)
        if (TorusSFAKey != null) {
            val text = "Private Key: ${TorusSFAKey.getPrivateKey()}"
            tv.text = text
        }
    }
}