/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * <code>UITestCase</code> is a useful super class for most
 * UI tests cases.  It contains methods to create new windows
 * and pages.  It will also automatically close the test
 * windows when the tearDown method is called.
 */
public abstract class UITestCase extends TestCase {

	/**
	 * Returns the workbench page input to use for newly created windows.
	 *
	 * @return the page input to use for newly created windows
	 * @since 3.1
	 */
	public static IAdaptable getPageInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	class TestWindowListener implements IWindowListener {
        private boolean enabled = true;

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
		public void windowActivated(IWorkbenchWindow window) {
            // do nothing
        }

        @Override
		public void windowDeactivated(IWorkbenchWindow window) {
            // do nothing
        }

        @Override
		public void windowClosed(IWorkbenchWindow window) {
            if (enabled)
                testWindows.remove(window);
        }

        @Override
		public void windowOpened(IWorkbenchWindow window) {
            if (enabled)
                testWindows.add(window);
        }
    }

    protected IWorkbench fWorkbench;

    private List<IWorkbenchWindow> testWindows;

    private TestWindowListener windowListener;

    public UITestCase(String testName) {
        super(testName);
        //		ErrorDialog.NO_UI = true;
        testWindows = new ArrayList<IWorkbenchWindow>(3);
    }

	/**
	 * Fails the test due to the given throwable.
	 */
	public static void fail(String message, Throwable e) {
		// If the exception is a CoreException with a multistatus
		// then print out the multistatus so we can see all the info.
		if (e instanceof CoreException) {
			IStatus status = ((CoreException) e).getStatus();
			write(status, 0);
		} else
			e.printStackTrace();
		fail(message + ": " + e);
	}

	private static void indent(OutputStream output, int indent) {
		for (int i = 0; i < indent; i++)
			try {
				output.write("\t".getBytes());
			} catch (IOException e) {
				// ignore
			}
	}

	private static void write(IStatus status, int indent) {
		PrintStream output = System.out;
		indent(output, indent);
		output.println("Severity: " + status.getSeverity());

		indent(output, indent);
		output.println("Plugin ID: " + status.getPlugin());

		indent(output, indent);
		output.println("Code: " + status.getCode());

		indent(output, indent);
		output.println("Message: " + status.getMessage());

		if (status.getException() != null) {
			indent(output, indent);
			output.print("Exception: ");
			status.getException().printStackTrace(output);
		}

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++)
				write(children[i], indent + 1);
		}
	}

	/**
     * Adds a window listener to the workbench to keep track of
     * opened test windows.
     */
    private void addWindowListener() {
        windowListener = new TestWindowListener();
        fWorkbench.addWindowListener(windowListener);
    }

    /**
     * Removes the listener added by <code>addWindowListener</code>.
     */
    private void removeWindowListener() {
        if (windowListener != null) {
            fWorkbench.removeWindowListener(windowListener);
        }
    }

    /**
     * Outputs a trace message to the trace output device, if enabled.
     * By default, trace messages are sent to <code>System.out</code>.
     *
     * @param msg the trace message
     */
    protected void trace(String msg) {
        System.out.println(msg);
    }

    /**
     * Simple implementation of setUp. Subclasses are prevented
     * from overriding this method to maintain logging consistency.
     * doSetUp() should be overriden instead.
     */
    @Override
	protected final void setUp() throws Exception {
    	super.setUp();
		fWorkbench = PlatformUI.getWorkbench();
    	trace("----- " + this.getName()); //$NON-NLS-1$
        trace(this.getName() + ": setUp..."); //$NON-NLS-1$
        addWindowListener();
        doSetUp();

    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     * The default implementation does nothing.
     * Subclasses may extend.
     */
    protected void doSetUp() throws Exception {
        // do nothing.
    }

    /**
     * Simple implementation of tearDown.  Subclasses are prevented
     * from overriding this method to maintain logging consistency.
     * doTearDown() should be overriden instead.
     */
    @Override
	protected final void tearDown() throws Exception {
        super.tearDown();
        trace(this.getName() + ": tearDown...\n"); //$NON-NLS-1$
        removeWindowListener();
        doTearDown();
    	fWorkbench = null;
    }

    /**
     * Tears down the fixture, for example, close a network connection.
     * This method is called after a test is executed.
     * The default implementation closes all test windows, processing events both before
     * and after doing so.
     * Subclasses may extend.
     */
    protected void doTearDown() throws Exception {
        processEvents();
        closeAllTestWindows();
        processEvents();
    }

    protected static void processEvents() {
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (display != null)
            while (display.readAndDispatch())
                ;
    }

    protected static interface Condition {
    	public boolean compute();
    }

	/**
	 *
	 * @param condition
	 *            , or null if this should only wait
	 * @param timeout
	 *            , -1 if forever
	 * @return true if successful, false if time out or interrupted
	 */
	protected boolean processEventsUntil(Condition condition, long timeout) {
		long startTime = System.currentTimeMillis();
		Display display = getWorkbench().getDisplay();
		while (condition == null || !condition.compute()) {
			if (timeout != -1
					&& System.currentTimeMillis() - startTime > timeout) {
				return false;
			}
			while (display.readAndDispatch())
				;
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
		}
		return true;
	}

    /**
     * Open a test window with the empty perspective.
     */
    public IWorkbenchWindow openTestWindow() {
        return openTestWindow(EmptyPerspective.PERSP_ID);
    }

    /**
     * Open a test window with the provided perspective.
     */
    public IWorkbenchWindow openTestWindow(String perspectiveId) {
		try {
			IWorkbenchWindow window = fWorkbench.openWorkbenchWindow(
					perspectiveId, getPageInput());
			waitOnShell(window.getShell());
			return window;
		} catch (WorkbenchException e) {
			fail("Problem opening test window", e);
			return null;
		}
	}

    /**
	 * Try and process events until the new shell is the active shell. This may
	 * never happen, so time out after a suitable period.
	 *
	 * @param shell
	 *            the shell to wait on
	 * @since 3.2
	 */
	private void waitOnShell(Shell shell) {

		processEvents();
//		long endTime = System.currentTimeMillis() + 5000;
//
//		while (shell.getDisplay().getActiveShell() != shell
//				&& System.currentTimeMillis() < endTime) {
//			processEvents();
//		}
	}

	/**
	 * Close all test windows.
	 */
    public void closeAllTestWindows() {
		for (IWorkbenchWindow testWindows : testWindows) {
            testWindows.close();
        }
        testWindows.clear();
    }

    /**
     * Open a test page with the empty perspective in a window.
     */
    public IWorkbenchPage openTestPage(IWorkbenchWindow win) {
        IWorkbenchPage[] pages = openTestPage(win, 1);
        if (pages != null) {
            return pages[0];
        }
        return null;
    }

    /**
     * Open "n" test pages with the empty perspective in a window.
     */
    public IWorkbenchPage[] openTestPage(IWorkbenchWindow win, int pageTotal) {
        try {
            IWorkbenchPage[] pages = new IWorkbenchPage[pageTotal];
            IAdaptable input = getPageInput();

            for (int i = 0; i < pageTotal; i++) {
                pages[i] = win.openPage(EmptyPerspective.PERSP_ID, input);
            }
            return pages;
        } catch (WorkbenchException e) {
        	fail("Problem opening test page", e);
            return null;
        }
    }

    /**
     * Close all pages within a window.
     */
    public void closeAllPages(IWorkbenchWindow window) {
        IWorkbenchPage[] pages = window.getPages();
        for (int i = 0; i < pages.length; i++)
            pages[i].close();
    }

    /**
     * Set whether the window listener will manage opening and closing of created windows.
     */
    protected void manageWindows(boolean manage) {
        windowListener.setEnabled(manage);
    }

    /**
     * Returns the workbench.
     *
     * @return the workbench
     * @since 3.1
     */
    protected IWorkbench getWorkbench() {
        return fWorkbench;
    }
}
