package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * A SplashManager can be used to run a SplashWindow in a background thread.
 * To use it just get the manager and call openWindow().  This will create a new thread.
 * The thread will create a Display and a SplashWindow in that Display.  
 * Close the SplashWindow by calling closeWindow().
 *
 * Warning: while the SplashWindow is open do not try to create or access a Display 
 * from another thread.  This will cause an SWTError will occur.
 */
public class SplashManager implements Runnable {
	private static SplashManager singleton;
	private ProductInfo productInfo;
	private boolean stop = false;
	private boolean running = false;
/**
 * Constructs a new manager.
 */
public SplashManager() {
	super();
}
/**
 * Closes the splash window and returns after the window and display
 * have been disposed.  
 */
public void closeWindow() {
	stop = true;
	while (running) {
		Thread.yield();
	}
}
/**
 * Opens the splash window and returns immediately.  To close the
 * window call closeWindow().  Between these two calls the caller may 
 * execute any startup code as long as it does not try to create a new Display.  
 */
public void openWindow(ProductInfo info) {
	stop = false;
	productInfo = info;
	Thread thread = new Thread(this);
	thread.start();
}
/**
 * Open the splash window and run an event loop until a
 * 'stop' request occurs.   Then close the window and cleanup.
 */
public void run() {
	// Create a display for this thread.  Note: there can only be one at a time.
	running = true;
	Display display = Display.getDefault();

	// Create image.
	Image img = productInfo.getSplashImage();
	if (img == null) {
		display.dispose();
		running = false;
		return;
	}
	
	// Open shell.
	SplashWindow splash = new SplashWindow("", img);
	splash.open();
	while (!stop) {
		display.readAndDispatch();
		Thread.yield();
	}

	// Close all.
	splash.close();
	display.dispose();
	running = false;
}
}
