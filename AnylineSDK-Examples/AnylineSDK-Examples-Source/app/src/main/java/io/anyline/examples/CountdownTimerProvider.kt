package io.anyline.examples

import android.os.CountDownTimer

interface CountdownTimerProvider {

    fun provideTimer(
            millisInFuture: Long,
            countdownInterval: Long,
            onTickAction: (Long) -> Unit,
            onFinishAction: () -> Unit
    ): CountDownTimer
}
