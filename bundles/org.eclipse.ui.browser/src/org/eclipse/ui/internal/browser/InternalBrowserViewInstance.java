/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.AbstractWebBrowser;
/**
 * An instance of a running Web browser.
 */
public class InternalBrowserViewInstance extends AbstractWebBrowser {
	protected int style;
	protected String name;
	protected String tooltip;
	protected WebBrowserView view;
	protected IPartListener listener;

	public InternalBrowserViewInstance(String id, int style, String name, String tooltip) {
		super(WebBrowserView.encodeStyle(id, style));
		this.style = style;
		this.name = name;
		this.tooltip = tooltip;
	}

	public void openURL(URL url) throws PartInitException {
        IWorkbenchWindow workbenchWindow = WebBrowserUIPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow();
        final IWorkbenchPage page = workbenchWindow.getActivePage();
		if (view == null) {
            try {
				IViewPart viewPart = page.showView(WebBrowserView.WEB_BROWSER_VIEW_ID, getId(), IWorkbenchPage.VIEW_CREATE);
				view = (WebBrowserView) viewPart;
				listener = new IPartListener() {
					public void partActivated(IWorkbenchPart part) {
						// ignore
					}

					public void partBroughtToTop(IWorkbenchPart part) {
						// ignore
					}

					public void partClosed(IWorkbenchPart part) {
						if (part.equals(view)) {
							view = null;
							page.removePartListener(listener);
							DefaultBrowserSupport.getInstance().removeBrowser(getId());
						}
					}

					public void partDeactivated(IWorkbenchPart part) {
						// ignore
					}

					public void partOpened(IWorkbenchPart part) {
						// ignore
					}
				};
				page.addPartListener(listener);
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error opening Web browser", e);
			}
		}
        if (view!=null) {
            page.bringToTop(view);
            view.setURL(url.toExternalForm());
        }
	}

	public boolean close() {
		try {
			view.getSite().getPage().hideView(view);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}