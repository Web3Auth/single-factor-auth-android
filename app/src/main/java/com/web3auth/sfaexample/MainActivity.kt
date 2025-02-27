package com.web3auth.sfaexample

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonArray
import com.web3auth.singlefactorauth.SingleFactorAuth
import com.web3auth.singlefactorauth.types.ChainConfig
import com.web3auth.singlefactorauth.types.ChainNamespace
import com.web3auth.singlefactorauth.types.LoginParams
import com.web3auth.singlefactorauth.types.Web3AuthOptions
import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork
import org.web3j.crypto.Credentials

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
            getSessionData()
        }
        val idToken = JwtUtils.generateIdToken(TORUS_TEST_EMAIL)
        val web3AuthOptions =
            Web3AuthOptions(
                "YOUR_CLIENT_ID",
                Web3AuthNetwork.SAPPHIRE_MAINNET,
                86400,
                redirectUrl = Uri.parse("torusapp://org.torusresearch.web3authexample")
            )
        singleFactorAuth = SingleFactorAuth(web3AuthOptions, this)
        loginParams = LoginParams(TEST_VERIFIER, TORUS_TEST_EMAIL, idToken)

        singleFactorAuth.initialize(this.applicationContext).whenComplete { res, err ->
            if (err == null) {
                val text =
                    "Public Address: ${singleFactorAuth.getSessionData()?.publicAddress} , Private Key: ${singleFactorAuth.getSessionData()?.privateKey}"
                tv.text = text
            }
        }

        val showWalletUI = findViewById<Button>(R.id.showWalletUI)
        showWalletUI.setOnClickListener {
            val launchWalletCompletableFuture = singleFactorAuth.showWalletUI(
                chainConfig = ChainConfig(
                    chainId = "0x89",
                    rpcTarget = "https://1rpc.io/matic",
                    chainNamespace = ChainNamespace.EIP155
                )
            )
            launchWalletCompletableFuture.whenComplete { _, error ->
                if (error == null) {
                    Log.d("MainActivity_Web3Auth", "Wallet launched successfully")
                } else {
                    Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong")
                }
            }
        }

        val signMsgButton = findViewById<Button>(R.id.signMsgButton)
        signMsgButton.setOnClickListener {
            val credentials: Credentials =
                Credentials.create(singleFactorAuth.getSessionData()?.privateKey)
            val params = JsonArray().apply {
                add("Hello, World!")
                add(credentials.address)
                add("Android")
            }
            val signMsgCompletableFuture = singleFactorAuth.request(
                chainConfig = ChainConfig(
                    chainId = "0x89",
                    rpcTarget = "https://polygon-rpc.com/",
                    chainNamespace = ChainNamespace.EIP155
                ), "personal_sign", requestParams = params, appState = "web3Auth"
            )
            signMsgCompletableFuture.whenComplete { signResult, error ->
                if (error == null) {
                    showAlertDialog("Sign Result", signResult.toString())
                } else {
                    Log.d("MainActivity_Web3Auth", error.message ?: "Something went wrong")
                }
            }
        }
    }

    private fun getSessionData() {
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

    private fun showAlertDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}