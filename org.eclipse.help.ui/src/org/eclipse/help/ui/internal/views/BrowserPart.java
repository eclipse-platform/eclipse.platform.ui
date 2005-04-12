/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.ui.internal.HelpUIResources;
import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class BrowserPart extends AbstractFormPart implements IHelpPart {
	private ReusableHelpPart parent;

	private Browser browser;

	private String id;

	private int lastProgress = -1;

	private String url;

	private Action showExternalAction;
	private Action bookmarkAction;
	private Action printAction;
	private String statusURL;
	private String title;
	private boolean query=false;

	public BrowserPart(final Composite parent, FormToolkit toolkit,
			IToolBarManager tbm) {
		browser = new Browser(parent, SWT.NULL);
		browser.addLocationListener(new LocationListener() {
			public void changing(LocationEvent event) {
				if (redirectLink(event.location))
					event.doit = false;
			}

			public void changed(LocationEvent event) {
				String url = event.location;
				BrowserPart.this.parent.browserChanged(url);
				BrowserPart.this.url = url;
			}
		});
		browser.addProgressListener(new ProgressListener() {
			public void changed(ProgressEvent e) {
				if (e.current == e.total)
					return;
				IStatusLineManager slm = BrowserPart.this.parent
				.getStatusLineManager();
				IProgressMonitor monitor = slm!=null?slm.getProgressMonitor():null; 
				if (lastProgress == -1) {
					lastProgress = 0;
					if (monitor!=null) {
						monitor.beginTask("", e.total); //$NON-NLS-1$
						slm.setCancelEnabled(true);
					}
				}
				else if (monitor!=null && monitor.isCanceled()) {
					browser.stop();
					return;
				}
				if (monitor!=null)
					monitor.worked(e.current - lastProgress);
				lastProgress = e.current;
			}

			public void completed(ProgressEvent e) {
				IStatusLineManager slm = BrowserPart.this.parent
				.getStatusLineManager();
				IProgressMonitor monitor = slm!=null?slm.getProgressMonitor():null;
				if (monitor!=null) {
					slm.setCancelEnabled(false);				
					monitor.done();
				}
				lastProgress = -1;
				query=true;
				boolean status = browser.execute("window.status=document.title;");
				if (status) {
					BrowserPart.this.title = (String)browser.getData("query");
				}
				query=false;
			}
		});
		browser.addStatusTextListener(new StatusTextListener() {
			public void changed(StatusTextEvent event) {
				if (query) {
					browser.setData("query", event.text);
					query=false;
					return;
				}
				IStatusLineManager statusLine = BrowserPart.this.parent
						.getStatusLineManager();
				if (statusLine!=null)
					statusLine.setMessage(event.text);
				if (event.text.indexOf("://")!= -1) //$NON-NLS-1$
					statusURL = event.text;
			}
		});
		browser.addOpenWindowListener(new OpenWindowListener() {
			public void open(WindowEvent event) {
				if (statusURL!=null) {
					try {
					String relativeURL = BaseHelpSystem.unresolve(new URL(statusURL));
					if (BrowserPart.this.parent.isHelpResource(relativeURL)){
						BrowserPart.this.parent.showExternalURL(relativeURL);
						event.required = true;
					}
					
					}
					catch (MalformedURLException e) {
						// TODO report this
					}
				}
			}
		});
		contributeToToolBar(tbm);
	}

	private void contributeToToolBar(IToolBarManager tbm) {
		showExternalAction = new Action() {
			public void run() {
				BusyIndicator.showWhile(browser.getDisplay(), new Runnable() {
					public void run() {
						try {
							parent.showExternalURL(BaseHelpSystem.unresolve(new URL(url)));
						}
						catch (MalformedURLException e) {
							// TODO report this
						}
					}
				});
			}
		};
		showExternalAction.setToolTipText(Messages.BrowserPart_showExternalTooltip); 
		showExternalAction.setImageDescriptor(HelpUIResources
				.getImageDescriptor(IHelpUIConstants.IMAGE_NW));
		bookmarkAction = new Action() {
			public void run() {
				BaseHelpSystem.getBookmarkManager().addBookmark(url, title);
			}
		};
		bookmarkAction.setToolTipText(Messages.BrowserPart_bookmarkTooltip); 
		bookmarkAction.setImageDescriptor(HelpUIResources.getImageDescriptor(IHelpUIConstants.IMAGE_ADD_BOOKMARK));
		tbm.insertBefore("back", showExternalAction); //$NON-NLS-1$
		tbm.insertBefore("back", bookmarkAction); //$NON-NLS-1$
		tbm.insertBefore("back", new Separator()); //$NON-NLS-1$
		printAction = new Action(ActionFactory.PRINT.getId()) {
			public void run() {
				doPrint();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#init(org.eclipse.help.ui.internal.views.NewReusableHelpPart)
	 */
	public void init(ReusableHelpPart parent, String id) {
		this.parent = parent;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#getControl()
	 */
	public Control getControl() {
		return browser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (browser != null) {
			browser.setVisible(visible);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IFormPart#setFocus()
	 */
	public void setFocus() {
		if (browser != null)
			browser.setFocus();
	}

	public void showURL(String url) {
		if (browser != null && url != null) {
			browser.setUrl(url);
		}
	}
	
	public void stop() {
		if (browser!=null && !browser.isDisposed())
			browser.stop();
		
	}
	
	private void doPrint() {
		browser.execute("window.print();"); //$NON-NLS-1$
	}

	private boolean redirectLink(final String url) {
		if (url.indexOf("/topic/") != -1) { //$NON-NLS-1$
			if (url.indexOf("noframes") == -1) { //$NON-NLS-1$
				char sep = url.lastIndexOf('?') != -1 ? '&' : '?';
				String newURL = url + sep + "noframes=true"; //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public boolean fillContextMenu(IMenuManager manager) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.ui.internal.views.IHelpPart#hasFocusControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean hasFocusControl(Control control) {
		return browser.equals(control);
	}

	public IAction getGlobalAction(String id) {
		if (id.equals(ActionFactory.PRINT.getId()))
			return printAction;
		return null;
	}

	public void toggleRoleFilter() {
	}

	public void refilter() {
	}
}
