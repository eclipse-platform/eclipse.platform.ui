package org.eclipse.ui.externaltools.internal.ui.ant;

import java.io.PrintStream;

import org.apache.tools.ant.*;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
public class NullBuildLogger implements BuildLogger {

	/**
	 * @see org.apache.tools.ant.BuildLogger#setMessageOutputLevel(int)
	 */
	public void setMessageOutputLevel(int level) {
	}

	/**
	 * @see org.apache.tools.ant.BuildLogger#setOutputPrintStream(PrintStream)
	 */
	public void setOutputPrintStream(PrintStream output) {
	}

	/**
	 * @see org.apache.tools.ant.BuildLogger#setEmacsMode(boolean)
	 */
	public void setEmacsMode(boolean emacsMode) {
	}

	/**
	 * @see org.apache.tools.ant.BuildLogger#setErrorPrintStream(PrintStream)
	 */
	public void setErrorPrintStream(PrintStream err) {
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#buildStarted(BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#buildFinished(BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetStarted(BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#targetFinished(BuildEvent)
	 */
	public void targetFinished(BuildEvent event) {
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#taskStarted(BuildEvent)
	 */
	public void taskStarted(BuildEvent event) {
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#taskFinished(BuildEvent)
	 */
	public void taskFinished(BuildEvent event) {
	}

	/**
	 * @see org.apache.tools.ant.BuildListener#messageLogged(BuildEvent)
	 */
	public void messageLogged(BuildEvent event) {
	}

}

