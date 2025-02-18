package com.blockstream.green.devices

import com.blockstream.gdk.HardwareWalletResolver
import com.blockstream.gdk.data.DeviceRequiredData
import com.blockstream.gdk.data.DeviceResolvedData
import com.blockstream.green.gdk.GreenSession
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.greenaddress.greenapi.HWWallet.SignTxResult
import com.greenaddress.greenapi.HWWalletBridge
import io.reactivex.rxjava3.core.Single
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class DeviceResolver constructor(private val session: GreenSession, private val hwWalletBridge: HWWalletBridge? = null) :
    HardwareWalletResolver {
    private val objectMapper by lazy { ObjectMapper() }

    override fun requestDataFromDevice(requiredData: DeviceRequiredData): Single<String> {
        return Single.create { emitter ->
            try {
                val data = requestDataFromHardware(requiredData)
                if (data != null) {
                    emitter.onSuccess(data)
                } else {
                    emitter.tryOnError(Exception("Unknown error"))
                }
            } catch (e: Exception) {
                emitter.tryOnError(e)
            }
        }
    }


    private fun toObjectNode(jsonElement: JsonElement?): ObjectNode {
        return objectMapper.readTree(Json.encodeToString(jsonElement)) as ObjectNode
    }

    @Synchronized
    fun requestDataFromHardware(requiredData: DeviceRequiredData): String? {

        val hwWallet = session.hwWallet ?: return null

        return when (requiredData.action) {
            "get_xpubs" -> {
                hwWallet.getXpubs(session.network, hwWalletBridge, requiredData.paths?.map { it.map { it.toInt() } }).let {
                    DeviceResolvedData(
                        xpubs = it
                    )
                }
            }

            "sign_message" -> {
                hwWallet.signMessage(
                    hwWalletBridge,
                    requiredData.path?.map { it.toInt() },
                    requiredData.message,
                    requiredData.useAeProtocol ?: false,
                    requiredData.aeHostCommitment,
                    requiredData.aeHostEntropy
                ).let {

                    val signerCommitment = it.signerCommitment.let { signerCommitment ->
                        // Corrupt the commitments to emulate a corrupted wallet
                        if (hwWallet.hardwareEmulator != null && hwWallet.hardwareEmulator?.getAntiExfilCorruptionForMessageSign() == true) {
                            // Make it random to allow proceeding to a logged in state
                            signerCommitment.replace("0", "1")
                        } else {
                            signerCommitment
                        }
                    }

                    DeviceResolvedData(
                        signature = it.signature,
                        signerCommitment = signerCommitment
                    )

                }
            }

            "sign_tx" -> {
                val result: SignTxResult
                if (session.network.isLiquid) {
                    result = hwWallet.signLiquidTransaction(
                        session.network,
                        hwWalletBridge,
                        toObjectNode(requiredData.transaction),
                        requiredData.signingInputs,
                        requiredData.transactionOutputs,
                        requiredData.signingTransactions,
                        requiredData.useAeProtocol ?: false
                    )
                } else {
                    result = hwWallet.signTransaction(
                        session.network,
                        hwWalletBridge,
                        toObjectNode(requiredData.transaction),
                        requiredData.signingInputs,
                        requiredData.transactionOutputs,
                        requiredData.signingTransactions,
                        requiredData.useAeProtocol ?: false
                    )
                }

                val signatures = result.signatures.let { signatures ->
                    // Corrupt the commitments to emulate a corrupted wallet
                    if (hwWallet.hardwareEmulator != null && hwWallet.hardwareEmulator?.getAntiExfilCorruptionForTxSign() == true) {
                        signatures.map {
                            it.replace("0", "1")
                        }
                    } else {
                        signatures
                    }
                }


                if (session.network.isLiquid) {
                    DeviceResolvedData(
                        signatures = signatures,
                        signerCommitments = result.signerCommitments,
                        assetCommitments = result.assetCommitments,
                        valueCommitments = result.valueCommitments,
                        assetBlinders = result.assetBlinders,
                        amountBlinders = result.amountBlinders
                    )
                } else {
                    DeviceResolvedData(
                        signatures = signatures,
                        signerCommitments = result.signerCommitments
                    )
                }
            }

            "get_master_blinding_key" -> {
                DeviceResolvedData(
                    masterBlindingKey = hwWallet.getMasterBlindingKey(hwWalletBridge)
                )
            }

            "get_blinding_nonces" -> {
                val nonces = mutableListOf<String>()
                val blindingPublicKeys = mutableListOf<String>()

                val scripts = requiredData.scripts
                val publicKeys = requiredData.publicKeys

                if(scripts != null && publicKeys != null && scripts.size == publicKeys.size){
                    for (i in 0 until (scripts.size)) {
                        nonces.add(
                            hwWallet.getBlindingNonce(
                                hwWalletBridge,
                                publicKeys[i],
                                scripts[i]
                            )
                        )

                        if(requiredData.blindingKeysRequired == true){
                            blindingPublicKeys.add(hwWallet.getBlindingKey(hwWalletBridge, scripts[i]));
                        }
                    }
                }

                DeviceResolvedData(
                    nonces = nonces,
                    publicKeys = blindingPublicKeys
                )
            }

            "get_blinding_public_keys" -> {
                val publicKeys = mutableListOf<String>()

                for (script in requiredData.scripts ?: listOf()) {
                    publicKeys.add(
                        hwWallet.getBlindingKey(hwWalletBridge, script)
                    )
                }

                DeviceResolvedData(
                    publicKeys = publicKeys
                )
            }

            else -> {
                null
            }
        }?.toJson()
    }
}
