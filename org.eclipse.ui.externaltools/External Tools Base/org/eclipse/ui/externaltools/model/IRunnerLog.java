package org.eclipse.ui.externaltools.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

/**
 * Provides an API for <code>IExternalToolRunner</code> implementors
 * to log messages captured from the running tool's output.
 * <p>
 * This interface is not be extended nor implemented by clients.
 * </p>
 */
public interface IRunnerLog {
	public static final int LEVEL_ERROR = 0;
	public static final int LEVEL_WARNING = 10;
	public static final int LEVEL_INFO = 20;
	public static final int LEVEL_VERBOSE = 30;
	public static final int LEVEL_DEBUG = 40;
	
	/**
	 * Places the specified message text into the log. Ignored
	 * if the specified message level is higher than the
	 * current filter level.
	 * 
	 * @param message the text to add to the log
	 * @param level the message priority
	 */
	public void append(String message, int level);
	
	/**
	 * Returns the current level used for filtering
	 * messages. Any calls to <code>append</code> with
	 * a level greater than this filter value will be
	 * ignored.
	 */
	public int getFilterLevel();
}
