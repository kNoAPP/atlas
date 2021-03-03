package com.knoban.atlas.utils;

import com.knoban.atlas.callbacks.Callback;
import com.knoban.atlas.scheduler.ClockedTask;
import com.knoban.atlas.scheduler.ClockedTaskManager;

/**
 * @author Alden Bansemer (kNoAPP)
 */
public class Cooldown {

	private ClockedTask clockedTask;
	private long total, start, finish;
	
	public Cooldown(long millis) {
		this.total = millis;
		this.start = System.currentTimeMillis();
		this.finish = start + millis;
	}

	public void setCompletionTask(Callback task) {
		cancelCompletionTask();

		clockedTask = new ClockedTask(finish, 0, false, task);
		ClockedTaskManager.getManager().addTask(clockedTask);
	}

	public void cancelCompletionTask() {
		if(clockedTask != null)
			clockedTask.setCancelled(true);
	}
	
	public long getTotal() {
		return total;
	}
	
	public long getStart() {
		return start;
	}
	
	public long getFinish() {
		return finish;
	}
	
	public float getPercentCompleted() {
		return (float)getRemainingTime()/(float)total;
	}
	
	public boolean isFinished() {
		return System.currentTimeMillis() >= finish;
	}
	
	public long getRemainingTime() {
		return finish - System.currentTimeMillis();
	}
	
	public String toTimestampString() {
		return Tools.millisToDHMS(getRemainingTime());
	}

	public String toPercentRemainingString() {
		return String.format("%.2f", getPercentCompleted());
	}
	
	public void add(long millis) {
		finish += millis;
		total += millis;

		if(clockedTask != null) {
			clockedTask.setCancelled(true);
			clockedTask = new ClockedTask(finish, clockedTask.getTask());
			ClockedTaskManager.getManager().addTask(clockedTask);
		}
	}
}
