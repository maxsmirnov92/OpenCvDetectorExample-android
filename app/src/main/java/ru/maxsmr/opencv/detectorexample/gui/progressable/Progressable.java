package ru.maxsmr.opencv.detectorexample.gui.progressable;

import android.support.annotation.MainThread;

public interface Progressable {

    Progressable STUB = new Progressable() {
        public void onStart() {
        }

        public void onStop() {
        }
    };


    @MainThread
    void onStart();

    @MainThread
    void onStop();

}