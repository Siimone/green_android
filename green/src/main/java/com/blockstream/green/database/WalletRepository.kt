package com.blockstream.green.database

import androidx.lifecycle.LiveData
import com.blockstream.gdk.data.Network
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WalletRepository @Inject constructor(private val walletDao: WalletDao) {

    suspend fun deleteWalletSuspend(wallet: Wallet) = walletDao.deleteSuspend(wallet)
    fun deleteWalletSync(wallet: Wallet) = walletDao.deleteSync(wallet)
    fun updateWalletSync(wallet: Wallet) = walletDao.updateSync(wallet)
    suspend fun updateWalletSuspend(wallet: Wallet) = walletDao.updateSuspend(wallet)

    fun deleteWallets() = walletDao.deleteWallets()

    fun getWalletLiveData(id: WalletId) = walletDao.getWalletLiveData(id)
    fun getWalletSync(id: WalletId) = walletDao.getWalletSync(id)

    fun getWalletObservable(id: WalletId) = walletDao.getWalletObservable(id)
    suspend fun getWalletSuspend(id: WalletId) = walletDao.getWalletSuspend(id)

    fun getWalletWithHashIdSync(walletHashId: String, isHardware: Boolean) = walletDao.getWalletWithHashIdSync(walletHashId, isHardware)

    fun addWallet(wallet: Wallet) = walletDao.insert(wallet)
    suspend fun addWalletSuspend(wallet: Wallet) = walletDao.insertSuspend(wallet)

    fun addLoginCredentialsSync(loginCredentials: LoginCredentials) = walletDao.insertSync(loginCredentials)
    suspend fun addLoginCredentialsSuspend(loginCredentials: LoginCredentials) = walletDao.insertSuspend(loginCredentials)
    fun updateLoginCredentialsSync(vararg loginCredentials: LoginCredentials) = walletDao.updateLoginCredentialsSync(*loginCredentials)
    fun deleteLoginCredentialsSync(loginCredentials: LoginCredentials) = walletDao.deleteLoginCredentialsSync(loginCredentials)
    suspend fun deleteLoginCredentialsSuspend(loginCredentials: LoginCredentials) = walletDao.deleteLoginCredentialsSuspend(loginCredentials)

    fun getWalletLoginCredentials(id: WalletId) = walletDao.getWalletLoginCredentials(id)
    suspend fun getWalletLoginCredentialsSuspend(id: WalletId) = walletDao.getWalletLoginCredentialsSuspend(id)
    fun getWalletLoginCredentialsObservable(id: WalletId) = walletDao.getWalletLoginCredentialsObservable(id)
    suspend fun getLoginCredentialsSuspend(id: WalletId) = walletDao.getLoginCredentialsSuspend(id)
    suspend fun getLoginCredentialsSuspend(id: WalletId, type: CredentialType) = walletDao.getLoginCredentialsSuspend(id,type)
    fun deleteLoginCredentials() = walletDao.deleteLoginCredentials()

    fun deleteLoginCredentialsSync(walletId: WalletId, type: CredentialType) = walletDao.deleteLoginCredentialsSync(walletId, type.value)
    suspend fun deleteLoginCredentialsSuspend(walletId: WalletId, type: CredentialType) = walletDao.deleteLoginCredentialsSuspend(walletId, type.value)

    fun walletsExistsSync(walletHashId: String, isHardware: Boolean) = walletDao.walletsExistsSync(walletHashId, isHardware)

    fun walletsExists() = walletDao.walletsExists()
    suspend fun walletsExistsSuspend() = walletDao.walletsExistsSuspend()
    fun getWalletsLiveData(): LiveData<List<Wallet>> = walletDao.getWalletsLiveData()
    suspend fun getWalletsSuspend(): List<Wallet> = walletDao.getWalletsSuspend()
    fun getWalletsSync() = walletDao.getWalletsSync()
    fun getSoftwareWallets(): LiveData<List<Wallet>> = walletDao.getSoftwareWallets()
    fun getHardwareWallets(): LiveData<List<Wallet>> = walletDao.getHardwareWallets()
    suspend fun getWalletsForNetworkSuspend(network: Network): List<Wallet> = walletDao.getWalletsForNetworkSuspend(network.network)
    fun getWalletsForNetworkSync(network: Network): List<Wallet> = walletDao.getWalletsForNetworkSync(network.network)
}