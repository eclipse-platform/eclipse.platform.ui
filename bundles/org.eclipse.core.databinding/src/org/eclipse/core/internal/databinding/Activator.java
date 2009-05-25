/*******************************************************************************
 * Copyright (c) 2007, 2008 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding;

import java.util.ArrayList;

import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @since 3.3
 * 
 */
public class Activator implements BundleActivator {
	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "org.eclipse.core.databinding"; //$NON-NLS-1$

	private volatile static ServiceTracker _frameworkLogTracker;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		_frameworkLogTracker = new ServiceTracker(context, FrameworkLog.class.getName(), null);
		_frameworkLogTracker.open();

		Policy.setLog(new ILogger() {

			public void log(IStatus status) {
				ServiceTracker frameworkLogTracker = _frameworkLogTracker;
				FrameworkLog log = frameworkLogTracker == null ? null : (FrameworkLog) frameworkLogTracker.getService();
				if (log != null) {
					log.log(createLogEntry(status));
				} else {
					// fall back to System.err
					System.err.println(status.getPlugin() + " - " + status.getCode() + " - " + status.getMessage());  //$NON-NLS-1$//$NON-NLS-2$
					if( status.getException() != null ) {
						status.getException().printStackTrace(System.err);
					}
				}
			}

		});
	}
	
	// Code copied from PlatformLogWriter.getLog(). Why is logging an IStatus so
	// hard?
	FrameworkLogEntry createLogEntry(IStatus status) {
		Throwable t = status.getException();
		ArrayList childlist = new ArrayList();

		int stackCode = t instanceof CoreException ? 1 : 0;
		// ensure a substatus inside a CoreException is properly logged 
		if (stackCode == 1) {
			IStatus coreStatus = ((CoreException) t).getStatus();
			if (coreStatus != null) {
				childlist.add(createLogEntry(coreStatus));
			}
		}

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				childlist.add(createLogEntry(children[i]));
			}
		}

		FrameworkLogEntry[] children = (FrameworkLogEntry[]) (childlist.size() == 0 ? null : childlist.toArray(new FrameworkLogEntry[childlist.size()]));

		return new FrameworkLogEntry(status.getPlugin(), status.getSeverity(), status.getCode(), status.getMessage(), stackCode, t, children);
	}

	
	public void stop(BundleContext context) throws Exception {
		if (_frameworkLogTracker != null) {
			_frameworkLogTracker.close();
			_frameworkLogTracker = null;
		}
	}

}
