package org.eclipse.ant.tests.core.testplugin;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.apache.tools.ant.BuildEvent;

public class AntLoggerChecker {
	
	private static AntLoggerChecker deflt= null;
	
	private int taskStartedCount;
	
	private int taskFinishedCount;
	
	private int messagesLoggedCount;
	
	private int targetsStartedCount;
	
	private int targetsFinishedCount;
	
	private int buildsStartedCount;
	
	private int buildsFinishedCount;
	
	private String lastMessageLogged;
	
	private AntLoggerChecker()  {
	}
	
	/**
	 * Returns the singleton AntLoggerChecker
	 */
	public static AntLoggerChecker getDefault() {
		if (deflt == null) {
			deflt= new AntLoggerChecker();
		}
		return deflt;
	}
	
	/**
	 * Returns the singleton AntLoggerChecker
	 */
	public static void reset() {
		if (deflt != null) {
			deflt.resetState();
		}
	}
	/**
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
		buildsFinishedCount++;
	}

	
	public void buildStarted(BuildEvent event) {
		buildsStartedCount++;
	}

	
	public void messageLogged(String message) {
		messagesLoggedCount++;
		setLastMessageLogged(message);
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void targetFinished(BuildEvent event) {
		targetsFinishedCount++;
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
		targetsStartedCount++;
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void taskFinished(BuildEvent event) {
		taskFinishedCount++;
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void taskStarted(BuildEvent event) {
		taskStartedCount++;
	}
	
	/**
	 * Returns the buildsFinishedCount.
	 * @return int
	 */
	public int getBuildsFinishedCount() {
		return buildsFinishedCount;
	}

	/**
	 * Returns the buildsStartedCount.
	 * @return int
	 */
	public int getBuildsStartedCount() {
		return buildsStartedCount;
	}

	/**
	 * Returns the messagesLoggedCount.
	 * @return int
	 */
	public int getMessagesLoggedCount() {
		return messagesLoggedCount;
	}

	/**
	 * Returns the targetsFinishedCount.
	 * @return int
	 */
	public int getTargetsFinishedCount() {
		return targetsFinishedCount;
	}

	/**
	 * Returns the targetsStartedCount.
	 * @return int
	 */
	public int getTargetsStartedCount() {
		return targetsStartedCount;
	}

	/**
	 * Returns the taskFinishedCount.
	 * @return int
	 */
	public int getTaskFinishedCount() {
		return taskFinishedCount;
	}

	/**
	 * Returns the taskStartedCount.
	 * @return int
	 */
	public int getTaskStartedCount() {
		return taskStartedCount;
	}
	
	protected void resetState() {
		taskStartedCount= 0;
		taskFinishedCount= 0;
		messagesLoggedCount= 0;
		targetsStartedCount= 0;
		targetsFinishedCount= 0;
		buildsStartedCount= 0;
		buildsFinishedCount= 0;
		lastMessageLogged= null;
	}
	/**
	 * Returns the lastMessageLogged.
	 * @return String
	 */
	public String getLastMessageLogged() {
		return lastMessageLogged;
	}

	protected void setLastMessageLogged(String lastMessageLogged) {
		this.lastMessageLogged = lastMessageLogged;
	}
}
