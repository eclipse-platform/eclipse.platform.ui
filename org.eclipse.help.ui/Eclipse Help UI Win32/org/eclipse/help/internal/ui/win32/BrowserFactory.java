package org.eclipse.help.internal.ui.win32;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.widgets.Composite;
import org.eclipse.help.internal.ui.*;
import org.eclipse.help.internal.ui.util.HelpWorkbenchException;

/**
 * Browser Factory
 */
public class BrowserFactory implements IBrowserFactory {
	/**
	 * BrowserFactory constructor.
	 */
	public BrowserFactory() {
		super();
	}
	/**
	 * Creates a browser control instance
	 */
	public IBrowser createBrowser(Composite parent) throws HelpWorkbenchException {
		return new WebBrowser(parent);
	}
}
