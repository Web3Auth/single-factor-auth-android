package com.web3auth.singlefactorauth.types

class AggregateVerifierParams {
    lateinit var verify_params: Array<VerifierParams?>
    lateinit var sub_verifier_ids: Array<String?>
    lateinit var verifier_id: String

    fun setVerifyParamItem(verify_param: VerifierParams, index: Int) {
        verify_params[index] = verify_param
    }

    fun setSubVerifierIdItem(sub_verifier_id: String, index: Int) {
        sub_verifier_ids[index] = sub_verifier_id
    }

    class VerifierParams(val verifier_id: String, val idtoken: String)
}
