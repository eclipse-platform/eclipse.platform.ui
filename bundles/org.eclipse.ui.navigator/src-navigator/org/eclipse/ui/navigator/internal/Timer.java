/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator.internal;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.EventListenerList;



public class Timer {

	/**
	 * DoPostEvent is a runnable class that fires actionEvents to the listeners on the
	 * EventDispatchThread, via invokeLater.
	 * 
	 * @see #post
	 */
	class DoPostEvent implements Runnable {
		public void run() {
			if (eventQueued) {
				fireActionPerformed(new ActionEvent(getSource(), 0, null));
				cancelEvent();
			}
		}

		Timer getTimer() {
			return Timer.this;
		}
	}

	protected ListenerList listenerList = new ListenerList();
	boolean eventQueued = false;
	int initialDelay, delay;
	boolean repeats = true, coalesce = true;
	private Runnable doPostEvent;
	// These fields are maintained by TimerQueue.
	// eventQueued can also be reset by the TimerQueue, but will only ever
	// happen in applet case when TimerQueues thread is destroyed.
	long expirationTime;
	Timer nextTimer;
	boolean running;

	/**
	 * Creates a Timer that will notify its listeners every <i>delay </i> milliseconds.
	 * 
	 * @param delay
	 *            The number of milliseconds between listener notification
	 * @param listener
	 *            An initial listener
	 * @see #setInitialDelay
	 * @see #setRepeats
	 */
	public Timer(int delay, ActionListener listener) {
		super();
		this.delay = delay;
		this.initialDelay = delay;

		doPostEvent = new DoPostEvent();

		if (listener != null) {
			addActionListener(listener);
		}
	}

	/**
	 * Adds an actionListener to the Timer
	 */
	public void addActionListener(ActionListener listener) {
		listenerList.add(listener);
	}

	synchronized void cancelEvent() {
		eventQueued = false;
	}

	/**
	 * Notify all listeners that have registered interest for notification on this event type. The
	 * event instance is lazily created using the parameters passed into the fire method.
	 * 
	 * @see EventListenerList
	 */
	protected void fireActionPerformed(ActionEvent e) {
		// Guaranteed to return a non-null array
		ActionListener[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 1; i >= 0; i -= 1) {
			listeners[i].actionPerformed(e);
		}
	}

	/**
	 * Returns the Timer's delay.
	 * 
	 * @see #setDelay
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * Returns the Timer's initial delay.
	 * 
	 * @see #setDelay
	 */
	public int getInitialDelay() {
		return initialDelay;
	}

	/**
	 * Returns <b>true </b> if the Timer coalesces multiple pending <b>performCommand() </b>
	 * messages.
	 * 
	 * @see #setCoalesce
	 */
	public boolean isCoalesce() {
		return coalesce;
	}

	/**
	 * Returns <b>true </b> if the Timer will send a <b>actionPerformed() </b> message to its
	 * listeners multiple times.
	 * 
	 * @see #setRepeats
	 */
	public boolean isRepeats() {
		return repeats;
	}

	/**
	 * Returns <b>true </b> if the Timer is running.
	 * 
	 * @see #start
	 */
	public boolean isRunning() {
		return timerQueue().containsTimer(this);
	}

	synchronized void post() {
		if (!eventQueued) {
			eventQueued = true;
			org.eclipse.swt.widgets.Display.getDefault().asyncExec(doPostEvent);
		}
	}

	/**
	 * Removes an ActionListener from the Timer.
	 */
	public void removeActionListener(ActionListener listener) {
		listenerList.remove(listener);
	}

	/**
	 * Restarts a Timer, canceling any pending firings, and causing it to fire with its initial
	 * dely.
	 */
	public void restart() {
		stop();
		start();
	}

	/**
	 * Sets whether the Timer coalesces multiple pending ActionEvent firings. A busy application may
	 * not be able to keep up with a Timer's message generation, causing multiple
	 * <b>actionPerformed() </b> message sends to be queued. When processed, the application sends
	 * these messages one after the other, causing the Timer's listeners to receive a sequence of
	 * <b>actionPerformed() </b> messages with no delay between them. Coalescing avoids this
	 * situation by reducing multiple pending messages to a single message send. Timers coalesce
	 * their message sends by default.
	 */
	public void setCoalesce(boolean flag) {
		coalesce = flag;
	}

	/**
	 * Sets the Timer's delay, the number of milliseconds between successive <b>actionPerfomed()
	 * </b> messages to its listeners
	 * 
	 * @see #setInitialDelay
	 */
	public void setDelay(int delay) {
		if (delay < 0) {
			/*
			 * String msg = WTPCommonUIResourceHandler.getString("Timer_UI_0", new Object[]
			 * {Integer.toString(delay)}); //$NON-NLS-1$
			 */throw new IllegalArgumentException(/* msg */"delay < 0"); //$NON-NLS-1$
		}
		this.delay = delay;
	}

	/**
	 * Sets the Timer's initial delay. This will be used for the first "ringing" of the Timer only.
	 * Subsequent ringings will be spaced using the delay property.
	 * 
	 * @see #setDelay
	 */
	public void setInitialDelay(int initialDelay) {
		if (initialDelay < 0) {
			/*
			 * String msg = WTPCommonUIResourceHandler.getString("Timer_UI_1", new Object[]
			 * {Integer.toString(initialDelay)}); //$NON-NLS-1$
			 */throw new IllegalArgumentException(/* msg */"initialDelay < 0"); //$NON-NLS-1$
		}
		this.initialDelay = initialDelay;
	}

	/**
	 * If <b>flag </b> is <b>false </b>, instructs the Timer to send <b>actionPerformed() </b> to
	 * its listeners only once, and then stop.
	 */
	public void setRepeats(boolean flag) {
		repeats = flag;
	}

	/**
	 * Starts the Timer, causing it to send <b>actionPerformed() </b> messages to its listeners.
	 * 
	 * @see #stop
	 */
	public void start() {
		timerQueue().addTimer(this, System.currentTimeMillis() + getInitialDelay());
	}

	/**
	 * Stops a Timer, causing it to stop sending <b>actionPerformed() </b> messages to its Target.
	 * 
	 * @see #start
	 */
	public void stop() {
		timerQueue().removeTimer(this);
		cancelEvent();
	}

	/**
	 * Returns the timer queue.
	 */
	TimerQueue timerQueue() {
		return TimerQueue.singleton();
	}

	/**
	 * Return the source for the ActionEvent that is fired.
	 * 
	 * @return
	 */
	protected Object getSource() {
		return this;
	}
}