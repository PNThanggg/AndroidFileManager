package com.file.manager

import android.content.Intent
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.file.manager.databinding.ActivityIntroBinding
import com.module.core.base.BaseActivity
import com.modules.core.datastore.repository.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IntroActivity : BaseActivity<ActivityIntroBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityIntroBinding {
        return ActivityIntroBinding.inflate(inflater)
    }

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun initView() {
        binding.buttonNext.setOnClickListener {
            startActivity(
                Intent(
                    this@IntroActivity, HomeActivity::class.java
                )
            )

            lifecycleScope.launch {
                preferencesRepository.updateApplicationPreferences { prefs ->
                    prefs.copy(firstLaunch = false)
                }
            }

            finish()
        }
    }

    override fun initData() {
    }

    override fun initListener() {
    }
}