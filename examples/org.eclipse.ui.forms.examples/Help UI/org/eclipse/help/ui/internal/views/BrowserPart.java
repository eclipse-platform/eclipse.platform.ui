/*
 * Created on Dec 13, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BrowserPart extends AbstractFormPart implements IHelpPart {
	private ReusableHelpPart parent;
	private Browser browser;
	private String id;
	
	public BrowserPart(final Composite parent, FormToolkit toolkit) {
		browser = new Browser(parent, SWT.NULL);
		browser.addLocationListener(new LocationListener() {
			public void changing(LocationEvent event) {
				if (redirectLink(event.location))
					event.doit=false;
			}
			public void changed(LocationEvent event) {
				String url = event.location;
				BrowserPart.this.parent.browserChanged(url);
			}
		});
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#init(org.eclipse.help.ui.internal.views.NewReusableHelpPart)
	 */
	public void init(ReusableHelpPart parent, String id) {
		this.parent = parent;
		this.id = id;
	}
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return browser;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (browser!=null)
			browser.setVisible(visible);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#setFocus()
	 */
	public void setFocus() {
		if (browser!=null)
			browser.setFocus();
	}
	public void showURL(String url) {
		if (browser!=null && url!=null)
			browser.setUrl(url);
	}

	private boolean redirectLink(final String url) {
		if (url.indexOf("/help/topic/")!= -1) {
			if (url.endsWith("noframes=true")==false) {
				char sep = url.lastIndexOf('?')!= -1 ? '&':'?';
				parent.showURL(url+sep+"noframes=true");
				return true;
			}
		}
		return false;
	}
}