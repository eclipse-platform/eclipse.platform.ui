package org.eclipse.help.ui.internal.browser.win32;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */


import org.eclipse.swt.widgets.*;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.help.internal.ui.util.WorkbenchResources;
import org.eclipse.help.ui.internal.browser.win32.*;

/**
 * Needed for the OLE implementation
 */
public class HelpControlSite extends OleControlSite {
	protected boolean beenBuilt = false;
	protected boolean startedDownload = false;
	// Web Browser
	private String webHome;
	private ProgressBar webProgress;
	private Label webStatus;

	/**
	 * HelpControlSite constructor
	 */
	public HelpControlSite(Composite parent, int style, String progId) {
		super(parent, style, progId);

		addEventListener(WebBrowser.DownloadBegin, new OleListener() {
			public void handleEvent(OleEvent event) {
				startedDownload = true;
			}
		});

		addEventListener(WebBrowser.DownloadComplete, new OleListener() {
			public void handleEvent(OleEvent event) {
				startedDownload = false;
			}
		});

		addEventListener(WebBrowser.BeforeNavigate2, new OleListener() {
			public void handleEvent(OleEvent event) {
				Variant urlVar = event.arguments[1];
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

				if (progress.getInt() != 0) {
				}
			}
		});

		addEventListener(WebBrowser.StatusTextChange, new OleListener() {
			public void handleEvent(OleEvent event) {
				Variant newText = event.arguments[0];
				String msg = newText.getString();
				if (msg != null) {
					if ((msg.indexOf("http://") != -1)
						|| (msg.indexOf("javascript:") != -1)
						|| (msg.indexOf("Connecting to") != -1)
						|| (msg.indexOf("Web site found") != -1)
						|| (msg.indexOf("Finding site") != -1)
						|| (msg.indexOf("Javascript called") != -1))
						return;
				}
			}
		});

	}
}
