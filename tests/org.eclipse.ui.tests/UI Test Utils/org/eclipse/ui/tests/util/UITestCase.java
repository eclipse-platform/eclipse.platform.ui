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
package org.eclipse.ui.tests.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Display;
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
    class TestWindowListener implements IWindowListener {
        private boolean enabled = true;

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void windowActivated(IWorkbenchWindow window) {
            // do nothing
        }

        public void windowDeactivated(IWorkbenchWindow window) {
            // do nothing
        }

        public void windowClosed(IWorkbenchWindow window) {
            if (enabled)
                testWindows.remove(window);
        }

        public void windowOpened(IWorkbenchWindow window) {
            if (enabled)
                testWindows.add(window);
        }
    }

    protected IWorkbench fWorkbench;

    private List testWindows;

    private TestWindowListener windowListener;

    public UITestCase(String testName) {
        super(testName);
        //		ErrorDialog.NO_UI = true;
        fWorkbench = PlatformUI.getWorkbench();
        testWindows = new ArrayList(3);
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
        System.err.println(msg);
    }

    /**
     * Simple implementation of setUp. Subclasses are prevented 
     * from overriding this method to maintain logging consistency.
     * doSetUp() should be overriden instead.
     */
    protected final void setUp() throws Exception {
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
    protected final void tearDown() throws Exception {
        trace(this.getName() + ": tearDown...\n"); //$NON-NLS-1$
        removeWindowListener();
        doTearDown();
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

    protected void processEvents() {
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (display != null)
            while (display.readAndDispatch())
                ;
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
            return fWorkbench.openWorkbenchWindow(perspectiveId,
                    ResourcesPlugin.getWorkspace());
        } catch (WorkbenchException e) {
            fail();
            return null;
        }
    }

    /**
     * Close all test windows.
     */
    public void closeAllTestWindows() {
        Iterator iter = new ArrayList(testWindows).iterator();
        while (iter.hasNext()) {
            IWorkbenchWindow win = (IWorkbenchWindow) iter.next();
            win.close();
        }
        testWindows.clear();
    }

    /**
     * Open a test page with the empty perspective in a window.
     */
    public IWorkbenchPage openTestPage(IWorkbenchWindow win) {
        IWorkbenchPage[] pages = openTestPage(win, 1);
        if (pages != null)
            return pages[0];
        else
            return null;
    }

    /**
     * Open "n" test pages with the empty perspectie in a window.
     */
    public IWorkbenchPage[] openTestPage(IWorkbenchWindow win, int pageTotal) {
        try {
            IWorkbenchPage[] pages = new IWorkbenchPage[pageTotal];
            IWorkspace work = ResourcesPlugin.getWorkspace();

            for (int i = 0; i < pageTotal; i++) {
                pages[i] = win.openPage(EmptyPerspective.PERSP_ID, work);
            }
            return pages;
        } catch (WorkbenchException e) {
            fail();
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
}