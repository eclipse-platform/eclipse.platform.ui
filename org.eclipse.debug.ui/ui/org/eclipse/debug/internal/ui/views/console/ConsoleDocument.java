/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;


import org.eclipse.debug.ui.console.*;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ITextStore;

public class ConsoleDocument extends AbstractDocument {
	
	private IConsoleColorProvider fContentProvider = null;
	
	public ConsoleDocument(IConsoleColorProvider contentProvider) {
		fContentProvider = contentProvider;
		setTextStore(newTextStore());	
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
	}
    	
	/**
	 * Returns whether this document is read-only.
	 */
	public boolean isReadOnly() {
		return fContentProvider.isReadOnly();
	}
	
	/**
	 * Creates a new text store for this document.
	 */
	protected ITextStore newTextStore() {
		return new ConsoleOutputTextStore(2500);
	}
	
}
