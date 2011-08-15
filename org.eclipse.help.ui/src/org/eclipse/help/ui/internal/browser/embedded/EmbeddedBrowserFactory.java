/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Martin Oberhuber (Wind River) - [352077] error dialogs when just probing browser
 *******************************************************************************/
package org.eclipse.help.ui.internal.browser.embedded;

import org.eclipse.core.runtime.*;
import org.eclipse.help.browser.*;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.osgi.service.environment.*;
import org.eclipse.swt.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.widgets.*;

public class EmbeddedBrowserFactory implements IBrowserFactory {
	private boolean tested = false;

	private boolean available = false;

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
		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_STANDALONE) {
			try {
				if (HelpUIEventLoop.isRunning()) {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							test();
						}
					});
				}
			} catch (Exception e) {
				// just in case
			}
		} else {
			test();
		}
		tested = true;
		return available;
	}

	/**
	 * Must run on UI thread
	 * 
	 * @return
	 */
	private boolean test() {
		if (!Constants.OS_WIN32.equalsIgnoreCase(Platform.getOS())
				&& !Constants.OS_LINUX.equalsIgnoreCase(Platform.getOS())) {
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
					Status errorStatus = new Status(IStatus.WARNING, HelpUIPlugin.PLUGIN_ID, IStatus.OK, 
							"An error occurred during creation of embedded help browser.", new Exception(se)); //$NON-NLS-1$
					HelpPlugin.getDefault().getLog().log(errorStatus);
				}
			} catch (Exception e) {
				// Browser not implemented
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
