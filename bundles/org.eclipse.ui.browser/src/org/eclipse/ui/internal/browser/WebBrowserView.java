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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.ViewPart;
/**
 * A Web browser viewer.
 */
public class WebBrowserView extends ViewPart implements IBrowserViewerContainer {
	public static final String WEB_BROWSER_VIEW_ID = "org.eclipse.ui.browser.view"; //$NON-NLS-1$
    protected BrowserViewer viewer;

	public void createPartControl(Composite parent) {
        int style = WebBrowserUtil.decodeStyle(getViewSite().getSecondaryId());
        viewer = new BrowserViewer(parent, style);
        viewer.setContainer(this);
		  
		  /*PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (BrowserViewer.PROPERTY_TITLE.equals(event.getPropertyName())) {
						setPartName((String) event.getNewValue());
					}
				}
		  };
		  viewer.addPropertyChangeListener(propertyChangeListener);*/
	}
    
    public void setURL(String url) {
		if (viewer != null)
			viewer.setURL(url);
	}

	public void setFocus() {
		viewer.setFocus();
	}

    public boolean close() {
        try {
            getSite().getPage().hideView(this);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public IActionBars getActionBars() {
        return getViewSite().getActionBars();
    }

    public void openInExternalBrowser(String url) {
        try {
            URL theURL = new URL(url);
            IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
            support.getExternalBrowser().openURL(theURL);
        }
        catch (MalformedURLException e) {
            //TODO handle this
        }
        catch (PartInitException e) {
            //TODO handle this
        }
    }
}