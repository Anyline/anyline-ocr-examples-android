package io.anyline.examples

import android.os.CountDownTimer
import io.anyline.examples.CountdownTimerProvider

class CountdownTimerProviderImpl : CountdownTimerProvider {

    override fun provideTimer(
            millisInFuture: Long,
            countdownInterval: Long,
            onTickAction: (Long) -> Unit,
            onFinishAction: () -> Unit
    ): CountDownTimer {
        return object : CountDownTimer(millisInFuture, countdownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                onTickAction(millisUntilFinished)
            }

            override fun onFinish() {
                onFinishAction()
            }
        }
    }
}
