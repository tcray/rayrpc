package com.tcray.rayrpc.core.handler;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author lirui
 */
@Slf4j
public class TimerWorkerHandler extends Thread {

    private volatile boolean toStop = false;

    private List<Runnable> timerJobList = new CopyOnWriteArrayList<Runnable>();

    public TimerWorkerHandler() {
        super("TimerAndEventDaemonThread");
        setDaemon(true);
    }

    public void close() {
        toStop = true;
    }

    @Override
    public void run() {
        while (true) {
            if (toStop) {
                return;
            }

            if (isInterrupted()) {
                return;
            }

            for (Runnable job : timerJobList) {
                try {
                    job.run();
                } catch (Throwable ex) {
                    log.error(ex.getMessage(), ex);
                    continue;
                }
            }
        }
    }

    public TimerWorkerHandler addTimerJob(Runnable timerJob) {
        this.timerJobList.add(timerJob);
        return this;
    }

    public TimerWorkerHandler clearTimerJobList() {
        timerJobList.clear();
        return this;
    }

}
