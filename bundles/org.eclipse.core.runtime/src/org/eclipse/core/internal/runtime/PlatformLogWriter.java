/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import org.eclipse.core.runtime.*;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;

/**
 * A log writer that writes log entries.  
 * See PlatformLogReader for reading logs back into memory.
 */
public class PlatformLogWriter implements ILogListener {
	/**
	 * @see ILogListener#logging.
	 */
	public synchronized void logging(IStatus status, String plugin) {
		writeLog(status, 0);
	}
	protected void writeLog(IStatus status, int depth) {
		StringBuffer entry = new StringBuffer();
		entry.append(status.getPlugin()).append(" "); //$NON-NLS-1$
		entry.append(Integer.toString(status.getSeverity())).append(" "); //$NON-NLS-1$
		entry.append(Integer.toString(status.getCode()));
		Throwable t = status.getException();
		int stackCode = t instanceof CoreException ? 1 : 0;
		FrameworkLogEntry logEntry = new FrameworkLogEntry(depth,entry.toString(),status.getMessage(),stackCode,t);
		InternalPlatform.getDefault().getFrameworkLog().log(logEntry);
		// ensure a substatus inside a CoreException is properly logged 
		if (stackCode == 1)
			writeLog(((CoreException)t).getStatus(),0);
		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				writeLog(children[i], depth + 1);
			}
		}
	}
}