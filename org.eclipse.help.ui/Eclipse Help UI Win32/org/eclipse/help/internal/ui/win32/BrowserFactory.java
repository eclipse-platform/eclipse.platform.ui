package org.eclipse.help.internal.ui.win32;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
