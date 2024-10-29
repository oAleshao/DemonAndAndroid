package itstep.learning.demonandandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextParams;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OnSwipeListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    public OnSwipeListener(Context context) {
        this.gestureDetector = new GestureDetector(context, new SwipeGestureListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void onSwipeBottom() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeRight() {
    }

    public void onSwipeTop() {
    }

    private final class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private final static int minVelocity = 150;
        private final static int minDistance = 100;
        private final static double minRation = 1.0 / 2.0;

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true; // true означає, що подія оброблена даним детектором.
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            boolean isHandled = false;
            if(e1 == null ) return false;
            float deltaX = e2.getX() - e1.getX(); // e1 - точка початку жесту (х, у)
            float deltaY = e2.getY() - e1.getY(); // е2 - точка кінця жесту (х, у)
            float distanceX = Math.abs(deltaX);
            float distanceY = Math.abs(deltaY);

            if(distanceX * minRation > distanceY && distanceX >= minDistance){ // горизонтальний свайп
                if(Math.abs(velocityX) >= minVelocity){ // аналізуємо лише швидкість Х
                    if(deltaX > 0){
                        onSwipeRight();
                    }
                    else{
                        onSwipeLeft();
                    }
                    isHandled = true;
                }
            }
            else if(distanceY * minRation > distanceX && distanceY >= minDistance ) { // вертикальний свайп
                if(Math.abs(velocityY) >= minVelocity){// аналізуємо лише швидкість Y
                    if(deltaY > 0){
                        onSwipeBottom();
                    }
                    else {
                        onSwipeTop();
                    }
                    isHandled = true;
                }
            }


            return isHandled;
        }
    }


}

/*
 * Swipes.
 * GestureDetector - детектор жестів, призначений для розпізнавання жестів та запуск
 * обробників (Listener) в залежносты від визначеного жесту.
 * Детектор "спостерігає" за певним конекстом (представлянням, областю екрану)
 *
 * Визначення свайпів базується на аналізі події "onFling" - жест, що складається з
 * торкання, проведення та відпускання екрану пристрою, а також нівелювання події "onDown",
 * яка може призвести до синтезу "Тар" або "Click".
 *
 * В основі візначення свайпів - аналіз швидкості та відстані жесту проведення,
 * а також можливості віднесення його до одного з напрямів (у даному прикладі - їх 4)
 *
 * */
