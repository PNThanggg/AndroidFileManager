package com.project

import android.content.Intent
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.project.databinding.ActivitySplashBinding
import com.module.core.base.BaseActivity
import com.modules.core.datastore.repository.LocalPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(inflater)
    }

    @Inject
    lateinit var preferencesRepository: LocalPreferencesRepository

    override fun initView() {
        lifecycleScope.launch {
            preferencesRepository.applicationPreferences.collect { prefs ->
                if (prefs.firstLaunch) {
                    startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
                } else {
                    startActivity(
                        Intent(
                            this@SplashActivity, HomeActivity::class.java
                        )
                    )
                }
                finish()
            }
        }
    }

    override fun initData() {
    }

    override fun initListener() {
    }
}