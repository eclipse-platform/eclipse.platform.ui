/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Provides part selection tracking for a part with a specific id
 * in all pages of a specific workbench window. This tracker shields
 * clients from a part opening and closing, and still provides selection
 * notification/information even when the part is not active.
 */
public class WindowPartSelectionTracker extends AbstractPartSelectionTracker
        implements IPageListener {
    /**
     * The window this selection tracker is working in
     */
    private IWorkbenchWindow fWindow;

    /**
     * Part selection listener.
     */
    private final INullSelectionListener selListener = new INullSelectionListener() {
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            fireSelection(part, selection);
        }
    };

    /**
     * Part post selection listener
     */
    private final INullSelectionListener postSelListener = new INullSelectionListener() {
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            firePostSelection(part, selection);
        }
    };

    /**
     * Constructs a new selection tracker for the given window and part id.
     * 
     * @param window workbench window
     * @param partId part identifier
     */
    public WindowPartSelectionTracker(IWorkbenchWindow window, String partId) {
        super(partId);
        setWindow(window);
        window.addPageListener(this);
        IWorkbenchPage[] pages = window.getPages();
        for (int i = 0; i < pages.length; i++) {
            pageOpened(pages[i]);
        }
    }

    /*
     * @see IPageListener#pageActivated(IWorkbenchPage)
     */
    public void pageActivated(IWorkbenchPage page) {
    }

    /*
     * @see IPageListener#pageClosed(IWorkbenchPage)
     */
    public void pageClosed(IWorkbenchPage page) {
        page.removeSelectionListener(getPartId(), selListener);
        page.removePostSelectionListener(getPartId(), postSelListener);
    }

    /*
     * @see IPageListener#pageOpened(IWorkbenchPage)
     */
    public void pageOpened(IWorkbenchPage page) {
        page.addSelectionListener(getPartId(), selListener);
        page.addPostSelectionListener(getPartId(), postSelListener);
    }

    /**
     * Sets the window this tracker is working in.
     * 
     * @param window workbench window
     */
    private void setWindow(IWorkbenchWindow window) {
        fWindow = window;
    }

    /**
     * Returns the window this tracker is working in.
     * 
     * @return workbench window
     */
    protected IWorkbenchWindow getWindow() {
        return fWindow;
    }

    /**
     * @see AbstractPartSelectionTracker#dispose()
     */
    public void dispose() {
        super.dispose();
        fWindow = null;
    }

    /*
     * @see AbstractPartSelectionTracker#getSelection()
     */
    public ISelection getSelection() {
        IWorkbenchPage page = getWindow().getActivePage();
        if (page != null) {
            return page.getSelection(getPartId());
        }
        return null;
    }
}
