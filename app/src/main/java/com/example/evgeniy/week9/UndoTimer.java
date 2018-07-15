package com.example.evgeniy.week9;

import java.util.Timer;
import java.util.TimerTask;

public class UndoTimer {

    private final UndoManager undoManager;

    private Timer timer;

    UndoTimer(UndoManager undoManager){
        this.undoManager = undoManager;
    }

    public void setTimer(long delay){
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        MyTimerTask timerTask = new MyTimerTask();
        timer.schedule(timerTask, delay);
    }

    public void stopTimer(){
        if (timer != null) {
            timer.cancel();
        }
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            undoManager.convertTempToScope();
        }
    }
}
