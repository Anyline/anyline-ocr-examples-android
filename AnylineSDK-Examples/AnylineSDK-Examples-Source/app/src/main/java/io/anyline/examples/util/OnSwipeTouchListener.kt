package io.anyline.examples.util

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

abstract class OnSwipeTouchListener : View.OnTouchListener {

    private val gestureDetector = GestureDetector(GestureListener())

    fun onTouch(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 50
        private val SWIPE_VELOCITY_THRESHOLD = 50

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            onTouch(e)
            return true
        }


        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x

            if (isHorizontalFling(diffX, diffY, velocityX)) {
                if (diffX > 0) {
                    onSwipeRight()
                } else {
                    onSwipeLeft()
                }
            }
            return false
        }

        fun isHorizontalFling(diffX: Float, diffY: Float, velocityX: Float): Boolean {
            return (Math.abs(diffX) > Math.abs(diffY)) &&
                    (Math.abs(diffX) > SWIPE_THRESHOLD) &&
                    (Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD)
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    abstract fun onSwipeRight()

    abstract fun onSwipeLeft()
}