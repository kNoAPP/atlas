package com.knoban.atlas.scheduler;

import com.knoban.atlas.callbacks.Callback;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

/**
 * Allows you to create a task to run at a specific time on the main game thread-- regardless of the current TPS.
 * Utilizes {@code System.currentTimeMillis()}
 *
 * @author Alden Bansemer (kNoAPP)
 */
public class ClockedTask implements Comparable<ClockedTask> {

    private final Long runtimeMillis, leniency;
    private final Callback task;
    private volatile boolean async, cancelled, ran;

    /**
     * Creates a ClockedTask that will run as close to the desired time as possible. There is a default 20ms of
     * leniency that the task may run within. Use the other constructor to change this.
     * @param date The time to run the task at
     * @param task The task
     */
    public ClockedTask(Calendar date, @NotNull Callback task) {
        this(date.getTimeInMillis(), 20, false, task);
    }

    /**
     * Creates a ClockedTask that will run as close to the desired time as possible. There is a default 20ms of
     * leniency that the task may run within. Use the other constructor to change this.
     * @param date The time to run the task at
     * @param async Should the task run asynchronous to the main game thread?
     * @param task The task
     */
    public ClockedTask(Calendar date, boolean async, @NotNull Callback task) {
        this(date.getTimeInMillis(), 20, async, task);
    }

    /**
     * Creates a ClockedTask that will run as close to the desired time as possible. There is a default 20ms of
     * leniency that the task may run within. Use the other constructor to change this.
     * @param date The time to run the task at
     * @param task The task
     */
    public ClockedTask(Date date, @NotNull Callback task) {
        this(date.getTime(), 20, false, task);
    }

    /**
     * Creates a ClockedTask that will run as close to the desired time as possible. There is a default 20ms of
     * leniency that the task may run within. Use the other constructor to change this.
     * @param date The time to run the task at
     * @param async Should the task run asynchronous to the main game thread?
     * @param task The task
     */
    public ClockedTask(Date date, boolean async, @NotNull Callback task) {
        this(date.getTime(), 20, async, task);
    }

    /**
     * Creates a ClockedTask that will run as close to the desired time as possible. There is a default 20ms of
     * leniency that the task may run within. Use the other constructor to change this.
     * By default, the task will run on the main game thread on execution.
     * @param runtimeMillis The time to run the task at
     * @param task The task
     */
    public ClockedTask(long runtimeMillis, @NotNull Callback task) {
        this(runtimeMillis, 20, false, task);
    }

    /**
     * Creates a ClockedTask that will run as close to the desired time as possible. There is a default 20ms of
     * leniency that the task may run within. Use the other constructor to change this.
     * By default, the task will run on the main game thread on execution.
     * @param runtimeMillis The time to run the task at
     * @param async Should the task run asynchronous to the main game thread?
     * @param task The task
     */
    public ClockedTask(long runtimeMillis, boolean async, @NotNull Callback task) {
        this(runtimeMillis, 20, async, task);
    }

    /**
     * Creates a ClockedTask that will run as close to the desired time as possible. Leniency allows the task to run
     * early within a span of time. Since {@code Thread.sleep()} isn't always precise, a small amount of leniency
     * is recommended (like default 20ms).
     * @param runtimeMillis The time to run the task at
     * @param leniency The range of time this task may run early in if the opportunity presents itself
     * @param async Should the task run asynchronous to the main game thread?
     * @param task The task
     */
    public ClockedTask(long runtimeMillis, long leniency, boolean async, @NotNull Callback task) {
        this.runtimeMillis = runtimeMillis;
        this.leniency = leniency;
        this.async = async;
        this.task = task;
        this.cancelled = false;
        this.ran = false;
    }

    /**
     * @return True, if the task has been cancelled. Cancelled tasks do not run when their timer expires.
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Marks this task's cancel state. A task that is cancelled and hasn't yet run will not be run.
     * The cancelled variable is checked both on the Clocked thread and on the event thread. If you cancel it, this
     * will run thread-safe and cancel.
     * @param cancelled True, if the task shouldn't be run.
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * @return True, if this task has already run.
     */
    public boolean wasRun() {
        return ran;
    }

    /**
     * For use by the {@link ClockedTaskManager} only!
     * @param ran True, if this task was ran. False, if not.
     */
    protected void setWasRun(boolean ran) {
        this.ran = ran;
    }

    /**
     * @return The epoch time that this task will run at.
     */
    public long getRunTimeMillis() {
        return runtimeMillis;
    }

    /**
     * @return The number of milliseconds until the task will execute. May be negative if the
     * task's execution time has passed.
     */
    public long getMillisUntilExecution() {
        return runtimeMillis - System.currentTimeMillis();
    }

    /**
     * @return True, if the time the task should execute at has passed.
     */
    public boolean isExpired() {
        return getMillisUntilExecution() < 0;
    }

    /**
     * @return The leniency (in milliseconds) that this task may run early if provided the opportunity.
     * By default, this number is 20ms.
     */
    public Long getLeniency() {
        return leniency;
    }

    /**
     * Default is false.
     * @return True, if this task will be run on an asynchronous thread when the timer expires.
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Sets whether or not to run the task asynchronously to the main game thread.
     * @param async True to run on an async thread. False to run on the main game thread.
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

    /**
     * @return The task set to run
     */
    @NotNull
    public Callback getTask() {
        return task;
    }

    /**
     * Compares this task's runtimeMillis to another's.
     */
    @Override
    public int compareTo(@NotNull ClockedTask o) {
        return runtimeMillis.compareTo(o.runtimeMillis);
    }
}
