/*******************************************************************************
 * Copyright (c) 2020 Remain BV.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wim Jongman - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.services;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.osgi.service.log.LogLevel;

/**
 * This class is used to prevent superfluous logging of the same message.
 *
 * @since 3.5
 *
 */
public class LogThrottle {

	private LinkedBlockingQueue<String> fThrottleQueue;
	private HashMap<String, MutableInt> fActiveMessages = new HashMap<>();
	private String fThrottleMessage;
	private int fThrottleValue;

	/**
	 * Creates a log throttle with a capacity of {@code queueSize} entries and a
	 * throttle value of {@code throttelValue}.
	 *
	 * @param queueSize     the maximum number of entries to track.
	 * @param throttleValue the amount of times a message may be logged before it is
	 *                      throttled.
	 */
	public LogThrottle(int queueSize, int throttleValue) {
		fThrottleValue = throttleValue;
		fThrottleQueue = new LinkedBlockingQueue<>(queueSize);
		fThrottleMessage = MessageFormat.format(
				"The previous message has been throttled.\nThe previous message has been logged {0} times and will not be logged again.", //$NON-NLS-1$
				fThrottleValue);
	}

	/**
	 * Logs a message with flood protection.
	 *
	 * @param logLevel any ordinal of @link(LogLevel)
	 * @param message  the message to log, may not be null.
	 * @param e        the exception to log or {@code null}
	 * @return true if the message was logged, false otherwise.
	 * @see LogLevel
	 */
	public boolean log(int logLevel, String message, Throwable e) {
		int store = store(message);
		if (store <= fThrottleValue) {
			Activator.log(logLevel, message, e);
		}
		if (store == fThrottleValue) {
			Activator.log(LogLevel.WARN.ordinal(), fThrottleMessage);
		}
		return store <= fThrottleValue;
	}

	/**
	 * Sets the new throttle value which must be greater than zero.
	 *
	 * @param throttleValue the new throttle value
	 * @return this instance
	 */
	public LogThrottle setThrottle(int throttleValue) {
		if (throttleValue > 0) {
			fThrottleValue = throttleValue;
		}
		return this;
	}

	/**
	 * Stores the passed message into the queue of logged messages. If the entry was
	 * already logged then it is moved to the tail of the queue. If the queue is
	 * full, the head is pushed out and can be logged again.
	 *
	 * @param messageText the message to log
	 * @return the number of times this message has been requested for log.
	 */
	protected synchronized int store(String messageText) {
		MutableInt counter = check(messageText);
		counter.increment();
		if (counter.fCount >= fThrottleValue) {
			fThrottleQueue.remove(messageText); // remove to place back in tail
			while (!fThrottleQueue.offer(messageText)) {
				String message = fThrottleQueue.poll(); // queue full, remove head entry
				fActiveMessages.remove(message); // and also purge from active messages
			}
		}
		return counter.fCount;
	}

	private synchronized MutableInt check(String messageText) {
		MutableInt counter = fActiveMessages.get(messageText);
		if (counter == null) {
			counter = new MutableInt();
			fActiveMessages.put(messageText, counter);
		}
		return counter;
	}

	private static class MutableInt {
		private int fCount;

		public void increment() {
			fCount++;
		}
	}

}
