/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.runtime;

import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;

/**
 * Performs string sharing passes on all string pool participants registered
 * with the platform.
 */
public class StringPoolJob extends Job {
	private static final long INITIAL_DELAY = 10000;//five seconds
	private static final long RESCHEDULE_DELAY = 300000;//five minutes
	private long lastDuration;
	/**
	 * Stores all registered string pool participants, along with the scheduling
	 * rule required when running it.
	 */
	private Map participants = Collections.synchronizedMap(new HashMap(10));

	public StringPoolJob() {
		super(Policy.bind("meta.stringJobName")); //$NON-NLS-1$
		setSystem(true);
		setPriority(DECORATE);
	}

	/**
	 * Registers a new string pool participant.
	 */
	public void addStringPoolParticipant(IStringPoolParticipant participant, ISchedulingRule rule) {
		participants.put(participant, rule);
		if (sleep())
			wakeUp(INITIAL_DELAY);
	}

	public void removeStringPoolParticipant(IStringPoolParticipant participant) {
		participants.remove(participant);
	}

	/* (non-Javadoc)
	 * Method declared on Job
	 */
	protected IStatus run(IProgressMonitor monitor) {
		//copy current participants to handle concurrent additions and removals to map
		Map.Entry[] entries = (Map.Entry[]) participants.entrySet().toArray(new Map.Entry[0]);
		ISchedulingRule[] rules = new ISchedulingRule[entries.length];
		IStringPoolParticipant[] toRun = new IStringPoolParticipant[entries.length];
		for (int i = 0; i < toRun.length; i++) {
			toRun[i] = (IStringPoolParticipant) entries[i].getKey();
			rules[i] = (ISchedulingRule) entries[i].getValue();
		}
		final ISchedulingRule rule = MultiRule.combine(rules);
		long start = -1;
		int savings = 0;
		try {
			Platform.getJobManager().beginRule(rule, monitor);
			start = System.currentTimeMillis();
			savings = shareStrings(toRun, monitor);
		} finally {
			Platform.getJobManager().endRule(rule);
		}
		if (start > 0) {
			lastDuration = System.currentTimeMillis() - start;
			if (InternalPlatform.DEBUG_STRINGS)
				Policy.debug("String sharing saved " + savings + " bytes in: " + lastDuration); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		//throttle frequency if it takes too long
		long scheduleDelay = Math.max(RESCHEDULE_DELAY, lastDuration*100);
		if (InternalPlatform.DEBUG_STRINGS)
			Policy.debug("Rescheduling string sharing job in: " + scheduleDelay); //$NON-NLS-1$
		schedule(scheduleDelay);
		return Status.OK_STATUS;
	}

	private int shareStrings(IStringPoolParticipant[] toRun, IProgressMonitor monitor) {
		final StringPool pool = new StringPool();
		for (int i = 0; i < toRun.length; i++) {
			if (monitor.isCanceled())
				break;
			final IStringPoolParticipant current = toRun[i];
			Platform.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					//exceptions are already logged, so nothing to do
				}

				public void run() {
					current.shareStrings(pool);
				}
			});
		}
		return pool.getSavedStringCount();
	}
}