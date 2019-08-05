package com.ivianuu.compose.common

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View

fun VerticalChangeHandler(duration: Long = AnimatorChangeHandler.NO_DURATION): AnimatorChangeHandler {
    return AnimatorChangeHandler(duration) { changeData ->
        val (_, from, to, isPush) = changeData

        val animator = AnimatorSet()
        val viewAnimators = mutableListOf<Animator>()

        if (isPush && to != null) {
            viewAnimators.add(
                ObjectAnimator.ofFloat(
                    to,
                    View.TRANSLATION_Y,
                    to.height.toFloat(),
                    0f
                )
            )
        } else if (!isPush && from != null) {
            viewAnimators.add(
                ObjectAnimator.ofFloat(
                    from,
                    View.TRANSLATION_Y,
                    from.height.toFloat()
                )
            )
        }

        animator.playTogether(viewAnimators)
        return@AnimatorChangeHandler animator
    }
}