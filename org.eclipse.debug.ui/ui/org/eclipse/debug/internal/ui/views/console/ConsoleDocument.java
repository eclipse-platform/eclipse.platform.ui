package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DefaultLineTracker;

public class ConsoleDocument extends AbstractDocument {
	
	private IProcess fProcess = null;
	
	public ConsoleDocument(IProcess process) {
		fProcess= process;
		setTextStore(new ConsoleOutputTextStore(2500));	
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
	}

	protected boolean isClosed() {
		return fProcess.isTerminated();
	}
	
	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		return obj instanceof ConsoleDocument && fProcess.equals(((ConsoleDocument)obj).fProcess);
    }
    
	/**
	 * @see Object#hashCode()
	 */
    public int hashCode() {
    	return fProcess.hashCode();
    }
    	
	/**
	 * Returns whether the document's underlying process is
	 * terminated.
	 */
	protected boolean isReadOnly() {
		return fProcess.isTerminated();
	}
	
}