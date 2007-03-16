/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dynamic;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.AbstractWorkbenchBrowserSupport;
import org.eclipse.ui.browser.IWebBrowser;

/**
 * @since 3.1
 */
public class DynamicBrowserSupport extends AbstractWorkbenchBrowserSupport {

    /**
     * 
     */
    public DynamicBrowserSupport() {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.browser.IWorkbenchBrowserSupport#createBrowser(int, java.lang.String, java.lang.String, java.lang.String)
     */
    public IWebBrowser createBrowser(int style, String browserId, String name,
            String tooltip) throws PartInitException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.browser.IWorkbenchBrowserSupport#createBrowser(java.lang.String)
     */
    public IWebBrowser createBrowser(String browserId) throws PartInitException {
        return null;
    }

}
