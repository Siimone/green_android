package com.blockstream.green.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.blockstream.gdk.AssetManager
import com.blockstream.gdk.GreenWallet
import com.blockstream.gdk.Logger
import com.blockstream.green.ApplicationScope
import com.blockstream.green.BuildConfig
import com.blockstream.green.GreenApplication
import com.blockstream.green.R
import com.blockstream.green.database.WalletRepository
import com.blockstream.green.gdk.SessionManager
import com.blockstream.green.lifecycle.AppLifecycleObserver
import com.blockstream.green.managers.NotificationManager
import com.blockstream.green.settings.Migrator
import com.blockstream.green.settings.SettingsManager
import com.blockstream.green.utils.AppKeystore
import com.blockstream.green.utils.QATester
import com.blockstream.green.utils.isDevelopmentFlavor
import com.blockstream.green.utils.isDevelopmentOrDebug
import com.blockstream.libgreenaddress.KotlinGDK
import com.blockstream.libwally.KotlinWally
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.common.configuration.Behavior
import com.pandulapeter.beagle.logCrash.BeagleCrashLogger
import com.pandulapeter.beagle.modules.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.MainScope
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class GreenModules {
    @Singleton
    @Provides
    fun provideApplicationScope(): ApplicationScope {
        return MainScope()
    }

    @Singleton
    @Provides
    fun provideKotlinGDK(): KotlinGDK {
        return KotlinGDK()
    }

    @Singleton
    @Provides
    fun provideKotlinWally(): KotlinWally {
        return KotlinWally()
    }

    @Singleton
    @Provides
    fun provideGreenWallet(
        @ApplicationContext context: Context,
        gdk: KotlinGDK,
        wally: KotlinWally,
        sharedPreferences: SharedPreferences,
        beagle: Beagle
    ): GreenWallet {
        var logger : Logger? = null

        if(context.isDevelopmentOrDebug()){
            logger = object : Logger{
                override fun log(message: String) {
                    beagle.log(message)
                }
            }
        }
        return GreenWallet(
            gdk = gdk,
            wally = wally,
            sharedPreferences = sharedPreferences,
            dataDir = context.filesDir,
            developmentFlavor = context.isDevelopmentFlavor(),
            extraLogger = logger
        )
    }

    @Singleton
    @Provides
    fun provideSessionManager(
        applicationScope: ApplicationScope,
        settingsManager: SettingsManager,
        assetManager: AssetManager,
        greenWallet: GreenWallet,
        qaTester: QATester
    ): SessionManager {
        return SessionManager(applicationScope, settingsManager, assetManager, greenWallet, qaTester)
    }

    @Singleton
    @Provides
    fun provideAssetManager(@ApplicationContext context: Context, QATester: QATester): AssetManager {
        return AssetManager(context, QATester)
    }

    @Singleton
    @Provides
    fun provideSettingsManager(@ApplicationContext context: Context, sharedPreferences: SharedPreferences): SettingsManager {
        return SettingsManager(context, sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideAppKeystore(): AppKeystore {
        return AppKeystore()
    }

    @Singleton
    @Provides
    fun provideMigrator(@ApplicationContext context: Context, walletRepository: WalletRepository, greenWallet: GreenWallet, settingsManager: SettingsManager, applicationScope: ApplicationScope): Migrator {
        return Migrator(context, walletRepository, greenWallet, settingsManager, applicationScope)
    }

    @Singleton
    @Provides
    fun provideAppLifecycleObserver(): AppLifecycleObserver {
        return AppLifecycleObserver()
    }

    @Singleton
    @Provides
    fun provideQATester(@ApplicationContext context: Context): QATester {
        return QATester(context)
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Singleton
    @Provides
    fun provideAndroidNotificationManager(@ApplicationContext context: Context): android.app.NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    }

    @Singleton
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        applicationScope: ApplicationScope,
        androidNotificationManager: android.app.NotificationManager,
        sessionManager: SessionManager,
        settingsManager: SettingsManager,
        walletRepository: WalletRepository
    ): NotificationManager {
        return NotificationManager(
            context,
            applicationScope,
            androidNotificationManager,
            sessionManager,
            settingsManager,
            walletRepository
        )
    }

    @Singleton
    @Provides
    fun provideBeagle(@ApplicationContext context: Context): Beagle {

        if (context.isDevelopmentOrDebug()) {
            Beagle.initialize(
                context as GreenApplication,
                behavior = Behavior(
                    bugReportingBehavior = Behavior.BugReportingBehavior(
                        crashLoggers = listOf(
                            BeagleCrashLogger
                        )
                    )
                )
            )

            Beagle.set(
                HeaderModule(
                    title = context.getString(R.string.app_name),
                    subtitle = BuildConfig.APPLICATION_ID,
                    text = "${BuildConfig.BUILD_TYPE} v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                ),
                AppInfoButtonModule(),
                DeveloperOptionsButtonModule(),
                PaddingModule(),
                TextModule("General", TextModule.Type.SECTION_HEADER),
                KeylineOverlaySwitchModule(),
                AnimationDurationSwitchModule(),
                ScreenCaptureToolboxModule(),
                DividerModule(),
                TextModule("Logs", TextModule.Type.SECTION_HEADER),
                LogListModule(maxItemCount = 50),
                DividerModule(),
                TextModule("Other", TextModule.Type.SECTION_HEADER),
                DeviceInfoModule(),
                BugReportButtonModule()
            )
        }

        return Beagle
    }

}