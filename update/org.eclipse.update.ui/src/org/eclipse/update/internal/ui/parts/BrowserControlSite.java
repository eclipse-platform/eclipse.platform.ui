package org.eclipse.update.internal.ui.parts;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.widgets.*;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.update.internal.ui.*;
import java.net.URL;
import org.eclipse.core.runtime.Platform;
import java.io.IOException;
import java.util.*;
import org.eclipse.ui.texteditor.*;
import org.eclipse.jface.action.IStatusLineManager;

/**
 * Needed for the OLE implementation
 */
public class BrowserControlSite extends OleControlSite {

	protected boolean beenBuilt = false;
	protected boolean startedDownload = false;
	// Web Browser
	private WebBrowser browser;
	private ProgressBar webProgress;
	private Label webStatus;
	private String presentationURL;
	private boolean redirection;
	private IStatusLineManager statusLineManager;
	private int workSoFar=0;
	private int prevMax=0;
	
	public void setStatusLineManager(IStatusLineManager manager) {
		this.statusLineManager = manager;
	}
	
	void setBrowser(WebBrowser browser) {
		this.browser = browser;
	}
	
	void setStatusContainer(Composite statusContainer) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 2;
		statusContainer.setLayout(layout);
		webProgress = new ProgressBar(statusContainer, SWT.FLAT | SWT.SMOOTH | SWT.HORIZONTAL);
		webProgress.setMinimum(0);
		webProgress.setMaximum(100);
		GridData gd = new GridData();
		gd.widthHint = 150;
		webProgress.setLayoutData(gd);
		//webProgress.setVisible(false);
		
		webStatus = new Label(statusContainer, SWT.NONE);
		webStatus.setText("");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		webStatus.setLayoutData(gd);
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
				if (statusLineManager!=null) 
				   statusLineManager.getProgressMonitor().done();
				if (redirection)
				   redirection = false;
				else
				   presentationURL = browser.getLocationURL();
			}
		});

		addEventListener(WebBrowser.BeforeNavigate2, new OleListener() {
			public void handleEvent(OleEvent event) {
				Variant urlVar = event.arguments[1];
                String strUrl = urlVar.getString();
                /*
                if (urlParser.isUpdateURL(strUrl)) {
                	final String redirURL = urlParser.parseURL(strUrl);
                    Variant cancel = event.arguments[6];
                    int ptr = cancel.getByRef();
                    OS.MoveMemory(ptr, new int [] { 1 }, 4);
                    //browser.stop();
                    if (redirURL!=null) {
                   		redirection = true;
               			browser.navigate(redirURL);
                    }
                    //int ptr = urlVar.getByRef();
                    //OS.MoveMemory(ptr, redirURL.getBytes(), redirURL.length()); 
        
                    //int ptr = urlVar.getByRef();
                    //OS.MoveMemory(ptr, new 
                    //browser.navigate(redirURL);
 
                }
                */
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
				if (statusLineManager!=null) 
				   statusLineManager.getProgressMonitor().worked(worked);
			}
		});

		addEventListener(WebBrowser.StatusTextChange, new OleListener() {
			public void handleEvent(OleEvent event) {
				Variant newText = event.arguments[0];
				String msg = newText.getString();

				//if (webStatus!=null) {
					if (msg != null) {
						//webStatus.setText(msg);
						if (statusLineManager!=null) 
						   statusLineManager.setMessage(msg);
					}
					else {
						//webStatus.setText("");
						if (statusLineManager!=null) statusLineManager.setMessage("");
					}
				//}
			}
		});
	}
}