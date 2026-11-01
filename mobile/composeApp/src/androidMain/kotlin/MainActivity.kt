package com.taxicompany.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.taxicompany.app.core.AndroidKeystoreSecretStore
import com.taxicompany.app.core.SecretStore
import com.taxicompany.app.ui.App
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val platformModule = module {
            single<SecretStore> { AndroidKeystoreSecretStore(applicationContext) }
        }

        setContent {
            App(platformModule)
        }
    }
}