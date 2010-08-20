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

import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.internal.contexts.EclipseContext.Scheduled;

public class TrackableComputationExt extends Computation {

	final private IEclipseContext originatingContext;
	private RunAndTrack runnable;
	private ContextChangeEvent cachedEvent;

	public TrackableComputationExt(RunAndTrack runnable, IEclipseContext originatingContext) {
		this.runnable = runnable;
		this.originatingContext = originatingContext;
	}

	public int hashCode() {
		return 31 + ((runnable == null) ? 0 : runnable.hashCode());
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TrackableComputationExt other = (TrackableComputationExt) obj;
		if (runnable == null) {
			if (other.runnable != null)
				return false;
		} else if (!runnable.equals(other.runnable))
			return false;
		return true;
	}

	protected void doHandleInvalid(ContextChangeEvent event, List<Scheduled> scheduledList) {
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

		Computation oldComputation = EclipseContext.localComputation().get();
		EclipseContext.localComputation().set(this);
		boolean result = true;
		try {
			if (cachedEvent != null) {
				if (runnable instanceof RunAndTrackExt)
					result = ((RunAndTrackExt) runnable).update(event.getContext(), event.getEventType(), event.getArguments());
				else {
					if (eventType == ContextChangeEvent.DISPOSE)
						runnable.disposed(cachedEvent.getContext());
					else if (eventType != ContextChangeEvent.UNINJECTED)
						result = runnable.changed(cachedEvent.getContext());
				}
				cachedEvent = null;
			}
			if (eventType != ContextChangeEvent.UPDATE) {
				if (runnable instanceof RunAndTrackExt)
					result = ((RunAndTrackExt) runnable).update(event.getContext(), event.getEventType(), event.getArguments());
				else {
					if (eventType == ContextChangeEvent.DISPOSE)
						runnable.disposed(event.getContext());
					else if (eventType != ContextChangeEvent.UNINJECTED)
						result = runnable.changed(event.getContext());
				}
			}
		} finally {
			EclipseContext.localComputation().set(oldComputation);
		}
		EclipseContext eventsContext = (EclipseContext) event.getContext();

		if (eventType == ContextChangeEvent.DISPOSE) {
			if (originatingContext.equals(eventsContext)) {
				removeAll();
				return false;
			}
		}
		if (result)
			startListening();
		else
			removeAll();
		return result;
	}

	public String toString() {
		return "TrackableComputationExt(" + runnable + ')'; //$NON-NLS-1$
	}

	public void startAccessRecording() {
		EclipseContext.localComputation().set(this);
	}

	public void stopAccessRecording() {
		EclipseContext.localComputation().set(null);
	}
}
