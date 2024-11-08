package com.web3auth.sfaexample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.web3auth.singlefactorauth.SingleFactorAuth
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.Web3AuthOptions
import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork

class MainActivity : AppCompatActivity() {

    private lateinit var btnTorusKey: Button
    private lateinit var tv: TextView
    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var loginParams: LoginParams
    var TEST_VERIFIER = "torus-test-health"
    var TORUS_TEST_EMAIL = "hello@tor.us"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTorusKey = findViewById(R.id.btnTorusKey)
        tv = findViewById(R.id.tv)

        btnTorusKey.setOnClickListener {
            getSFAKey()
        }
        val idToken = JwtUtils.generateIdToken(TORUS_TEST_EMAIL)
        val web3AuthOptions =
            Web3AuthOptions("YOUR_CLIENT_ID", Web3AuthNetwork.SAPPHIRE_MAINNET, 86400)
        singleFactorAuth = SingleFactorAuth(web3AuthOptions, this)
        loginParams = LoginParams(TEST_VERIFIER, TORUS_TEST_EMAIL, idToken)

        val sfakey = singleFactorAuth.initialize(this.applicationContext)
        sfakey.whenComplete { response, error ->
            if (response != null) {
                val text =
                    "Public Address: ${response?.publicAddress} , Private Key: ${response?.privateKey}"
                tv.text = text
            } else {
                tv.text = error.message
            }
        }

    }

    private fun getSFAKey() {
        val idToken = JwtUtils.generateIdToken(TORUS_TEST_EMAIL)
        loginParams = LoginParams(TEST_VERIFIER, TORUS_TEST_EMAIL, idToken)
        val sfakey =
            singleFactorAuth.connect(loginParams, this.applicationContext)
        if (sfakey != null) {
            val text =
                "Public Address: ${sfakey.publicAddress} , Private Key: ${sfakey.privateKey}"
            tv.text = text
        }
    }
}