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

/**
 * Needed for the OLE implementation
 */
public class BrowserControlSite extends OleControlSite {
	public static final String HTML_ROOT = "html/";
	protected boolean beenBuilt = false;
	protected boolean startedDownload = false;
	// Web Browser
	private WebBrowser browser;
	private ProgressBar webProgress;
	private Label webStatus;
	
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

	/**
	 * BrowserControlSite constructor
	 */
	public BrowserControlSite(Composite parent, int style, String progId) {
		super(parent, style, progId);
		
		addEventListener(WebBrowser.DownloadBegin, new OleListener() {
			public void handleEvent(OleEvent event) {
				startedDownload = true;
				webProgress.setSelection(0);
			}
		});

		addEventListener(WebBrowser.DownloadComplete, new OleListener() {
			public void handleEvent(OleEvent event) {
				startedDownload = false;
				webProgress.setSelection(0);
			}
		});

		addEventListener(WebBrowser.BeforeNavigate2, new OleListener() {
			public void handleEvent(OleEvent event) {
				Variant urlVar = event.arguments[1];
                String strUrl = urlVar.getString();
                if (isUpdateURL(strUrl)) {
                	final String redirURL = createUpdatePage(strUrl);
                    Variant cancel = event.arguments[6];
                    int ptr = cancel.getByRef();
                    OS.MoveMemory(ptr, new int [] { 1 }, 4);
                    getDisplay().asyncExec(new Runnable() {
                    	public void run() {
                    		browser.navigate(redirURL);
                    	}
                    });
                    //int ptr = urlVar.getByRef();
                    //OS.MoveMemory(ptr, redirURL.getBytes(), redirURL.length()); 
        
                    //int ptr = urlVar.getByRef();
                    //OS.MoveMemory(ptr, new 
                    //browser.navigate(redirURL);
                }
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
				if (maxProgress.getInt()!=100)
				   webProgress.setMaximum(maxProgress.getInt());
				
				webProgress.setSelection(progress.getInt());
			}
		});

		addEventListener(WebBrowser.StatusTextChange, new OleListener() {
			public void handleEvent(OleEvent event) {
				Variant newText = event.arguments[0];
				String msg = newText.getString();

				if (webStatus!=null) {
					if (msg != null) webStatus.setText(msg);
					else webStatus.setText("");
				}
			}
		});
	}
	
	private boolean isUpdateURL(String url) {
		return url.startsWith("update://");
	}
	
	private String createUpdatePage(String url) {
		URL location = UpdateUIPlugin.getDefault().getDescriptor().getInstallURL();
		String realURL = url.substring(8);
		String loc = location.toString();
		if (loc.endsWith("/")) 
		   loc = loc.substring(0, loc.length()-1);
		return loc + "/" + HTML_ROOT + realURL;
	}
}