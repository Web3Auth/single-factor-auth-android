package com.web3auth.sfaexample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.web3auth.singlefactorauth.SingleFactorAuth
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SFAParams
import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork

class MainActivity : AppCompatActivity() {

    private lateinit var btnTorusKey: Button
    private lateinit var tv: TextView
    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var sfaParams: SFAParams
    lateinit var loginParams: LoginParams
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
        sfaParams =
            SFAParams(Web3AuthNetwork.SAPPHIRE_MAINNET, "YOUR_CLIENT_ID", 86400, null, 0)
        singleFactorAuth = SingleFactorAuth(sfaParams, this)
        loginParams = LoginParams(TEST_VERIFIER, TORUS_TEST_EMAIL, idToken)

        val res = singleFactorAuth.isSessionIdExists(this)
        res.whenComplete { res, err ->
            if (res) {
                val sfakey = singleFactorAuth.initialize(this.applicationContext)
                sfakey.whenComplete { response, error ->
                    if (error == null) {
                        val text =
                            "Public Address: ${response.getPublicAddress()} , Private Key: ${response.getPrivateKey()}"
                        tv.text = text
                    } else {
                        tv.text = error.message
                    }
                }
            } else {
                tv.text = err.message
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
                "Public Address: ${sfakey.getPublicAddress()} , Private Key: ${sfakey.getPrivateKey()}"
            tv.text = text
        }
    }
}