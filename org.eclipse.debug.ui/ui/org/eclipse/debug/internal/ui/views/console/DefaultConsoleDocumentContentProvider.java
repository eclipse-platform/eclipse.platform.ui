package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.swt.graphics.Color;

/**
 */
public class DefaultConsoleDocumentContentProvider implements IConsoleDocumentContentProvider {

	private IProcess fProcess;
	private IConsoleDocument fPartitioner;
	
	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleDocumentContentProvider#connect(org.eclipse.debug.core.model.IProcess, org.eclipse.debug.internal.ui.views.console.IConsoleDocumentPartitioner)
	 */
	public void connect(IProcess process, IConsoleDocument partitioner) {
		fProcess = process;
		fPartitioner = partitioner;
		fPartitioner.connect(fProcess.getStreamsProxy());
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleDocumentContentProvider#disconnect()
	 */
	public void disconnect() {
		fPartitioner = null;
		fProcess = null;
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleDocumentContentProvider#isReadOnly()
	 */
	public boolean isReadOnly() {
		return isTerminated();
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleDocumentContentProvider#isTerminated()
	 */
	public boolean isTerminated() {
		return fProcess == null || fProcess.isTerminated();
	}

	/**
	 * @see org.eclipse.debug.internal.ui.views.console.IConsoleDocumentContentProvider#getForeground(java.lang.String)
	 */
	public Color getColor(String streamIdentifer) {
		return DebugUIPlugin.getPreferenceColor(streamIdentifer);
	}

}
