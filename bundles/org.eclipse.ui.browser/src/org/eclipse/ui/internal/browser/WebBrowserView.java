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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
/**
 * A Web browser viewer.
 */
public class WebBrowserView extends ViewPart implements IBrowserViewerContainer {
	public static final String WEB_BROWSER_VIEW_ID = "org.eclipse.ui.browser.view";
    private static final int DEFAULT_STYLE = BrowserViewer.BUTTON_BAR | BrowserViewer.LOCATION_BAR;
    private static final char STYLE_SEP = '-';

	protected BrowserViewer viewer;

	public void createPartControl(Composite parent) {
        int style = decodeStyle(getViewSite().getSecondaryId());
        viewer = new BrowserViewer(parent, style);
        viewer.setContainer(this);
	}
    
    /**
     * Encodes browser style in the secondary id as id-style
     * @param browserId
     * @param style
     * @return
     */
    public static String encodeStyle(String browserId, int style) {
        return browserId+STYLE_SEP+style;
    }
    
    private int decodeStyle(String secondaryId) {
        int sep = secondaryId.lastIndexOf(STYLE_SEP);
        if (sep== -1) return DEFAULT_STYLE;
        String stoken = secondaryId.substring(sep+1);
        try {
            return Integer.parseInt(stoken);
        }
        catch (NumberFormatException e) {
            return DEFAULT_STYLE;
        }
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
}