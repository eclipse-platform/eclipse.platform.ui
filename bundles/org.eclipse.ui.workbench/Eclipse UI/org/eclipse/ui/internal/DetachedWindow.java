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

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;
import org.eclipse.ui.help.WorkbenchHelp;

public class DetachedWindow extends Window {

    private ViewStack folder;

    private WorkbenchPage page;

    //Keep the state of a DetachedWindow when switching perspectives.
    private String title;

    private Rectangle bounds;

    /**
     * Create a new FloatingWindow.
     */
    public DetachedWindow(WorkbenchPage workbenchPage) {
        super(workbenchPage.getWorkbenchWindow().getShell());
        setShellStyle( //SWT.CLOSE | SWT.MIN | SWT.MAX | 
        SWT.RESIZE);
        this.page = workbenchPage;
        folder = new ViewStack(page, false);
    }

    /**
     * Adds a visual part to this window.
     * Supports reparenting.
     */
    public void add(ViewPane part) {

        Shell shell = getShell();
        if (shell != null)
            part.reparent(shell);
        folder.add(part);
    }

    public boolean belongsToWorkbenchPage(IWorkbenchPage workbenchPage) {
        return (this.page == workbenchPage);
    }

    /**
     * Closes this window and disposes its shell.
     */
    public boolean close() {
        Shell s = getShell();
        if (s != null) {
            title = s.getText();
            bounds = s.getBounds();
        }

        if (folder != null)
            folder.dispose();

        // Unregister this detached view as a window (for key bindings).
        final IWorkbenchContextSupport contextSupport = getWorkbenchPage()
                .getWorkbenchWindow().getWorkbench().getContextSupport();
        contextSupport.unregisterShell(s);

        return super.close();
    }

    /**
     * Answer a list of the view panes.
     */
    private void collectViewPanes(List result, LayoutPart[] parts) {
        for (int i = 0, length = parts.length; i < length; i++) {
            LayoutPart part = parts[i];
            if (part instanceof ViewPane) {
                result.add(part);
            }
        }
    }

    /**
     * This method will be called to initialize the given Shell's layout
     */
    protected void configureShell(Shell shell) {
        if (title != null)
            shell.setText(title);
        shell.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                Shell shell = (Shell) event.widget;
                folder.setBounds(shell.getClientArea());
            }
        });

        // Register this detached view as a window (for key bindings).
        final IWorkbenchContextSupport contextSupport = getWorkbenchPage()
                .getWorkbenchWindow().getWorkbench().getContextSupport();
        contextSupport.registerShell(shell,
                IWorkbenchContextSupport.TYPE_WINDOW);

        WorkbenchHelp.setHelp(shell, IWorkbenchHelpContextIds.DETACHED_WINDOW);
    }

    /**
     * Override this method to create the widget tree that is used as the window's contents.
     */
    protected Control createContents(Composite parent) {
        // Create the tab folder.
        folder.createControl(parent);

        // Reparent each view in the tab folder.
        Vector detachedChildren = new Vector();
        collectViewPanes(detachedChildren, getChildren());
        Enumeration enum = detachedChildren.elements();
        while (enum.hasMoreElements()) {
            LayoutPart part = (LayoutPart) enum.nextElement();
            part.reparent(parent);
        }

        // Return tab folder control.
        return folder.getControl();
    }

    public LayoutPart[] getChildren() {
        return folder.getChildren();
    }

    public WorkbenchPage getWorkbenchPage() {
        return this.page;
    }

    /**
     * Close has been pressed.  Close all views.
     */
    protected void handleShellCloseEvent() {
        List views = new ArrayList();
        collectViewPanes(views, getChildren());
        Iterator enum = views.iterator();
        while (enum.hasNext()) {
            ViewPane child = (ViewPane) enum.next();
            page.hideView(child.getViewReference());
        }
        close();
    }

    protected void initializeBounds() {
        if (bounds != null)
            getShell().setBounds(bounds);
        else
            super.initializeBounds();
    }

    /**
     * @see IPersistablePart
     */
    public void restoreState(IMemento memento) {
        // Read the title.
        title = memento.getString(IWorkbenchConstants.TAG_TITLE);

        // Read the bounds.
        Integer bigInt;
        bigInt = memento.getInteger(IWorkbenchConstants.TAG_X);
        int x = bigInt.intValue();
        bigInt = memento.getInteger(IWorkbenchConstants.TAG_Y);
        int y = bigInt.intValue();
        bigInt = memento.getInteger(IWorkbenchConstants.TAG_WIDTH);
        int width = bigInt.intValue();
        bigInt = memento.getInteger(IWorkbenchConstants.TAG_HEIGHT);
        int height = bigInt.intValue();

        // Set the bounds.
        bounds = new Rectangle(x, y, width, height);
        if (getShell() != null) {
            getShell().setText(title);
            getShell().setBounds(bounds);
        }

        // Create the folder.
        IMemento childMem = memento.getChild(IWorkbenchConstants.TAG_FOLDER);
        if (childMem != null)
            folder.restoreState(childMem);
    }

    /**
     * @see IPersistablePart
     */
    public void saveState(IMemento memento) {
        if (getShell() != null) {
            title = getShell().getText();
            bounds = getShell().getBounds();
        }
        // Save the title.
        memento.putString(IWorkbenchConstants.TAG_TITLE, title);

        // Save the bounds.
        memento.putInteger(IWorkbenchConstants.TAG_X, bounds.x);
        memento.putInteger(IWorkbenchConstants.TAG_Y, bounds.y);
        memento.putInteger(IWorkbenchConstants.TAG_WIDTH, bounds.width);
        memento.putInteger(IWorkbenchConstants.TAG_HEIGHT, bounds.height);

        // Save the views.	
        IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_FOLDER);
        folder.saveState(childMem);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.IWorkbenchDragDropPart#getControl()
     */
    public Control getControl() {
        return folder.getControl();
    }

    /**
     * 
     * Returns true iff the given rectangle is located in the client area of any
     * monitor.
     * 
     * @param someRectangle a rectangle in display coordinates (not null)
     * @return true iff the given point can be seen on any monitor
     */
    private static boolean intersectsAnyMonitor(Display display,
            Rectangle someRectangle) {
        Monitor[] monitors = display.getMonitors();

        for (int idx = 0; idx < monitors.length; idx++) {
            Monitor mon = monitors[idx];

            if (mon.getClientArea().intersects(someRectangle)) {
                return true;
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#getConstrainedShellSize(org.eclipse.swt.graphics.Rectangle)
     */
    protected Rectangle getConstrainedShellBounds(Rectangle preferredSize) {
        // As long as the initial position is somewhere on the display, don't mess with it.
        if (intersectsAnyMonitor(getShell().getDisplay(), preferredSize)) {
            return preferredSize;
        }

        return super.getConstrainedShellBounds(preferredSize);
    }
}