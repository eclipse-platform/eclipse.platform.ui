package org.eclipse.help.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.widgets.Composite;
import org.eclipse.help.internal.ui.util.HelpWorkbenchException;

/**
 * Factory for creating a help browser
 */
public interface IBrowserFactory {
	/**
	 * Creates a browser control instance
	 */
	public IBrowser createBrowser(Composite parent) throws HelpWorkbenchException;
}
