/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.welcome.internal;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.swt.widgets.Composite;

/**
 * Needed for the OLE implementation
 */
public class BrowserControlSite extends OleControlSite {

	protected boolean beenBuilt = false;
	protected boolean startedDownload = false;
	// Web Browser
	private WebBrowser browser;
	private String presentationURL;
	private IStatusLineManager statusLineManager;
	private int workSoFar = 0;
	private int prevMax = 0;

	public void setStatusLineManager(IStatusLineManager manager) {
		this.statusLineManager = manager;
	}

	void setBrowser(WebBrowser browser) {
		this.browser = browser;
	}

	public String getPresentationURL() {
		return presentationURL;
	}

	/**
	 * BrowserControlSite constructor
	 */
	public BrowserControlSite(Composite parent, int style, String progId) {
		super(parent, style, progId);

		addEventListener(WebBrowser.DownloadBegin, new OleListener() {
			public void handleEvent(OleEvent event) {
				startedDownload = true;
				//webProgress.setSelection(0);
				prevMax = -1;
			}
		});

		addEventListener(WebBrowser.DownloadComplete, new OleListener() {
			public void handleEvent(OleEvent event) {
				startedDownload = false;
				//webProgress.setSelection(0);
				if (statusLineManager != null)
					statusLineManager.getProgressMonitor().done();
				presentationURL = browser.getLocationURL();
			}
		});

		addEventListener(WebBrowser.BeforeNavigate2, new OleListener() {
			public void handleEvent(OleEvent event) {
				//Variant urlVar = event.arguments[1];
				//String strUrl = urlVar.getString();
			}
		});

		// Respond to ProgressChange events by updating the Progress bar
		addEventListener(WebBrowser.ProgressChange, new OleListener() {
			public void handleEvent(OleEvent event) {
				if (!startedDownload) {
					return;
				}

				Variant progress = event.arguments[0];
				Variant maxProgress = event.arguments[1];

				if (progress == null || maxProgress == null || progress.getInt() == -1) {
					return;
				}
				/*
				if (maxProgress.getInt()!=100)
				   webProgress.setMaximum(maxProgress.getInt());
				*/
				if (prevMax != maxProgress.getInt()) {
					statusLineManager.getProgressMonitor().beginTask("", maxProgress.getInt());
					prevMax = maxProgress.getInt();
				}
				//webProgress.setSelection(progress.getInt());
				int newValue = progress.getInt();
				int worked = newValue - workSoFar;
				workSoFar = newValue;
				if (statusLineManager != null)
					statusLineManager.getProgressMonitor().worked(worked);
			}
		});

		addEventListener(WebBrowser.StatusTextChange, new OleListener() {
			public void handleEvent(OleEvent event) {
				Variant newText = event.arguments[0];
				String msg = newText.getString();

				if (msg != null) {
					if (statusLineManager != null)
						statusLineManager.setMessage(msg);
				} else {
					if (statusLineManager != null)
						statusLineManager.setMessage("");
				}
			}
		});
	}
}
