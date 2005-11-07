/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.update;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.internal.ui.viewers.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;

/**
 * @since 3.2
 */
public class EventHandlerModelProxy extends AbstractModelProxy implements IDebugEventSetListener {

	/**
	 * Map of elements to timer tasks
	 */
	private Map fTimerTasks = new HashMap();

	/**
	 * Timer for timer tasks
	 */
	private Timer fTimer = new Timer(true);

	/**
	 * Map of event source to resume events with a pending suspend that timed
	 * out.
	 */
	private Map fPendingSuspends = new HashMap();

	/**
	 * Event handlers for specific elements
	 */
	private DebugEventHandler[] fHandlers = new DebugEventHandler[0];

	private boolean fDisposed = false;

	/**
	 * Task used to update an element that resumed for a step or evaluation that
	 * took too long to suspend.
	 */
	private class PendingSuspendTask extends TimerTask {

		private DebugEvent fEvent;
		private DebugEventHandler fHandler;

		/**
		 * Resume event for which there is a pending suspend.
		 * 
		 * @param resume event
		 */
		public PendingSuspendTask(DebugEventHandler handler, DebugEvent resume) {
			fHandler = handler;
			fEvent = resume;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		public void run() {
			synchronized (fPendingSuspends) {
				fPendingSuspends.put(fEvent.getSource(), fEvent);
			}
			dispatchSuspendTimeout(fHandler, fEvent);
		}

	}

	/**
	 * Adds the given handler to this event update policy.
	 * 
	 * @param handler
	 */
	protected DebugEventHandler[] createEventHandlers() {
		return new DebugEventHandler[] { new DebugTargetEventHandler(this), new ThreadEventHandler(this) };
	}

	public synchronized void dispose() {
		fDisposed = true;
		fTimer.cancel();
		fTimerTasks.clear();
		DebugPlugin.getDefault().removeDebugEventListener(this);
		for (int i = 0; i < fHandlers.length; i++) {
			DebugEventHandler handler = fHandlers[i];
			handler.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.internal.ui.viewers.update.AbstractUpdatePolicy#init(org.eclipse.debug.ui.viewers.update.IPresentation)
	 */
	public void init(IPresentationContext context) {
		DebugPlugin.getDefault().addDebugEventListener(this);
		fHandlers = createEventHandlers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public synchronized final void handleDebugEvents(DebugEvent[] events) {
		if (isDisposed()) {
			return;
		}
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			for (int j = 0; j < fHandlers.length; j++) {
				DebugEventHandler handler = fHandlers[j];
				if (handler.handlesEvent(event)) {
					switch (event.getKind()) {
					case DebugEvent.CREATE:
						dispatchCreate(handler, event);
						break;
					case DebugEvent.TERMINATE:
						dispatchTerminate(handler, event);
						break;
					case DebugEvent.SUSPEND:
						dispatchSuspend(handler, event);
						break;
					case DebugEvent.RESUME:
						dispatchResume(handler, event);
						break;
					case DebugEvent.CHANGE:
						dispatchChange(handler, event);
						break;
					default:
						dispatchOther(handler, event);
						break;
					}
				}
			}
		}
	}

	private boolean isDisposed() {
		return fDisposed;
	}

	/**
	 * Dispatches a create event.
	 * 
	 * @param event
	 */
	protected void dispatchCreate(DebugEventHandler handler, DebugEvent event) {
		handler.handleCreate(event);
	}

	/**
	 * Dispatches a terminate event.
	 * 
	 * @param event
	 */
	protected void dispatchTerminate(DebugEventHandler handler, DebugEvent event) {
		handler.handleTerminate(event);
	}

	/**
	 * Dispatches a suspend event. Subclasses may override.
	 * 
	 * @param event
	 */
	protected void dispatchSuspend(DebugEventHandler handler, DebugEvent event) {
		// stop timer, if any
		synchronized (fTimerTasks) {
			TimerTask task = (TimerTask) fTimerTasks.remove(event.getSource());
			if (task != null) {
				task.cancel();
			}
		}
		DebugEvent resume = null;
		synchronized (fPendingSuspends) {
			resume = (DebugEvent) fPendingSuspends.remove(event.getSource());
		}
		if (resume == null) {
			handler.handleSuspend(event);
		} else {
			handler.handleLateSuspend(event, resume);
		}
	}

	/**
	 * Dispatches a resume event. By default, if the resume is for an evaluation
	 * or a step, a timer is started to update the event source if the step or
	 * evaluation takes more than 500ms. Otherwise the source is refreshed.
	 * Subclasses may override.
	 * 
	 * @param event
	 */
	protected void dispatchResume(DebugEventHandler handler, DebugEvent event) {
		if (event.isEvaluation() || event.isStepStart()) {
			// start a timer to update if the corresponding suspend does not
			// come quickly
			PendingSuspendTask task = new PendingSuspendTask(handler, event);
			synchronized (fTimerTasks) {
				fTimerTasks.put(event.getSource(), task);
			} 
			fTimer.schedule(task, 500);
			handler.handleResumeExpectingSuspend(event);
		} else {
			handler.handleResume(event);
		}
	}

	/**
	 * Dispatches a change event.
	 * 
	 * @param event
	 */
	protected void dispatchChange(DebugEventHandler handler, DebugEvent event) {
		handler.handleChange(event);
	}

	/**
	 * Dispatches an unknown event.
	 * 
	 * @param event
	 */
	protected void dispatchOther(DebugEventHandler handler, DebugEvent event) {
		handler.handleOther(event);
	}

	/**
	 * Notification that a pending suspend event was not received for the given
	 * resume event and handler within the timeout period.
	 * 
	 * @param resume resume event with missing suspend event
	 */
	protected void dispatchSuspendTimeout(DebugEventHandler handler, DebugEvent resume) {
		handler.handleSuspendTimeout(resume);
	}

}
