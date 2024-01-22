/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.browser;

import org.eclipse.ui.PartInitException;

/**
 * Implements <code>IWorkbenchBrowserSupport</code> while leaving some methods
 * to the implementors. Classes that extend this abstract class are meant to be
 * contributed via 'org.eclipse.ui.browserSupport' extension point.
 *
 * @since 3.1
 */
public abstract class AbstractWorkbenchBrowserSupport implements IWorkbenchBrowserSupport {

	private static final String SHARED_EXTERNAL_BROWSER_ID = "org.eclipse.ui.externalBrowser"; //$NON-NLS-1$

	/**
	 * The default constructor.
	 */
	public AbstractWorkbenchBrowserSupport() {
	}

	@Override
	public IWebBrowser getExternalBrowser() throws PartInitException {
		return createBrowser(AS_EXTERNAL, SHARED_EXTERNAL_BROWSER_ID, null, null);
	}

	@Override
	public boolean isInternalWebBrowserAvailable() {
		return false;
	}
}