package pepes.co.trofes

import android.app.Application
import pepes.co.trofes.data.remote.AppContextProvider

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContextProvider.init(this)
    }
}
