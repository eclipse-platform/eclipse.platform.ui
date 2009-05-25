/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris Gross (schtoo@schtoo.com) - support for ILogger added
 *       (bug 49497 [RCP] JFace dependency on org.eclipse.core.runtime enlarges standalone JFace applications)
 *     Brad Reynolds - bug 164653
 *     Tom Schindl <tom.schindl@bestsolution.at> - bug 194587
 *******************************************************************************/
package org.eclipse.core.databinding.util;

import org.eclipse.core.runtime.IStatus;

/**
 * The Policy class handles settings for behaviour, debug flags and logging
 * within JFace Data Binding.
 * 
 * @since 1.1
 */
public class Policy {

	/**
	 * Constant for the the default setting for debug options.
	 */
	public static final boolean DEFAULT = false;

	/**
	 * The unique identifier of the JFace plug-in.
	 */
	public static final String JFACE_DATABINDING = "org.eclipse.core.databinding";//$NON-NLS-1$

	private static ILogger log;

	/**
	 * Returns the dummy log to use if none has been set
	 */
	private static ILogger getDummyLog() {
		return new ILogger() {
			public void log(IStatus status) {
				System.err.println(status.toString());
				if( status.getException() != null ) {
					status.getException().printStackTrace(System.err);
				}
			}
		};
	}

	/**
	 * Sets the logger used by JFace Data Binding to log errors.
	 * 
	 * @param logger
	 *            the logger to use, or <code>null</code> to use the default
	 *            logger
	 */
	public static synchronized void setLog(ILogger logger) {
		log = logger;
	}

	/**
	 * Returns the logger used by JFace Data Binding to log errors.
	 * <p>
	 * The default logger prints the status to <code>System.err</code>.
	 * </p>
	 * 
	 * @return the logger
	 */
	public static synchronized ILogger getLog() {
		if (log == null) {
			log = getDummyLog();
		}
		return log;
	}

}
