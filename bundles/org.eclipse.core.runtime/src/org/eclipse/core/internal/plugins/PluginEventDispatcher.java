/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.plugins;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

public class PluginEventDispatcher implements IPluginEventDispatcher {
	private Set listeners;
	private static PluginEventDispatcher instance;
	public PluginEventDispatcher() {
		this.listeners = new HashSet();
		if (BootLoader.inDebugMode())
			this.addListener(new DebugPluginListener());
	}
	public void addListener(IPluginListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	public synchronized void removeListener(IPluginListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	public void firePluginEvent(final IPluginEvent event) {
		firePluginEvent(new IPluginEvent[] {event});
	}	
	public void firePluginEvent(final IPluginEvent[] events) {
		final IPluginListener[] tmpListeners;
		synchronized (listeners) {
			tmpListeners = (IPluginListener[]) listeners.toArray(new IPluginListener[listeners.size()]);
		}
		new DispatcherJob(tmpListeners, events).schedule();
	}
	private class DispatcherJob extends Job {
		private IPluginEvent[] events;
		private IPluginListener[] listeners;
		DispatcherJob(IPluginListener[] listeners, IPluginEvent[] events) {
			super("PluginEventDispatcher"); //$NON-NLS-1$
			this.listeners = listeners;
			this.events = events;
		}
		public IStatus run(IProgressMonitor monitor) {
			MultiStatus result = new MultiStatus(Platform.PI_RUNTIME, IStatus.OK, Policy.bind("pluginEvent.errorListener"), null); //$NON-NLS-1$
			for (int i = 0; i < listeners.length; i++) {
				try {
					listeners[i].pluginChanged(events);
				} catch (RuntimeException re) {
					String message = re.getMessage() == null ? "" : re.getMessage(); //$NON-NLS-1$
					result.add(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.OK, message, re));
				}
			}
			return result;
		}
	}
	public synchronized static PluginEventDispatcher getInstance() {
		if (instance == null)
			instance = new PluginEventDispatcher();
		return instance;
	}
	/**
	 * A simple event dumper for when running on debug mode 
	 */	
	class DebugPluginListener implements IPluginListener {
		private String getEventName(int type) {
			switch (type) {
				case IPluginEvent.INSTALLED : return "INSTALLED"; //$NON-NLS-1$
				case IPluginEvent.STARTED : return "STARTED"; //$NON-NLS-1$
				case IPluginEvent.STOPPED : return "STOPPED"; //$NON-NLS-1$
				case IPluginEvent.UPDATED : return "UPDATED"; //$NON-NLS-1$
				case IPluginEvent.UNINSTALLED : return "UNINSTALLED"; //$NON-NLS-1$
				case IPluginEvent.RESOLVED : return "RESOLVED"; //$NON-NLS-1$
				case IPluginEvent.UNRESOLVED : return "UNRESOLVED"; //$NON-NLS-1$
			}
			return "INVALID"; //$NON-NLS-1$
		}
		public void pluginChanged(IPluginEvent[] events) {
			for (int i = 0; i < events.length; i++)
				System.out.println(events[i].getPluginDescriptor().getUniqueIdentifier() + "[" +getEventName(events[i].getType()) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		}	
	}
}