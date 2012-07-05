package de.splashfish.flinky;

import java.util.Timer;
import java.util.TimerTask;

public class DelayTask {
	
	private static final Timer t = new Timer();
	
	public static void invoke(TimerTask task, long delay) {
		t.schedule(task, delay);
	}
	
}
