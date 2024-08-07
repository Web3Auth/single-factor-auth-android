package com.web3auth.sfaexample

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.auth0.jwt.algorithms.Algorithm
import com.web3auth.singlefactorauth.SingleFactorAuth
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.SingleFactorAuthArgs
import com.web3auth.singlefactorauth.types.TorusKey
import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {

    private lateinit var btnTorusKey: Button
    private lateinit var tv: TextView
    lateinit var singleFactorAuth: SingleFactorAuth
    private lateinit var singleFactorAuthArgs: SingleFactorAuthArgs
    lateinit var loginParams: LoginParams
    lateinit var algorithmRs: Algorithm
    var TEST_VERIFIER = "torus-test-health"
    var TORUS_TEST_EMAIL = "hello@tor.us"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTorusKey = findViewById(R.id.btnTorusKey)
        tv = findViewById(R.id.tv)

        btnTorusKey.setOnClickListener {
            getTorusKey()
        }

        singleFactorAuthArgs = SingleFactorAuthArgs(Web3AuthNetwork.AQUA, "YOUR_CLIENT_ID")
        singleFactorAuth = SingleFactorAuth(singleFactorAuthArgs)
        val sessionResponse: CompletableFuture<TorusKey> = singleFactorAuth.initialize(this.applicationContext)
        sessionResponse.whenComplete { torusKey, error ->
            if (error == null) {
                tv.text = "Private Key: ${torusKey.privateKey}"
            } else {
                Log.d("MainActivity_SFA", error.message ?: "Something went wrong")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTorusKey() {
        val idToken = JwtUtils.generateIdToken(TORUS_TEST_EMAIL)
        loginParams = LoginParams(TEST_VERIFIER, TORUS_TEST_EMAIL, idToken)
        val torusKey: TorusKey =
            singleFactorAuth.getKey(loginParams, this.applicationContext, 86400).get()
        tv.text = "Private Key: ${torusKey.privateKey}"
    }
}