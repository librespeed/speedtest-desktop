package util

import java.util.prefs.Preferences

object Settings {

    private var preferences = Preferences.userRoot()

    var isNightTheme : Boolean
        set(value) {
            preferences.putBoolean("night-theme", value)
        }
        get() = preferences.getBoolean("night-theme", true)

}