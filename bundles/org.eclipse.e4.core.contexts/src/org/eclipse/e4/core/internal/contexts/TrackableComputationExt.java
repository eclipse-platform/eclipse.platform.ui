/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts;

import java.util.Set;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.internal.contexts.EclipseContext.Scheduled;

public class TrackableComputationExt extends Computation {

	final private IEclipseContext originatingContext;
	final private RunAndTrack runnable;

	private ContextChangeEvent cachedEvent;

	public TrackableComputationExt(RunAndTrack runnable, IEclipseContext originatingContext) {
		this.runnable = runnable;
		this.originatingContext = originatingContext;
		init();
	}

	public int hashCode() {
		return hashCode;
	}

	protected int calcHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((originatingContext == null) ? 0 : originatingContext.hashCode());
		result = prime * result + ((runnable == null) ? 0 : runnable.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TrackableComputationExt other = (TrackableComputationExt) obj;
		if (originatingContext == null) {
			if (other.originatingContext != null)
				return false;
		} else if (!originatingContext.equals(other.originatingContext))
			return false;
		if (runnable == null) {
			if (other.runnable != null)
				return false;
		} else if (!runnable.equals(other.runnable))
			return false;
		return true;
	}

	public void handleInvalid(ContextChangeEvent event, Set<Scheduled> scheduledList) {
		//	don't call super - we keep the link unless uninjected / disposed
		int eventType = event.getEventType();
		if (eventType == ContextChangeEvent.INITIAL || eventType == ContextChangeEvent.DISPOSE) {
			// process right away
			update(event);
		} else {
			// schedule processing
			scheduledList.add(new Scheduled(this, event));
		}
	}

	public boolean update(ContextChangeEvent event) {
		// is this a structural event?
		// structural changes: INITIAL, DISPOSE, UNINJECTED are always processed right away
		int eventType = event.getEventType();
		if ((runnable instanceof RunAndTrackExt) && ((RunAndTrackExt) runnable).batchProcess()) {
			if ((eventType == ContextChangeEvent.ADDED) || (eventType == ContextChangeEvent.REMOVED)) {
				cachedEvent = event;
				EclipseContext eventsContext = (EclipseContext) event.getContext();
				eventsContext.addWaiting(this);
				return true;
			}
		}

		((EclipseContext) originatingContext).pushComputation(this);
		boolean result = true;
		try {
			if (cachedEvent != null) {
				if (runnable instanceof RunAndTrackExt) {
					result = ((RunAndTrackExt) runnable).update(event.getContext(), event.getEventType(), event.getArguments());
					if (eventType != ContextChangeEvent.DISPOSE && eventType != ContextChangeEvent.UNINJECTED)
						cachedEvent = null;
				} else {
					if (eventType != ContextChangeEvent.DISPOSE && eventType != ContextChangeEvent.UNINJECTED) {
						result = runnable.changed(originatingContext);
						cachedEvent = null;
					}
				}
			}
			if (eventType != ContextChangeEvent.UPDATE) {
				if (runnable instanceof RunAndTrackExt)
					result = ((RunAndTrackExt) runnable).update(event.getContext(), event.getEventType(), event.getArguments());
				else {
					if (eventType != ContextChangeEvent.DISPOSE && eventType != ContextChangeEvent.UNINJECTED)
						result = runnable.changed(originatingContext);
				}
			}
		} finally {
			((EclipseContext) originatingContext).popComputation(this);
		}
		EclipseContext eventsContext = (EclipseContext) event.getContext();

		if (eventType == ContextChangeEvent.DISPOSE) {
			if (originatingContext.equals(eventsContext)) {
				((EclipseContext) originatingContext).removeRAT(this);
				invalidateComputation();
				return false;
			}
		}
		if (!result) {
			((EclipseContext) originatingContext).removeRAT(this);
			invalidateComputation();
		}
		return result;
	}

	public String toString() {
		return runnable.toString();
	}

}
