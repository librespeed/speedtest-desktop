package util

import java.util.prefs.Preferences

object Settings {

    private val preferences = Preferences.userRoot().node("org/librespeed/desktop").apply {
        //earlier releases wrote these keys directly at the shared user root
        val legacyRoot = Preferences.userRoot()
        for (key in arrayOf("night-theme", "custom-servers")) {
            if (get(key, null) == null) {
                legacyRoot.get(key, null)?.let {
                    put(key, it)
                    legacyRoot.remove(key)
                }
            }
        }
    }

    var isNightTheme : Boolean
        set(value) {
            preferences.putBoolean("night-theme", value)
        }
        get() = preferences.getBoolean("night-theme", true)

    var customServers : String
        set(value) {
            preferences.put("custom-servers", value)
        }
        get() = preferences.get("custom-servers", "[]")

}