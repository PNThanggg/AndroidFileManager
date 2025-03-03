package com.features.file.manager.extensions

import android.content.Context
import com.features.file.manager.utils.Config

val Context.config: Config get() = Config.newInstance(applicationContext)