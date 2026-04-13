package com.beastsaber.app

import android.app.Application
import com.beastsaber.app.data.db.PlaylistDatabase

class BeastSaberApplication : Application() {
    val database by lazy { PlaylistDatabase.get(this) }
}
