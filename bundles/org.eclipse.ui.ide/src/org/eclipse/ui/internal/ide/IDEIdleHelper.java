package org.eclipse.ui.internal.ide;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.application.IWorkbenchConfigurer;

/**
 * The idle helper detects when the system is idle in order to perform garbage 
 * collection in a way that minimizes impact on responsiveness of the UI.
 * The algorithm for determining when to perform a garbage collection
 * is as follows:
 * 
 *  - Don't gc if background jobs are running
 *  - Don't gc if the keyboard or mouse have been active within IDLE_INTERVAL
 *  - Don't gc if there has been a GC within the minimum gc interval (system property PROP_GC_INTERVAL)
 *  - After a gc, don't gc again until (duration * GC_DELAY_MULTIPLIER) has elapsed.
 *    For example, if a GC takes 100ms and the multiplier is 60, don't gc for at least five seconds
 *  - Never gc again if any single gc takes longer than system property PROP_GC_MAX
 */
class IDEIdleHelper {

	/**
	 * The default minimum time between garbage collections.
	 */
	private static final int DEFAULT_GC_INTERVAL = 60000;

	/**
	 * The default maximum duration for a garbage collection, beyond which
	 * the explicit gc mechanism is automatically disabled.
	 */
	private static final int DEFAULT_GC_MAX = 8000;

	/**
	 * The multiple of the last gc duration before we will consider doing
	 * another one.
	 */
	private static final int GC_DELAY_MULTIPLIER = 60;

	/**
	 * The time interval of no keyboard or mouse events after which the system 
	 * is considered idle.
	 */
	private static final int IDLE_INTERVAL = 5000;

	/**
	 * The name of the boolean system property that specifies whether explicit
	 * garbage collection is enabled.
	 */
	private static final String PROP_GC = "ide.gc"; //$NON-NLS-1$

	/**
	 * The name of the integer system property that specifies the minimum time 
	 * interval in milliseconds between garbage collections.
	 */
	private static final String PROP_GC_INTERVAL = "ide.gc.interval"; //$NON-NLS-1$

	/**
	 * The name of the integer system property that specifies the maximum 
	 * duration for a garbage collection. If this duration is ever exceeded, the 
	 * explicit gc mechanism is disabled for the remainder of the session.
	 */
	private static final String PROP_GC_MAX = "ide.gc.max"; //$NON-NLS-1$

	protected IWorkbenchConfigurer configurer;

	private Listener idleListener;

	/**
	 * The last time we garbage collected.
	 */
	private long lastGC = System.currentTimeMillis();

	/**
	 * The maximum gc duration. If this value is exceeded, the
	 * entire explicit gc mechanism is disabled.
	 */
	private int maxGC = DEFAULT_GC_MAX;
	/**
	 * The minimum time interval until the next garbage collection
	 */
	private int minGCInterval = DEFAULT_GC_INTERVAL;

	/**
	 * The time interval until the next garbage collection
	 */
	private int nextGCInterval = DEFAULT_GC_INTERVAL;

	/**
	 * Creates and initializes the idle handler
	 * @param aConfigurer The workbench configurer.
	 */
	IDEIdleHelper(IWorkbenchConfigurer aConfigurer) {
		this.configurer = aConfigurer;
		String enabled = System.getProperty(PROP_GC);
		//gc is turned on by default if property is missing
		if (enabled != null && enabled.equalsIgnoreCase(Boolean.FALSE.toString()))
			return;
		//init gc interval
		Integer prop = Integer.getInteger(PROP_GC_INTERVAL);
		if (prop != null && prop.intValue() >= 0)
			minGCInterval = nextGCInterval = prop.intValue();

		//init max gc interval
		prop = Integer.getInteger(PROP_GC_MAX);
		if (prop != null)
			maxGC = prop.intValue();

		//hook idle handler
		final Display display = configurer.getWorkbench().getDisplay();
		final Runnable handler = new Runnable() {
			public void run() {
				if (!configurer.getWorkbench().isClosing())
					display.timerExec(performGC(), this);
			}
		};
		idleListener = new Listener() {
			public void handleEvent(Event event) {
				display.timerExec(IDLE_INTERVAL, handler);
			}
		};
		display.addFilter(SWT.KeyUp, idleListener);
		display.addFilter(SWT.MouseUp, idleListener);
	}

	/**
	 * The idle handler has detected that the system is idle. Perform
	 * idle processing. Returns the amount of time that should pass before
	 * the next GC.
	 */
	protected int performGC() {
		//don't garbage collect if background jobs are running
		if (!Platform.getJobManager().isIdle())
			return IDLE_INTERVAL;
		final long start = System.currentTimeMillis();
		//don't garbage collect if we have collected within the specific interval
		if ((start - lastGC) < nextGCInterval)
			return nextGCInterval - (int) (start - lastGC);
		System.gc();
		System.runFinalization();
		lastGC = start;
		final int duration = (int) (System.currentTimeMillis() - start);
		if (Policy.DEBUG_GC)
			System.out.println("Explicit GC took: " + duration); //$NON-NLS-1$
		if (duration > maxGC) {
			if (Policy.DEBUG_GC)
				System.out.println("Further explicit GCs disabled due to long GC"); //$NON-NLS-1$
			shutdown();
			return -1;
		}
		//if the gc took a long time, ensure the next gc doesn't happen for awhile
		nextGCInterval = Math.max(minGCInterval, GC_DELAY_MULTIPLIER * duration);
		if (Policy.DEBUG_GC)
			System.out.println("Next GC to run in: " + nextGCInterval); //$NON-NLS-1$
		return nextGCInterval;
	}

	/**
	 * Shuts down the idle helper, removing any installed listeners, etc.
	 */
	void shutdown() {
		if (idleListener == null)
			return;
		Display display = configurer.getWorkbench().getDisplay();
		if (display != null && !display.isDisposed()) {
			display.removeFilter(SWT.KeyUp, idleListener);
			display.removeFilter(SWT.MouseUp, idleListener);
		}
	}
}