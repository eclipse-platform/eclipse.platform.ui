/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.browser.macosx;

import org.eclipse.core.runtime.*;
import org.eclipse.help.browser.*;
import org.eclipse.osgi.service.environment.*;

public class DefaultBrowserFactory implements IBrowserFactory {

	public DefaultBrowserFactory() {
		super();
	}

	/*
	 * @see IBrowserFactory#isAvailable()
	 */
	public boolean isAvailable() {
		return Constants.OS_MACOSX.equalsIgnoreCase(Platform.getOS());
		/*
		 * we assume that every Mac OS X has an "/usr/bin/osascript" so we don't
		 * test any further
		 */
	}

	/*
	 * @see IBrowserFactory#createBrowser()
	 */
	public IBrowser createBrowser() {
		return DefaultBrowserAdapter.getInstance();
	}
}
