/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.plugins.events;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.internal.plugins.InternalPlatform;
import org.eclipse.core.internal.plugins.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.*;

public class PluginEventDispatcher implements BundleListener {
	private Set listeners;
	private static PluginEventDispatcher instance;
	public PluginEventDispatcher() {
		this.listeners = new HashSet();
		BundleContext context = org.eclipse.core.internal.runtime.InternalPlatform.getDefault().getBundleContext();
		context.addBundleListener(this);
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
	private void firePluginEvent(final IPluginEvent event) {
		final IPluginListener[] tmpListeners;
		synchronized (listeners) {
			tmpListeners = (IPluginListener[]) listeners.toArray(new IPluginListener[listeners.size()]);
		}
		new DispatcherJob(tmpListeners, event).schedule();
	}
	private IPluginEvent createPluginEvent(BundleEvent bundleEvent) {
		String pluginId = bundleEvent.getBundle().getGlobalName();
		IPluginDescriptor pluginDescriptor = InternalPlatform.getPluginRegistry().getPluginDescriptor(pluginId);
		return new PluginEvent(pluginDescriptor, bundleEvent.getType());
	}
	public void bundleChanged(BundleEvent bundleEvent) {
		IPluginEvent event = createPluginEvent(bundleEvent);
		firePluginEvent(event);
	}
	private class DispatcherJob extends Job {
		private IPluginEvent event;
		private IPluginListener[] listeners;
		DispatcherJob(IPluginListener[] listeners, IPluginEvent event) {
			super("PluginEventDispatcher"); //$NON-NLS-1$
			this.listeners = listeners;
			this.event = event;
		}
		public IStatus run(IProgressMonitor monitor) {
			MultiStatus result = new MultiStatus(Platform.PI_RUNTIME, IStatus.OK, Policy.bind("pluginEvent.errorListener"), null); //$NON-NLS-1$
			for (int i = 0; i < listeners.length; i++) {
				try {
					// events are supposed to be batched - just wrap in an array for now
					listeners[i].pluginChanged(new IPluginEvent[] { event });
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
}
