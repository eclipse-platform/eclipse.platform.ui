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
package org.eclipse.help.ui.internal.browser.embedded;
import java.net.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.ui.internal.*;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
/**
 * Application providing embeded Internet Explorer The controlling commands are
 * read from standard input Commands and their parameters are separated using
 * spaced and should be provided one command per line.
 */
public class EmbeddedBrowser {
	private static final String BROWSER_X = "browser.x";
	private static final String BROWSER_Y = "browser.y";
	private static final String BROWSER_WIDTH = "browser.w";
	private static final String BROWSER_HEIGTH = "browser.h";
	private static final String BROWSER_MAXIMIZED = "browser.maximized";
	private String windowTitle;
	private Image[] shellImages;
	Shell shell;
	Browser webBrowser;
	private Preferences store;
	int x, y, w, h;
	boolean firstopenning = true;
	/**
	 * Constructor
	 */
	public EmbeddedBrowser() {
		store = HelpUIPlugin.getDefault().getPluginPreferences();
		windowTitle = getWindowTitle();
		shellImages = createImages();
		createShell();
	}
	/**
	 * Create shell image
	 */
	private Image[] createImages() {
		String[] productImageURLs = getProductImageURLs();
		if (productImageURLs != null) {
			Image[] shellImgs = new Image[productImageURLs.length];
			for (int i = 0; i < productImageURLs.length; i++) {
				try {
					shellImgs[i] = ImageDescriptor.createFromURL(
							new URL(productImageURLs[i])).createImage();
				} catch (MalformedURLException mue) {
					if (!"".equals(productImageURLs[i])) {
						System.out.println("Invalid URL of product image.");
					}
				}
			}
			return shellImgs;
		}
		return new Image[0];
	}
	/**
	 * Creates hosting shell.
	 */
	private void createShell() {
		shell = new Shell();
		if (shellImages != null)
			shell.setImages(shellImages);
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				// save position
				store.setValue(BROWSER_X, Integer.toString(x));
				store.setValue(BROWSER_Y, Integer.toString(y));
				store.setValue(BROWSER_WIDTH, Integer.toString(w));
				store.setValue(BROWSER_HEIGTH, Integer.toString(h));
				store.setValue(BROWSER_MAXIMIZED, (new Boolean(shell
						.getMaximized()).toString()));
			}
		});
		shell.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {
				if (!shell.getMaximized()) {
					Point location = shell.getLocation();
					x = location.x;
					y = location.y;
				}
			}
			public void controlResized(ControlEvent e) {
				if (!shell.getMaximized()) {
					Point size = shell.getSize();
					w = size.x;
					h = size.y;
				}
			}
		});
		shell.setText(windowTitle);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		shell.setLayout(layout);
		webBrowser = new Browser(shell, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		webBrowser.setLayoutData(data);
		webBrowser.setUrl("about:blank");
		// use saved location and size
		x = store.getInt(BROWSER_X);
		y = store.getInt(BROWSER_Y);
		w = store.getInt(BROWSER_WIDTH);
		h = store.getInt(BROWSER_HEIGTH);
		if (w == 0 || h == 0) {
			// first launch, use default size
			w = 1024;
			h = 768;
		} else {
			// do not set location to 0,0 the first time
			shell.setLocation(x, y);
		}
		shell.setSize(w, h);
		if (store.getBoolean(BROWSER_MAXIMIZED))
			shell.setMaximized(true);
		webBrowser.addOpenWindowListener(new OpenWindowListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.browser.NewWindowListener#newWindow(org.eclipse.swt.browser.NewWindowEvent)
			 */
			public void open(WindowEvent event) {
				EmbeddedBrowserDialog dialog = new EmbeddedBrowserDialog(shell,
						windowTitle, createImages());
				event.browser = dialog.getBrowser();
			}
		});
		shell.open();
	}
	public void setLocation(int x, int y) {
		shell.setLocation(x, y);
	}
	public void setSize(int width, int height) {
		shell.setSize(width, height);
	}
	/**
	 * Closes the browser.
	 */
	public void close() {
		shell.dispose();
	}
	public void displayUrl(String url) {
		webBrowser.setUrl(url);
		makeVisible();
	}
	private void makeVisible() {
		if (firstopenning) {
			firstopenning = false;
		} else {
			shell.setVisible(false);
			shell.setMinimized(true);
		}
		shell.setVisible(true);
		shell.setMinimized(false);
		shell.moveAbove(null);
		shell.forceActive();
	}
	public boolean isDisposed() {
		return shell.isDisposed();
	}
	private String getWindowTitle() {
		if ("true".equalsIgnoreCase(HelpBasePlugin.getDefault()
				.getPluginPreferences().getString("windowTitlePrefix"))) {
			return HelpUIResources.getString("browserTitle", BaseHelpSystem
					.getProductName());
		} else {
			return BaseHelpSystem.getProductName();
		}
	}
	/**
	 * Obtains URL to product image
	 * 
	 * @return URL as String or null
	 */
	private String[] getProductImageURLs() {
		IProduct product = Platform.getProduct();
		if (product != null) {
			String url = product.getProperty("windowImages");
			if (url != null && url.length() > 0) {
				return url.split(",\\s*");
			}
			url = product.getProperty("windowImage");
			if (url != null && url.length() > 0) {
				return new String[]{url};
			}
		}
		return null;
	}
}