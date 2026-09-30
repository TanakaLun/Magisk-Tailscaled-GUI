package io.github.tanakalun.tailcontrol.service.tile

import android.app.Activity
import android.os.Bundle

class DummyActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
