package com.ruizburga.denunciasapp.ui.components

import android.animation.ValueAnimator

/** Uses short transitions only when the user has not disabled system animations. */
fun motionDurationMillis(preferredMillis: Int): Int =
    if (ValueAnimator.areAnimatorsEnabled()) preferredMillis else 0
