package org.eclipse.debug.ui.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.console.*;
import org.eclipse.swt.graphics.Color;

/**
 * Default console content provider for a processs.
 * <p>
 * Clients implementing a console content provider should subclass this class.
 * </p>
 * <p>
 * <b>This interface is still evolving</b>
 * </p>
 * @since 2.1
 */
public class ConsoleContentProvider implements IConsoleContentProvider {

	private IProcess fProcess;
	private IConsole fConsole;
	
	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleContentProvider#connect(IProcess, IConsole)
	 */
	public void connect(IProcess process, IConsole 	console) {
		fProcess = process;
		fConsole = console;
		fConsole.connect(fProcess.getStreamsProxy());
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleContentProvider#disconnect()
	 */
	public void disconnect() {
		fConsole = null;
		fProcess = null;
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleContentProvider#isReadOnly()
	 */
	public boolean isReadOnly() {
		return fProcess == null || fProcess.isTerminated();
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleContentProvider#getForeground(java.lang.String)
	 */
	public Color getColor(String streamIdentifer) {
		return DebugUIPlugin.getPreferenceColor(streamIdentifer);
	}

	/**
	 * Returns the process this content provider is providing content for, or
	 * <code>null</code> if none.
	 * 
	 * @return the process this content provider is providing content for, or
	 * <code>null</code> if none
	 */
	protected IProcess getProcess() {
		return fProcess;
	}
	
	/**
	 * Returns the consonle this content provider is connected to, or
	 * <code>null</code> if none.
	 * 
	 * @return IConsole the consonle this content provider is connected to, or
	 * <code>null</code> if none	 */
	protected IConsole getConsole() {
		return fConsole;
	}
}
