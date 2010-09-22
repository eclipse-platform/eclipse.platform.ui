/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.net.URL;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
/**
 * An instance of a running Web browser.
 */
public class InternalBrowserViewInstance extends InternalBrowserInstance {
	public InternalBrowserViewInstance(String id, int style, String name, String tooltip) {
		super(WebBrowserUtil.encodeStyle(id, style), style, name, tooltip);
	}

	public void openURL(URL url) throws PartInitException {
        IWorkbenchWindow workbenchWindow = WebBrowserUIPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow();
        final IWorkbenchPage page = workbenchWindow.getActivePage();
        WebBrowserView view = (WebBrowserView)part;
		if (view == null) {
            try {
				view = (WebBrowserView)page.showView(WebBrowserView.WEB_BROWSER_VIEW_ID, getId(), IWorkbenchPage.VIEW_CREATE);
				if (tooltip != null && tooltip.length() > 0) {
				    view.setBrowserViewTooltip(tooltip);
				}
				if (name != null && name.length() > 0) {
				    view.setBrowserViewName(name);
				}
				hookPart(page, view);
                
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error opening Web browser", e); //$NON-NLS-1$
			}
		}
        if (view!=null) {
            page.bringToTop(view);
            view.setURL(url.toExternalForm());
        }
	}

	public boolean close() {
        return ((WebBrowserView)part).close();
	}
}