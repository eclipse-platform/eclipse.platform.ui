/**********************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.jobs;

import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;

/**
 * Responsible for notifying all job listeners about job lifecycle events.  Uses a
 * specialized iterator to ensure the complex iteration logic is contained in one place.
 */
class JobListeners {
	/**
	 * Static singleton empty listener list.
	 */
	static final IJobChangeListener[] EMPTY_LISTENERS = new IJobChangeListener[0];
	
	interface IListenerDoit {
		public void notify(IJobChangeListener listener, IJobChangeEvent event);
	}

	private final IListenerDoit aboutToRun = new IListenerDoit() {
		public void notify(IJobChangeListener listener, IJobChangeEvent event) {
			listener.aboutToRun(event);
		}
	};
	private final IListenerDoit awake = new IListenerDoit() {
		public void notify(IJobChangeListener listener, IJobChangeEvent event) {
			listener.awake(event);
		}
	};
	private final IListenerDoit done = new IListenerDoit() {
		public void notify(IJobChangeListener listener, IJobChangeEvent event) {
			listener.done(event);
		}
	};
	private final IListenerDoit running = new IListenerDoit() {
		public void notify(IJobChangeListener listener, IJobChangeEvent event) {
			listener.running(event);
		}
	};
	private final IListenerDoit scheduled = new IListenerDoit() {
		public void notify(IJobChangeListener listener, IJobChangeEvent event) {
			listener.scheduled(event);
		}
	};
	private final IListenerDoit sleeping = new IListenerDoit() {
		public void notify(IJobChangeListener listener, IJobChangeEvent event) {
			listener.sleeping(event);
		}
	};
	/**
	 * The global job listeners.
	 */
	protected final ListenerList global = new ListenerList(ListenerList.IDENTITY);

	/**
	 * TODO Could use an instance pool to re-use old event objects
	 */
	static JobChangeEvent newEvent(Job job) {
		JobChangeEvent instance = new JobChangeEvent();
		instance.job = job;
		return instance;
	}

	static JobChangeEvent newEvent(Job job, IStatus result) {
		JobChangeEvent instance = new JobChangeEvent();
		instance.job = job;
		instance.result = result;
		return instance;
	}

	static JobChangeEvent newEvent(Job job, long delay) {
		JobChangeEvent instance = new JobChangeEvent();
		instance.job = job;
		instance.delay = delay;
		return instance;
	}

	/**
	 * Process the given doit for all global listeners and all local listeners
	 * on the given job.
	 */
	private void doNotify(final IListenerDoit doit, final IJobChangeEvent event) {
		//notify all global listeners
		Object[] listeners = global.getListeners();
		int size = listeners.length;
		for (int i = 0; i < size; i++) {
			try {
				if (listeners[i] != null)
					doit.notify((IJobChangeListener)listeners[i], event);
			} catch (Exception e) {
				handleException(e);
			} catch (LinkageError e) {
				handleException(e);
			}
		}
		//notify all local listeners
		ListenerList list = ((InternalJob) event.getJob()).getListeners();
		listeners = list == null ? null : list.getListeners();
		if (listeners == null)
			return;
		size = listeners.length;
		for (int i = 0; i < size; i++) {
			try {
				if (listeners[i] != null)
					doit.notify((IJobChangeListener)listeners[i], event);
			} catch (Exception e) {
				handleException(e);
			} catch (LinkageError e) {
				handleException(e);
			}
		}
	}

	private void handleException(Throwable e) {
		//this code is roughly copied from InternalPlatform.run(ISafeRunnable), 
		//but inlined here for performance reasons
		if (e instanceof OperationCanceledException)
			return;
		String pluginId = Platform.PI_RUNTIME;
		String message = Policy.bind("meta.pluginProblems", pluginId); //$NON-NLS-1$
		IStatus status = new Status(IStatus.ERROR, pluginId, Platform.PLUGIN_ERROR, message, e);
		//we have to be safe, so don't try to log if the platform is not running 
		//since it will fail - last resort is to print the stack trace on stderr
		if (InternalPlatform.getDefault().isRunning())
			InternalPlatform.getDefault().log(status);
		else
			e.printStackTrace();
	}

	public void add(IJobChangeListener listener) {
		global.add(listener);
	}

	public void remove(IJobChangeListener listener) {
		global.remove(listener);
	}

	public void aboutToRun(Job job) {
		doNotify(aboutToRun, newEvent(job));
	}

	public void awake(Job job) {
		doNotify(awake, newEvent(job));
	}

	public void done(Job job, IStatus result, boolean reschedule) {
		JobChangeEvent event = newEvent(job, result);
		event.reschedule = reschedule;
		doNotify(done, event);
	}

	public void running(Job job) {
		doNotify(running, newEvent(job));
	}

	public void scheduled(Job job, long delay, boolean reschedule) {
		JobChangeEvent event = newEvent(job, delay);
		event.reschedule = reschedule;
		doNotify(scheduled, event);
	}

	public void sleeping(Job job) {
		doNotify(sleeping, newEvent(job));
	}
}