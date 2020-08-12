package com.knoban.atlas.scheduler;

import org.bukkit.plugin.java.JavaPlugin;
import org.godcomplex.core.Core;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * A scheduler to run tasks as close to a {@code System.currentTimeMillis()} as possible.
 *
 * @author Alden Bansemer (kNoAPP)
 */
public final class ClockedTaskManager {

    private static final ClockedTaskManager manager = new ClockedTaskManager(Core.getCore());

    private final JavaPlugin plugin;
    private volatile boolean valid;
    private Thread processor;

    private final PriorityBlockingQueue<ClockedTask> queue = new PriorityBlockingQueue<>();

    /**
     * Creates an instance of ClockedTaskManager. This instance will schedule and run tasks as close as possible
     * to a desired time.
     * @param plugin The JavaPlugin owning this instance
     */
    private ClockedTaskManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.valid = true;
        this.processor = new Thread(this::handleQueue);
        processor.start();
    }

    /**
     * Add a scheduled task to be run. If a passed task should have been ran in the past,
     * it will instead run immediately.
     * @param task The task that should be run
     */
    public synchronized void addTask(@NotNull ClockedTask task) {
        queue.offer(task);

        if(!processor.isAlive()) {
            processor = new Thread(this::handleQueue);
            processor.start();
        } else if(queue.peek() == task) { // Task is front of the queue! Need to alert the thread to switch context.
            processor.interrupt();
        }
    }

    /**
     * Helper method to process, schedule, and run the task queue on the main thread.
     */
    private void handleQueue() {
        while(!queue.isEmpty()) {
            ClockedTask task = queue.poll();

            // https://stackoverflow.com/questions/18736681/how-accurate-is-thread-sleep
            long diff;
            while((diff = task.getMillisUntilExecution()) > task.getLeniency()) {
                try {
                    Thread.sleep(diff);
                } catch(InterruptedException e) { // Thread interrupted, probably means another sooner task was added.
                    if(!valid)
                        return;

                    queue.offer(task); // Put current task back into the queue
                    task = queue.peek(); // Get the new (up-front) task
                }
            }

            // Run task if not cancelled. Two checks here to save CPU time while maintaining thread-safe behaviour
            if(!task.isCancelled()) {
                final ClockedTask runTask = task;
                if(task.isAsync()) {
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        boolean wasRun = !runTask.isCancelled();
                        runTask.setWasRun(wasRun);
                        if(wasRun)
                            runTask.getTask().call();
                    });
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        boolean wasRun = !runTask.isCancelled();
                        runTask.setWasRun(wasRun);
                        if(wasRun)
                            runTask.getTask().call();
                    });
                }
            }
        }
    }

    /**
     * Call this to clear the task queue and safely mark all tasks as expired. You may still continue to add
     * tasks despite calling this. Recommended to call this once while shutting the server down to avoid exceptions
     * for planned tasks.
     */
    public synchronized void safeShutdown() {
        valid = false;
        queue.clear();
        processor.interrupt();

        try {
            processor.join(5000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static ClockedTaskManager getManager() {
        return manager;
    }
}
