/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.util;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
/**
 * The Policy class is a class to handle debug flags within the JFace plug-in.
 * @since 3.0
 */
public class Policy {
	public static final boolean DEFAULT = false;
	public static final String JFACE = "org.eclipse.jface";//$NON-NLS-1$
	private static ILog log;
	public static boolean DEBUG_DIALOG_NO_PARENT = DEFAULT;
	static {
		if (getDebugOption("/debug")) { //$NON-NLS-1$
			DEBUG_DIALOG_NO_PARENT = getDebugOption("/debug/dialog/noparent"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Get the dummy log to use if none has been set
	 * @return ILog
	 */
	private static ILog getDummyLog() {
		return new ILog() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ILog#addLogListener(org.eclipse.core.runtime.ILogListener)
			 */
			public void addLogListener(ILogListener listener) {
				// Do nothing as this is a dummy placeholder
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ILog#getBundle()
			 */
			public Bundle getBundle() {
				//Do nothing as this is a dummy placeholder
				return null;
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ILog#log(org.eclipse.core.runtime.IStatus)
			 */
			public void log(IStatus status) {
				//Do nothing as this is a dummy placeholder
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.ILog#removeLogListener(org.eclipse.core.runtime.ILogListener)
			 */
			public void removeLogListener(ILogListener listener) {
				//Do nothing as this is a dummy placeholder
			}
		};
	}
	private static boolean getDebugOption(String option) {
		return "true".equalsIgnoreCase(Platform.getDebugOption(JFACE + option)); //$NON-NLS-1$
	}
	
	/**
	 * Set the log to be forwarding log,
	 * @param forwardingLog
	 */
	public static void setLog(ILog forwardingLog){
		log = forwardingLog;
	}
	/**
	 * Return the log the receiver is using.
	 * @return
	 */
	public static ILog getLog(){
		if (log == null)
			log = getDummyLog();
		return log;
	}
}