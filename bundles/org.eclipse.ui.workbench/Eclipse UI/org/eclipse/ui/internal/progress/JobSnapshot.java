/*******************************************************************************
 * Copyright (c) 2022 Joerg Kubitz and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Joerg Kubitz - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.jobs.Job;

/**
 * A immutable, comparable Snapshot of a JobTreeElement
 */
public class JobSnapshot implements Comparable<JobSnapshot> {
	/** for information only - not to be used for comparison as it is mutable */
	private final JobTreeElement reference;
	/** for information only - not to be used for comparison */
	private final int index;

	private final int hashCode;
	private final boolean isUser;
	private final boolean isBlocked;
	private final int priority;
	private final String name;
	private final boolean isCanceled;
	private final int state;
	private final String displayString;

	public JobSnapshot(JobTreeElement reference, int index) {
		this.reference = reference;
		this.index = index;
		this.hashCode = reference.hashCode();
		this.displayString = reference.getDisplayString();

		JobInfo jobInfo = (reference instanceof JobInfo) ? (JobInfo) reference : null;
		this.isBlocked = jobInfo == null ? false : jobInfo.isBlocked();
		this.isCanceled = jobInfo == null ? false : jobInfo.isCanceled();

		Job job = jobInfo == null ? null : jobInfo.getJob();
		this.isUser = job == null ? false : job.isUser();
		this.priority = job == null ? 0 : job.getPriority();
		this.name = job == null ? "" : job.getName(); //$NON-NLS-1$
		this.state = job == null ? 0 : job.getState();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JobSnapshot) {
			return reference.equals(((JobSnapshot) obj).reference);
		}
		return false;
	}

	@Override
	public int compareTo(JobSnapshot other) {
		boolean thisCanceled = isCanceled();
		boolean anotherCanceled = other.isCanceled();
		if (thisCanceled && !anotherCanceled) {
			// If the receiver is cancelled then it is lowest priority
			return 1;
		} else if (!thisCanceled && anotherCanceled) {
			return -1;
		}

		// if equal job state, compare other job attributes
		if (getState() != other.getState()) {
			// ordering by job states, Job.RUNNING should be ordered first
			return Integer.compare(other.getState(), getState());
		}
		// User jobs have top priority
		if (isUser()) {
			if (!other.isUser()) {
				return -1;
			}
		} else if (other.isUser()) {
			return 1;
		}

		// Show the blocked ones last.
		if (isBlocked()) {
			if (!other.isBlocked()) {
				return 1;
			}
		} else if (other.isBlocked()) {
			return -1;
		}

		// If equal priority, order by names
		if (getPriority() != other.getPriority()) {
			// order by priority (lower value is higher priority)
			if (getPriority() < other.getPriority()) {
				return -1;
			}
			return 1;
		}
		int n = getName().compareTo(other.getName());
		if (n != 0)
			return n;
		return getDisplayString().compareTo(other.getDisplayString());
	}

	public boolean isUser() {
		return isUser;
	}

	public boolean isBlocked() {
		return isBlocked;
	}

	public int getPriority() {
		return priority;
	}

	public String getName() {
		return name;
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public int getState() {
		return state;
	}

	public String getDisplayString() {
		return displayString;
	}
	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return reference.toString();
	}
}
