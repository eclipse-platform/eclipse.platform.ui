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
package org.eclipse.help.ui.internal.browser.embedded;
import org.eclipse.help.browser.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.swt.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.widgets.*;
public class EmbeddedBrowserFactory implements IBrowserFactory {
	private boolean tested = false;
	private boolean available;
	/**
	 * Constructor.
	 */
	public EmbeddedBrowserFactory() {
		super();
	}
	/*
	 * @see IBrowserFactory#isAvailable()
	 */
	public boolean isAvailable() {
		if (!System.getProperty("os.name").startsWith("Win")
			&& !System.getProperty("os.name").startsWith("Linux")) {
			return false;
		}
		if (!tested) {
			tested = true;
			Shell sh = new Shell();
			try {
				new Browser(sh, SWT.NONE);
				available = true;
			} catch (SWTError se) {
				if (se.code == SWT.ERROR_NO_HANDLES) {
					// Browser not implemented
					available = false;
				} else {
					HelpUIPlugin.logError(
						HelpUIResources.getString(
							"EmbeddedBrowserFactory.error"),
						se);
				}
			}
			if (sh != null && !sh.isDisposed())
				sh.dispose();
		}
		return available;
	}
	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		return new EmbeddedBrowserAdapter();
	}
}
