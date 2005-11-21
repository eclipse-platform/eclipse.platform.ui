/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.util.ArrayList;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;

/**
 * A log writer that writes log entries.  
 * See PlatformLogReader for reading logs back into memory.
 * <p>
 * Note that this class just provides a bridge from the old ILog interface
 * to the OSGi FrameworkLog interface.
 */
public class PlatformLogWriter implements ILogListener {
	private final FrameworkLog frameworkLog;

	public PlatformLogWriter(FrameworkLog frameworkLog) {
		this.frameworkLog = frameworkLog;
	}

	/**
	 * @see ILogListener#logging(IStatus, String)
	 */
	public synchronized void logging(IStatus status, String plugin) {
		frameworkLog.log(getLog(status));
	}

	protected FrameworkLogEntry getLog(IStatus status) {
		StringBuffer entry = new StringBuffer();
		entry.append(status.getPlugin()).append(" "); //$NON-NLS-1$
		entry.append(Integer.toString(status.getSeverity())).append(" "); //$NON-NLS-1$
		entry.append(Integer.toString(status.getCode()));
		Throwable t = status.getException();
		ArrayList childlist = new ArrayList();

		int stackCode = t instanceof CoreException ? 1 : 0;
		// ensure a substatus inside a CoreException is properly logged 
		if (stackCode == 1) {
			IStatus coreStatus = ((CoreException) t).getStatus();
			if (coreStatus != null) {
				childlist.add(getLog(coreStatus));
			}
		}

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				childlist.add(getLog(children[i]));
			}
		}

		FrameworkLogEntry[] children = (FrameworkLogEntry[]) (childlist.size() == 0 ? null : childlist.toArray(new FrameworkLogEntry[childlist.size()]));

		return new FrameworkLogEntry(entry.toString(), status.getMessage(), stackCode, t, children);
	}
}
