package com.activities.videos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import coil.transform.RoundedCornersTransformation
import com.activities.videos.R
import com.activities.videos.databinding.ItemVideoBinding
import com.module.core.base.BaseAdapterRecyclerView
import com.module.core.extensions.formatDurationMillis
import com.module.core.extensions.formatFileSize
import com.modules.core.datastore.models.ApplicationPreferences
import com.modules.core.model.Video
import kotlin.math.max


class VideoAdapter(
    private val context: Context,
    private val applicationPreferences: ApplicationPreferences,
    private val onClickListener: (Video) -> Unit,
    list: MutableList<Video>,
) : BaseAdapterRecyclerView<Video, ItemVideoBinding>() {
    init {
        setDataList(list)
    }

    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): ItemVideoBinding {
        return ItemVideoBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: ItemVideoBinding, item: Video, position: Int) {
        binding.root.setOnClickListener {
            onClickListener(item)
        }

        binding.videoTitle.text = item.displayName
        binding.path.text = item.path

        binding.duration.text = item.duration.formatDurationMillis
        if (!applicationPreferences.showDurationField) {
            binding.duration.visibility = View.GONE
        }

        val screenWidthDp =
            context.resources.displayMetrics.widthPixels / context.resources.displayMetrics.density
        val minWidthDp = 150f
        val calculatedWidthDp = 0.35f * screenWidthDp
        val finalWidthPx =
            max(minWidthDp, calculatedWidthDp) * context.resources.displayMetrics.density

        val layoutParams = binding.cardView.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.width = finalWidthPx.toInt()
        layoutParams.dimensionRatio = "${item.width}:${item.height}"
        binding.cardView.layoutParams = layoutParams

        binding.videoThumbnail.load(item.thumbnailPath) {
            crossfade(true)
            placeholder(R.drawable.artwork_default)
            error(R.drawable.artwork_default)
            val radiusPx = 8f * context.resources.displayMetrics.density
            transformations(RoundedCornersTransformation(radiusPx))
        }

        if (applicationPreferences.showSizeField) {
            binding.sizeChip.text = item.size.formatFileSize
            binding.sizeChip.visibility = View.VISIBLE
        } else {
            binding.sizeChip.visibility = View.GONE
        }

        if (applicationPreferences.showResolutionField && item.height > 0) {
            binding.resolutionChip.text = context.getString(R.string.resolution, item.height)
            binding.resolutionChip.visibility = View.VISIBLE
        } else {
            binding.resolutionChip.visibility = View.GONE
        }
    }
}