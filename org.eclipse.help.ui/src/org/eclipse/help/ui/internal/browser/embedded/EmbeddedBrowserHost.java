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
package org.eclipse.help.ui.internal.browser.embedded;
import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.core.boot.*;
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
public class EmbeddedBrowserHost implements Runnable {
	public static final String SYS_PROPERTY_DEBUG = "debug";
	public static final String SYS_PROPERTY_LOCALE = "locale";
	public static final String SYS_PROPERTY_INSTALLURL = "installURL";
	public static final String SYS_PROPERTY_PRODUCTIMAGEURL = "windowImage";
	public static final String SYS_PROPERTY_PRODUCTNAME = "name";
	public static final String SYS_PROPERTY_STATELOCATION = "stateLocation";
	public static final String CMD_CLOSE = "close";
	public static final String CMD_DISPLAY_URL = "displayURL";
	public static final String CMD_SET_LOCATION = "setLocation";
	public static final String CMD_SET_SIZE = "setSize";
	private static final String BROWSER_X = "browser.x";
	private static final String BROWSER_Y = "browser.y";
	private static final String BROWSER_WIDTH = "browser.w";
	private static final String BROWSER_HEIGTH = "browser.h";
	private static final String BROWSER_MAXIMIZED = "browser.maximized";
	public static boolean DEBUG = false;
	private static String locale;
	private static String installURL;
	private static String productName;
	private static String productImageURL;
	private static String stateLocation;
	private Display display;
	private Image shellImage;
	Shell shell;
	Browser webBrowser;
	private EmbeddedBrowserResources ieResources;
	EmbeddedBrowserStore store;
	int x, y, w, h;
	boolean closing = false;
	Thread inputReader;
	boolean firstopenning = true;
	/**
	 * Constructor used for launching in process embeded IE (for debugging)
	 */
	public EmbeddedBrowserHost(String installURL, String productImageURL) {
		if (HelpUIPlugin.DEBUG_EMBEDDED_BROWSER) {
			EmbeddedBrowserHost.DEBUG = true;
		}
		locale = BootLoader.getNL();
		EmbeddedBrowserHost.installURL = installURL;
		EmbeddedBrowserHost.productImageURL = productImageURL;
		productName = "IN-PROCESS DEBUG MODE";
		EmbeddedBrowserHost.stateLocation =
			HelpUIPlugin.getDefault().getStateLocation().toOSString();

		ieResources = new EmbeddedBrowserResources(locale, installURL);
		store = new EmbeddedBrowserStore(new File(stateLocation, ".iestore").toString());
		store.restore();
		shellImage = createImage();
		createShell();
	}
	/**
	 * Constructor
	 */
	private EmbeddedBrowserHost() {
		display = new Display();
		ieResources = new EmbeddedBrowserResources(locale, installURL);
		store = new EmbeddedBrowserStore(new File(stateLocation, ".iestore").toString());
		store.restore();
		shellImage = createImage();
		createShell();
		// Start command interpreter
		inputReader = new Thread(this);
		inputReader.setDaemon(true);
		inputReader.setName("IE Command Interpreter");
		inputReader.start();
	}
	/**
	 * Runs event loop for the display
	 */
	private void runUI() {
		while (!isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	/**
	 * Entry point to the program Command line arguments are not used.
	 */
	public static void main(String[] args) throws Throwable {
		String debug = System.getProperty(SYS_PROPERTY_DEBUG);
		if (debug != null && debug.length() >= 0) {
			EmbeddedBrowserHost.DEBUG = true;
		}
		locale = System.getProperty(SYS_PROPERTY_LOCALE);
		if (locale == null || locale.length() <= 0) {
			System.err.println(
				"Property " + SYS_PROPERTY_LOCALE + " not set, using default.");
			locale = Locale.getDefault().toString();
		}
		installURL = System.getProperty(SYS_PROPERTY_INSTALLURL);
		if (installURL == null || installURL.length() <= 0) {
			System.err.println(
				"Property " + SYS_PROPERTY_INSTALLURL + " must be set.");
			return;
		}
		productImageURL = System.getProperty(SYS_PROPERTY_PRODUCTIMAGEURL);
		if (productImageURL == null) {
			System.err.println(
				"Property " + SYS_PROPERTY_PRODUCTIMAGEURL + " must be set.");
			return;
		}
		productName = System.getProperty(SYS_PROPERTY_PRODUCTNAME);
		if (productName == null) {
			System.err.println(
				"Property " + SYS_PROPERTY_PRODUCTNAME + " must be set.");
			return;
		}
		stateLocation = System.getProperty(SYS_PROPERTY_STATELOCATION);
		if (stateLocation == null || stateLocation.length() <= 0) {
			System.err.println(
				"Property " + SYS_PROPERTY_STATELOCATION + " must be set.");
			return;
		}
		EmbeddedBrowserHost ie = new EmbeddedBrowserHost();
		ie.runUI();
		ie.dispose();
	}
	/**
	 * Create shell image
	 */
	private Image createImage() {
		Image shellImg = null;
		try {
			shellImg =
				ImageDescriptor
					.createFromURL(new URL(productImageURL))
					.createImage();
		} catch (MalformedURLException mue) {
			if (!"".equals(productImageURL)) {
				System.out.println("Invalid URL of product image.");
			}
		}
		return shellImg;
	}
	/**
	 * Disposes of resources
	 */
	private void dispose() {
		if (HelpUIPlugin.DEBUG_EMBEDDED_BROWSER_IN_PROCESS) {
			shell.dispose();
		} else {
			display.dispose();
		}
	}

	/**
	 * Creates hosting shell.
	 */
	private void createShell() {
		shell = new Shell();
		if (shellImage != null)
			shell.setImage(shellImage);
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				closing = true;
				// save preferences
				store.put(BROWSER_X, Integer.toString(x));
				store.put(BROWSER_Y, Integer.toString(y));
				store.put(BROWSER_WIDTH, Integer.toString(w));
				store.put(BROWSER_HEIGTH, Integer.toString(h));
				store.put(
					BROWSER_MAXIMIZED,
					(new Boolean(shell.getMaximized()).toString()));
				store.save();
				if (!HelpUIPlugin.DEBUG_EMBEDDED_BROWSER_IN_PROCESS) {
					// wait for command thread to stop
					if (inputReader.isAlive()
						&& !HelpUIPlugin.DEBUG_EMBEDDED_BROWSER_IN_PROCESS) {
						try {
							inputReader.join(1000);
						} catch (InterruptedException ie) {
						}
					}
				}
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
		shell.setText(productName);
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
			w = 800;
			h = 600;
		} else {
			// do not set location to 0,0 the first time
			shell.setLocation(x, y);
		}
		shell.setSize(w, h);
		if (store.getBoolean(BROWSER_MAXIMIZED))
			shell.setMaximized(true);
		webBrowser.addNewWindowListener(new NewWindowListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.browser.NewWindowListener#newWindow(org.eclipse.swt.browser.NewWindowEvent)
			 */
			public void newWindow(NewWindowEvent event) {
				int dw=300;
				int dh=300;
				int dx=x+(w-dw)/2;
				int dy=y+(h-dh)/2;
				if(dy>50) dy-=50;
				EmbeddedBrowserDialog workingSetManagerDialog =
					new EmbeddedBrowserDialog(
						shell,
						productName,
						createImage(),
						dx,
						dy,
						dw,
						dh);
				event.browser = workingSetManagerDialog.getBrowser();

			}
		});
		shell.open();
	}
	/**
	 * Reads commands from standard input.
	 */
	public void run() {
		BufferedReader reader =
			new BufferedReader((new InputStreamReader(System.in)));
		// Run command loop
		String line;
		try {
			while (!closing && null != (line = reader.readLine())) {
				if (line.length() > 0) {
					executeCommand(line);
					// The inner loop is required, otherwise shell stops
					// responding after right-click->properties.
					// This workaround has side effect of preventing
					// help from openin url second time in J9
					// as System.in.available() <=0 always.
					while (!closing && System.in.available() <= 0) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException ie) {
						}
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			System.err.println(EmbeddedBrowserResources.getString("WE026", e.getMessage()));
			return;
		}
	}
	/**
	 * Command Inerpreter for commands. Commands are passed on stdin one per
	 * line Supported commands: close displayURL <url>setLocation <x><y>
	 * setSize <width><height>
	 */
	public void executeCommand(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line);
		if (!tokenizer.hasMoreTokens())
			return;
		String command = tokenizer.nextToken();
		String pars[] = new String[tokenizer.countTokens()];
		for (int i = 0; i < pars.length; i++) {
			pars[i] = tokenizer.nextToken();
		}
		if (CMD_DISPLAY_URL.equalsIgnoreCase(command)) {
			if (pars.length >= 1 && pars[0] != null) {
				if (HelpUIPlugin.DEBUG_EMBEDDED_BROWSER_IN_PROCESS) {
					webBrowser.setUrl(pars[0]);
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
					return;
				}
				display.syncExec(new DisplayURLCommand(pars[0]));
				display.syncExec(new MakeVisible());
			}
		} else if (CMD_SET_LOCATION.equalsIgnoreCase(command)) {
			if (pars.length >= 2 && pars[0] != null && pars[1] != null) {
				try {
					display.syncExec(
						new SetLocationCommand(
							Integer.parseInt(pars[0]),
							Integer.parseInt(pars[1])));
				} catch (NumberFormatException nfe) {
				}
			}
		} else if (CMD_SET_SIZE.equalsIgnoreCase(command)) {
			if (pars.length >= 2 && pars[0] != null && pars[1] != null) {
				try {
					display.syncExec(
						new SetSizeCommand(
							Integer.parseInt(pars[0]),
							Integer.parseInt(pars[1])));
				} catch (NumberFormatException nfe) {
				}
			}
		} else if (CMD_CLOSE.equalsIgnoreCase(command)) {
			if (HelpUIPlugin.DEBUG_EMBEDDED_BROWSER_IN_PROCESS) {
				shell.dispose();
				return;
			}
			display.syncExec(new CloseCommand());
			return;
		} else {
			System.err.println(EmbeddedBrowserResources.getString("WE028", command));
		}
	}
	class SetLocationCommand implements Runnable {
		int newX, newY;
		public SetLocationCommand(int x, int y) {
			this.newX = x;
			this.newY = y;
		}
		public void run() {
			shell.setLocation(newX, newY);
		}
	}
	class SetSizeCommand implements Runnable {
		int newX, newY;
		public SetSizeCommand(int x, int y) {
			this.newX = x;
			this.newY = y;
		}
		public void run() {
			shell.setSize(newX, newY);
		}
	}
	class CloseCommand implements Runnable {
		public void run() {
			shell.dispose();
		}
	}
	class DisplayURLCommand implements Runnable {
		String url;
		public DisplayURLCommand(String url) {
			this.url = url;
		}
		public void run() {
			webBrowser.setUrl(url);
		}
	}
	class MakeVisible implements Runnable {
		public void run() {
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
	}
	public boolean isDisposed() {
		return shell.isDisposed();
	}
}
