/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.console;

import org.eclipse.ant.internal.ui.launchConfigurations.TaskLinkManager;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jface.text.IRegion;

/**
 * Processes task hyperlinks as lines are appended to the console
 */
public class TaskLineTracker implements IConsoleLineTrackerExtension {

	private IConsole fConsole;

	/**
	 * Constructor for TaskLineTracker.
	 */
	public TaskLineTracker() {
		super();
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	@Override
	public void init(IConsole console) {
		fConsole = console;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	@Override
	public void lineAppended(IRegion line) {
		TaskLinkManager.processNewLine(fConsole, line);
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	@Override
	public void dispose() {
		fConsole = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.console.IConsoleLineTrackerExtension#consoleClosed()
	 */
	@Override
	public void consoleClosed() {
		TaskLinkManager.dispose(fConsole.getProcess());
		AbstractJavacPatternMatcher.consoleClosed(fConsole.getProcess());
	}
}
