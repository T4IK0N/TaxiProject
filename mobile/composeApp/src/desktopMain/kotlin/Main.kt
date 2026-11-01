import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.taxicompany.app.core.DesktopKeyringSecretStore
import com.taxicompany.app.core.SecretStore
import com.taxicompany.app.ui.App
import org.koin.dsl.module

private val platformModule = module {
    single<SecretStore> { DesktopKeyringSecretStore() }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Taxi Company App") {
        App(platformModule)
    }
}