/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal;


/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2 
 *
 */
class TimerQueue implements Runnable {
	private static TimerQueue singleton;
	Timer firstTimer;
	boolean running;
	private static final Object classLock = new Object();

	/**
	 * Constructor for TimerQueue.
	 */
	public TimerQueue() {
		super();

		// Now start the TimerQueue thread.
		start();
	}

	synchronized void addTimer(Timer timer, long expirationTime) {
		Timer previousTimer;
		Timer nextTimer;

		// If the Timer is already in the queue, then ignore the add.
		if (timer.running) {
			return;
		}

		previousTimer = null;
		nextTimer = firstTimer;

		// Insert the Timer into the linked list in the order they will
		// expire. If two timers expire at the same time, put the newer entry
		// later so they expire in the order they came in.

		while (nextTimer != null) {
			if (nextTimer.expirationTime > expirationTime)
				break;

			previousTimer = nextTimer;
			nextTimer = nextTimer.nextTimer;
		}

		if (previousTimer == null) {
			firstTimer = timer;
		} else {
			previousTimer.nextTimer = timer;
		}

		timer.expirationTime = expirationTime;
		timer.nextTimer = nextTimer;
		timer.running = true;
		notify();
	}

	synchronized boolean containsTimer(Timer timer) {
		return timer.running;
	}

	/**
	 * If there are a ton of timers, this method may never return. It loops checking to see if the
	 * head of the Timer list has expired. If it has, it posts the Timer and reschedules it if
	 * necessary.
	 */
	synchronized long postExpiredTimers() {
		Timer timer;
		long currentTime;
		long timeToWait;

		// The timeToWait we return should never be negative and only be zero
		// when we have no Timers to wait for.

		do {
			timer = firstTimer;
			if (timer == null)
				return 0;

			currentTime = System.currentTimeMillis();
			timeToWait = timer.expirationTime - currentTime;

			if (timeToWait <= 0) {
				try {
					timer.post(); // have timer post an event
				} catch (SecurityException e) {
				}

				// Remove the timer from the queue
				removeTimer(timer);

				// This tries to keep the interval uniform at
				// the cost of drift.
				if (timer.isRepeats()) {
					addTimer(timer, currentTime + timer.getDelay());
				}
			}

			// Allow other threads to call addTimer() and removeTimer()
			// even when we are posting Timers like mad. Since the wait()
			// releases the lock, be sure not to maintain any state
			// between iterations of the loop.

			try {
				wait(1);
			} catch (InterruptedException e) {
			}
		} while (timeToWait <= 0);

		return timeToWait;
	}

	synchronized void removeTimer(Timer timer) {
		Timer previousTimer;
		Timer nextTimer;
		boolean found;

		if (!timer.running)
			return;

		previousTimer = null;
		nextTimer = firstTimer;
		found = false;

		while (nextTimer != null) {
			if (nextTimer == timer) {
				found = true;
				break;
			}

			previousTimer = nextTimer;
			nextTimer = nextTimer.nextTimer;
		}

		if (!found)
			return;

		if (previousTimer == null) {
			firstTimer = timer.nextTimer;
		} else {
			previousTimer.nextTimer = timer.nextTimer;
		}

		timer.expirationTime = 0;
		timer.nextTimer = null;
		timer.running = false;
	}

	public synchronized void run() {
		long timeToWait;

		try {
			while (running) {
				timeToWait = postExpiredTimers();
				try {
					wait(timeToWait);
				} catch (InterruptedException e) {
				}
			}
		} catch (ThreadDeath td) {
			running = false;
			// Mark all the timers we contain as not being queued.
			Timer timer = firstTimer;
			while (timer != null) {
				timer.eventQueued = false;
				timer = timer.nextTimer;
			}
			synchronized (this) {
				if (!this.running)
					start();
			}
			throw td;
		}
	}

	public static TimerQueue singleton() {
		if (singleton == null)
			synchronized (classLock) {
				singleton = new TimerQueue();
			}
		return singleton;
	}

	synchronized void start() {
		if (running) {
			// TODO HANDLE STRING
			throw new RuntimeException(/* WTPCommonUIResourceHandler.getString("TimerQueue_ERROR_0") */"Ack!"); //$NON-NLS-1$
		}
		Thread timerThread = new Thread(this, "TimerQueue");//$NON-NLS-1$
		try {
			timerThread.setDaemon(true);
		} catch (SecurityException e) {
		}
		timerThread.start();
		running = true;
	}

	synchronized void stop() {
		running = false;
		notify();
	}

	public synchronized String toString() {
		StringBuffer buf;
		Timer nextTimer;

		buf = new StringBuffer();
		buf.append("TimerQueue (");//$NON-NLS-1$

		nextTimer = firstTimer;
		while (nextTimer != null) {
			buf.append(nextTimer.toString());

			nextTimer = nextTimer.nextTimer;
			if (nextTimer != null)
				buf.append(", ");//$NON-NLS-1$
		}

		buf.append(")");//$NON-NLS-1$
		return buf.toString();
	}
}
