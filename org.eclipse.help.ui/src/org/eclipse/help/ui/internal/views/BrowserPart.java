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
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.util.URLCoder;
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
import org.eclipse.ui.IMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class BrowserPart extends AbstractFormPart implements IHelpPart {
	private final static String QUERY = "BrowserPartQuery:"; //$NON-NLS-1$

	private ReusableHelpPart parent;

	private Browser browser;

	private String id;

	private int lastProgress = -1;

	private String url;

	private Action showExternalAction;

	private Action syncTocAction;

	private Action bookmarkAction;

	private Action printAction;

	private String statusURL;

	private String title;

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
				updateSyncTocAction();
			}
		});
		browser.addProgressListener(new ProgressListener() {
			public void changed(ProgressEvent e) {
				if (e.current == e.total)
					return;
				IStatusLineManager slm = BrowserPart.this.parent
						.getStatusLineManager();
				IProgressMonitor monitor = slm != null ? slm
						.getProgressMonitor() : null;
				if (lastProgress == -1) {
					lastProgress = 0;
					if (monitor != null) {
						monitor.beginTask("", e.total); //$NON-NLS-1$
						slm.setCancelEnabled(true);
					}
				} else if (monitor != null && monitor.isCanceled()) {
					browser.stop();
					return;
				}
				if (monitor != null)
					monitor.worked(e.current - lastProgress);
				lastProgress = e.current;
			}

			public void completed(ProgressEvent e) {
				IStatusLineManager slm = BrowserPart.this.parent
						.getStatusLineManager();
				IProgressMonitor monitor = slm != null ? slm
						.getProgressMonitor() : null;
				if (monitor != null) {
					slm.setCancelEnabled(false);
					monitor.done();
				}
				lastProgress = -1;
				String value = executeQuery("document.title"); //$NON-NLS-1$
				BrowserPart.this.title = value != null ? value : "N/A"; //$NON-NLS-1$
			}
		});
		browser.addStatusTextListener(new StatusTextListener() {
			public void changed(StatusTextEvent event) {
				if (processQuery(event.text))
					return;
				IStatusLineManager statusLine = BrowserPart.this.parent
						.getStatusLineManager();
				if (statusLine != null)
					statusLine.setMessage(event.text);
				if (event.text.indexOf("://") != -1) //$NON-NLS-1$
					statusURL = event.text;
			}
		});
		browser.addOpenWindowListener(new OpenWindowListener() {
			public void open(WindowEvent event) {
				if (statusURL != null) {
					try {
						String relativeURL = BaseHelpSystem.unresolve(new URL(
								statusURL));
						if (BrowserPart.this.parent.isHelpResource(relativeURL)) {
							BrowserPart.this.parent
									.showExternalURL(relativeURL);
							event.required = true;
						}
					} catch (MalformedURLException e) {
						// TODO report this
					}
				}
			}
		});
		contributeToToolBar(tbm);
	}

	private String executeQuery(String domValue) {
		String query = "window.status=\"" + QUERY + "\"+" + domValue + ";"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		boolean status = browser.execute(query);
		if (status) {
			return (String) browser.getData("query"); //$NON-NLS-1$
		}
		return null;
	}

	private boolean processQuery(String text) {
		if (text.startsWith(QUERY)) {
			browser.setData("query", text.substring(QUERY.length())); //$NON-NLS-1$
			return true;
		}
		return false;
	}

	private void contributeToToolBar(IToolBarManager tbm) {
		showExternalAction = new Action() {
			public void run() {
				BusyIndicator.showWhile(browser.getDisplay(), new Runnable() {
					public void run() {
						try {
							parent.showExternalURL(BaseHelpSystem
									.unresolve(new URL(url)));
						} catch (MalformedURLException e) {
							// TODO report this
						}
					}
				});
			}
		};
		showExternalAction
				.setToolTipText(Messages.BrowserPart_showExternalTooltip);
		showExternalAction.setImageDescriptor(HelpUIResources
				.getImageDescriptor(IHelpUIConstants.IMAGE_NW));
		syncTocAction = new Action() {
			public void run() {
				doSyncToc();
			}
		};
		syncTocAction.setToolTipText(Messages.BrowserPart_syncTocTooltip);
		syncTocAction.setImageDescriptor(HelpUIResources
				.getImageDescriptor(IHelpUIConstants.IMAGE_SYNC_TOC));
		syncTocAction.setEnabled(false);
		bookmarkAction = new Action() {
			public void run() {
				String href = BaseHelpSystem.unresolve(url);
				BaseHelpSystem.getBookmarkManager().addBookmark(href, title);
			}
		};
		bookmarkAction.setToolTipText(Messages.BrowserPart_bookmarkTooltip);
		bookmarkAction.setImageDescriptor(HelpUIResources
				.getImageDescriptor(IHelpUIConstants.IMAGE_ADD_BOOKMARK));
		tbm.insertBefore("back", showExternalAction); //$NON-NLS-1$
		tbm.insertBefore("back", syncTocAction); //$NON-NLS-1$
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
	public void init(ReusableHelpPart parent, String id, IMemento memento) {
		this.parent = parent;
		this.id = id;
		if (memento != null) {
			String href = memento.getString("BrowserPart.url"); //$NON-NLS-1$
			if (href != null)
				showURL(BaseHelpSystem.resolve(href, "/help/ntopic").toString()); //$NON-NLS-1$
		}
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
		if (browser != null && !browser.isDisposed()) {
			browser.stop();
		}
	}

	private void doPrint() {
		browser.execute("window.print();"); //$NON-NLS-1$
	}

	private void doSyncToc() {
		String href = BaseHelpSystem.unresolve(this.url);
		int ix = href.indexOf("?resultof="); //$NON-NLS-1$
		if (ix >= 0) {
			href = href.substring(0, ix);
		}
		parent.showPage(IHelpUIConstants.HV_ALL_TOPICS_PAGE);
		AllTopicsPart part = (AllTopicsPart) parent
				.findPart(IHelpUIConstants.HV_TOPIC_TREE);
		if (part != null) {
			part.selectReveal(href);
		}
	}

	private void updateSyncTocAction() {
		String href = BaseHelpSystem.unresolve(this.url);
		syncTocAction.setEnabled(parent.isHelpResource(href));
	}

	private boolean redirectLink(final String url) {
		if (url.indexOf("/topic/") != -1) { //$NON-NLS-1$
			if (url.indexOf("noframes") == -1) { //$NON-NLS-1$
				// char sep = url.lastIndexOf('?') != -1 ? '&' : '?';
				// String newURL = url + sep + "noframes=true"; //$NON-NLS-1$
				return true;
			}
		} else if (url.indexOf("livehelp/?pluginID=")>0) { //$NON-NLS-1$
			processLiveAction(url);
			return true;
		}
		return false;
	}

	private void processLiveAction(String url) {
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
		if (!"true".equalsIgnoreCase(prefs.getString("activeHelp"))) { //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		String query = null;
		try {
			URL u = new URL(url);
			query = u.getQuery();
		} catch (MalformedURLException mue) {
		}
		if (query == null)
			return;
		StringTokenizer st = new StringTokenizer(query, "=&"); //$NON-NLS-1$
		if (st.countTokens() < 6) {
			return;
		}
		st.nextToken();
		String pluginId = URLCoder.decode(st.nextToken());
		st.nextToken();
		String className = URLCoder.decode(st.nextToken());
		st.nextToken();
		String arg = URLCoder.decode(st.nextToken());
		if (pluginId == null || className == null || arg == null)
			return;
		BaseHelpSystem.runLiveHelp(pluginId, className, arg);
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
		showURL(this.url);
	}

	public void saveState(IMemento memento) {
		if (url != null) {
			String href = BaseHelpSystem.unresolve(url);
			memento.putString("BrowserPart.url", href); //$NON-NLS-1$
		}
	}
}
