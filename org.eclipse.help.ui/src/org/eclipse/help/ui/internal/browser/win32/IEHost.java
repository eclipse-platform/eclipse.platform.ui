/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser.win32;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

import org.eclipse.help.internal.ui.util.HelpWorkbenchException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
/**
 * Application providing embeded Internet Explorer
 * The controlling commands are read from standard input
 * Commands and their parameters are separated using spaced
 * and should be provided one command per line.
 */
public class IEHost implements Runnable, ICommandStateChangedListener {
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
	private static String installURL;
	private static String productName;
	private static String productImageURL;
	private static String stateLocation;
	private Display display;
	private Image shellImg, backImg, forwardImg;
	private Shell shell;
	private WebBrowser webBrowser;
	private IEResources ieResources;
	private IEStore store;
	boolean opened = false;
	ToolItem backItem, forwardItem;
	int x, y, w, h;
	/**
	 * Constructor
	 */
	private IEHost() {
		display = new Display();
		ieResources = new IEResources(installURL);
		store = new IEStore(new File(stateLocation, ".iestore").toString());
		store.restore();
		createImages();
		createShell();
		// Start command interpreter
		Thread inputReader = new Thread(this);
		inputReader.setDaemon(true);
		inputReader.setName("IE Command Interpreter");
		inputReader.start();
	}
	/**
	 * Runs event loop for the display
	 */
	private void runUI() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	/**
	 * Entry point to the program
	 * Command line arguments are not used.
	 */
	public static void main(String[] args) throws Throwable {
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
		IEHost ie = new IEHost();
		ie.runUI();
	}
	/**
	 * Creates the toolbar images
	 */
	private void createImages() {
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
		backImg =
			ImageDescriptor
				.createFromURL(ieResources.getImagePath("back_icon"))
				.createImage();
		forwardImg =
			ImageDescriptor
				.createFromURL(ieResources.getImagePath("forward_icon"))
				.createImage();
	}

	/**
	 * Creates hosting shell.
	 */
	private void createShell() {
		shell = new Shell();
		if (shellImg != null)
			shell.setImage(shellImg);
		shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				store.put(BROWSER_X, Integer.toString(x));
				store.put(BROWSER_Y, Integer.toString(y));
				store.put(BROWSER_WIDTH, Integer.toString(w));
				store.put(BROWSER_HEIGTH, Integer.toString(h));
				store.put(
					BROWSER_MAXIMIZED,
					(new Boolean(shell.getMaximized()).toString()));
				store.save();
				if (shellImg != null) {
					shellImg.dispose();
				}
				if (backImg != null) {
					backImg.dispose();
				}
				if (forwardImg != null) {
					forwardImg.dispose();
				}
				shell.close();
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
		shell.setText(ieResources.getString("browserTitle", productName));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		shell.setLayout(layout);
		createContents(shell);
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
		shell.open();
		opened = true;
	}
	/**
	 * Populates shell control with toolbar and ActiveX IE.
	 */
	private Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		composite.setLayout(layout);
		// Add a toolbar
		ToolBar bar = new ToolBar(composite, SWT.FLAT | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		//gridData.horizontalSpan = 3;
		bar.setLayoutData(gridData);
		// Add a button to navigate back
		backItem = new ToolItem(bar, SWT.HORIZONTAL, 0);
		//backItem.setText(ieResources.getString("back"));
		backItem.setToolTipText(ieResources.getString("back_tip"));
		backItem.setImage(backImg);
		backItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				webBrowser.back();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		// Add a button to navigate forward
		forwardItem = new ToolItem(bar, SWT.NONE, 1);
		//forwardItem.setText(ieResources.getString("forward"));
		forwardItem.setToolTipText(ieResources.getString("forward_tip"));
		forwardItem.setImage(forwardImg);
		forwardItem.setHotImage(null);
		forwardItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				webBrowser.forward();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		try {
			webBrowser = new WebBrowser(composite);
			webBrowser.addCommandStateChangedListener(this);
			webBrowser.navigate("about:blank");
		} catch (HelpWorkbenchException hwe) {
			System.err.println(
				ieResources.getString("WE027", hwe.getMessage()));
		}
		return composite;
	}
	public void commandStateChanged(boolean back, boolean forward) {
		if (backItem.getEnabled() != back)
			backItem.setEnabled(back);
		if (forwardItem.getEnabled() != forward)
			forwardItem.setEnabled(forward);
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
			while (null != (line = reader.readLine())) {
				if (line.length() > 0) {
					executeCommand(line);
					// this is required, otherwise shell stops responding
					while (System.in.available() <= 0) {
						try {
							Thread.currentThread().sleep(30);
						} catch (InterruptedException ie) {
						}
					}
				}
			}
		} catch (IOException e) {
			System.err.println(ieResources.getString("WE026", e.getMessage()));
			return;
		}
	}
	/**
	 * Command Inerpreter for commands.
	 * Commands are passed on stdin one per line
	 * Supported commands:
	 * 	 close
	 *   displayURL <url>
	 *   setLocation <x> <y>
	 *   setSize <width> <height>
	 */
	private void executeCommand(String line) {
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
			display.syncExec(new CloseCommand());
			return;
		} else {
			System.err.println(ieResources.getString("WE028", command));
		}
	}
	class SetLocationCommand implements Runnable {
		int x, y;
		public SetLocationCommand(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public void run() {
			shell.setLocation(x, y);
		}
	}
	class SetSizeCommand implements Runnable {
		int x, y;
		public SetSizeCommand(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public void run() {
			shell.setSize(x, y);
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
			webBrowser.navigate(url);
		}
	}
	class MakeVisible implements Runnable {
		public void run() {
			shell.setVisible(true);
			shell.setMinimized(false);
			shell.moveAbove(null);
			shell.forceActive();
		}
	}
}