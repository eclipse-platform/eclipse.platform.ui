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
	
	/**
	 * Constant for the the default setting for debug options.
	 */
	public static final boolean DEFAULT = false;
	/**
	 * Constant for the first segment of jface debug option
	 * names.
	 */
	public static final String JFACE = "org.eclipse.jface";//$NON-NLS-1$
	private static ILog log;
	
	/**
	 * A flag to indicate whether unparented dialogs should
	 * be checked.
	 */
	public static boolean DEBUG_DIALOG_NO_PARENT = DEFAULT;
	
	/**
	 * A flag to indicate whether actions are being traced.
	 */
	public static boolean TRACE_ACTIONS = DEFAULT;
	
	/**
	 * A flag to indicate whether toolbars are being traced.
	 */
	
	public static boolean TRACE_TOOLBAR = DEFAULT;
	static {
		if (getDebugOption("/debug")) { //$NON-NLS-1$
			DEBUG_DIALOG_NO_PARENT = getDebugOption("/debug/dialog/noparent"); //$NON-NLS-1$
			TRACE_ACTIONS = getDebugOption("/trace/actions"); //$NON-NLS-1$
			TRACE_TOOLBAR = getDebugOption("/trace/toolbarDisposal"); //$NON-NLS-1$
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
				System.err.println(status.getMessage());
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
		if(Platform.isRunning())
			return "true".equalsIgnoreCase(Platform.getDebugOption(JFACE + option)); //$NON-NLS-1$
		return false;
	}
	
	/**
	 * Set the log to be forwarding log.
	 * @param forwardingLog
	 */
	public static void setLog(ILog forwardingLog){
		log = forwardingLog;
	}
	/**
	 * Return the log the receiver is using.
	 * @return ILog
	 */
	public static ILog getLog(){
		if (log == null)
			log = getDummyLog();
		return log;
	}
}