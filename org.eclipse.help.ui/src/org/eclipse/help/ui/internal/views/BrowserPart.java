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
package org.eclipse.help.ui.internal.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class BrowserPart extends AbstractFormPart implements IHelpPart {
	private ReusableHelpPart parent;
	private Browser browser;
	private String id;
	private int lastProgress = -1;

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
		browser.addProgressListener(new ProgressListener() {
			public void changed(ProgressEvent e) {
				if (e.current == e.total)
					return;
				IProgressMonitor monitor = BrowserPart.this.parent.getStatusLineManager().getProgressMonitor();
				if (lastProgress == -1) {
					lastProgress = 0;
					monitor.beginTask("", e.total); //$NON-NLS-1$
				}
				monitor.worked(e.current - lastProgress);
				lastProgress = e.current;
			}
			public void completed(ProgressEvent e) {
				IProgressMonitor monitor = BrowserPart.this.parent.getStatusLineManager().getProgressMonitor();
				monitor.done();
				lastProgress = -1;
			}
		});
		browser.addStatusTextListener(new StatusTextListener() {
			public void changed(StatusTextEvent event) {
				IStatusLineManager statusLine = BrowserPart.this.parent.getStatusLineManager();
				statusLine.setMessage(event.text);
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
		if (url.indexOf("/help/topic/")!= -1) { //$NON-NLS-1$
			if (url.endsWith("noframes=true")==false) { //$NON-NLS-1$
				char sep = url.lastIndexOf('?')!= -1 ? '&':'?';
				parent.showURL(url+sep+"noframes=true"); //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocusControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean hasFocusControl(Control control) {
		return browser.equals(control);
	}
}