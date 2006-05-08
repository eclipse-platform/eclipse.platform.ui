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

import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Provides debug view selection management/notification for
 * a debug view in a specific workbench page. This selection
 * provider sheilds clients from a debug view openning and closing,
 * and still provides selection notification/information even
 * when the debug view is not the active part.
 */
public class PagePartSelectionTracker extends AbstractPartSelectionTracker
        implements IPartListener, ISelectionChangedListener {

    /**
     * The workbench page for which this is tracking selection.
     */
    private IWorkbenchPage fPage;

    /**
     * The part in this tracker's page, or <code>null</code> if one is not open.
     */
    private IWorkbenchPart fPart;

    private ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            fireSelection(getPart(), event.getSelection());
        }
    };

    private ISelectionChangedListener postSelectionListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
            firePostSelection(getPart(), event.getSelection());
        }
    };

    public PagePartSelectionTracker(IWorkbenchPage page, String partId) {
        super(partId);
        setPage(page);
        page.addPartListener(this);
        IViewPart part = page.findView(partId);
        if (part != null) {
            setPart(part, false);
        }
    }

    /**
     * Disposes this selection provider - removes all listeners
     * currently registered.
     */
    public void dispose() {
        setPart(null, false);
        setPage(null);
        super.dispose();
    }

    /*
     * @see IPartListener#partActivated(IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part) {
    }

    /*
     * @see IPartListener#partBroughtToTop(IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part) {
    }

    /**
     * @see IPartListener#partClosed(IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part) {
        if (getPartId(part).equals(getPartId())) {
            setPart(null, true);
        }
    }

    /*
     * @see IPartListener#partDeactivated(IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part) {
    }

    /**
     * @see IPartListener#partOpened(IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part) {
        if (getPartId(part).equals(getPartId())) {
            setPart(part, true);
        }
    }

    /**
     * Returns the id for the given part, taking into account
     * multi-view instances which may have a secondary id.
     * 
     * @since 3.0
     */
    private Object getPartId(IWorkbenchPart part) {
        String id = part.getSite().getId();
        if (part instanceof IViewPart) {
            String secondaryId = ((IViewPart) part).getViewSite()
                    .getSecondaryId();
            if (secondaryId != null) {
                id = id + ':' + secondaryId;
            }
        }
        return id;
    }

    /**
     * The selection has changed in the part being tracked.
     * Forward it to the listeners.
     * 
     * @see ISelectionChangedListener#selectionChanged
     */
    public void selectionChanged(SelectionChangedEvent event) {
        fireSelection(getPart(), event.getSelection());
    }

    /**
     * Sets the page this selection provider works for
     * 
     * @param page workbench page
     */
    private void setPage(IWorkbenchPage page) {
        fPage = page;
    }

    /**
     * Returns the page this selection provider works for
     * 
     * @return workbench page
     */
    protected IWorkbenchPage getPage() {
        return fPage;
    }

    /**
     * Returns the part this is tracking, 
     * or <code>null</code> if it is not open
     * 
     * @return part., or <code>null</code>
     */
    protected IWorkbenchPart getPart() {
        return fPart;
    }

    /*
     * @see AbstractPartSelectionTracker#getSelection()
     */
    public ISelection getSelection() {
        IWorkbenchPart part = getPart();
        if (part != null) {
            ISelectionProvider sp = part.getSite().getSelectionProvider();
            if (sp != null) {
                return sp.getSelection();
            }
        }
        return null;
    }

    /**
     * @see AbstractDebugSelectionProvider#getSelectionProvider()
     */
    protected ISelectionProvider getSelectionProvider() {
        IWorkbenchPart part = getPart();
        if (part != null) {
            return part.getSite().getSelectionProvider();
        }
        return null;
    }

    /**
     * Sets the part for this selection tracker.
     * 
     * @param part the part
     * @param notify whether to send notification that the selection has changed.
     */
    private void setPart(IWorkbenchPart part, boolean notify) {
        if (fPart != null) {
            // remove myself as a listener from the existing part
            ISelectionProvider sp = fPart.getSite().getSelectionProvider();
            if (sp != null) {
                sp.removeSelectionChangedListener(selectionListener);
                if (sp instanceof IPostSelectionProvider) {
					((IPostSelectionProvider) sp)
                            .removePostSelectionChangedListener(postSelectionListener);
				} else {
					sp.removeSelectionChangedListener(postSelectionListener);
				}
            }
        }
        fPart = part;
        ISelection sel = null;
        if (part != null) {
            ISelectionProvider sp = part.getSite().getSelectionProvider();
            if (sp != null) {
                sp.addSelectionChangedListener(selectionListener);
                if (sp instanceof IPostSelectionProvider) {
					((IPostSelectionProvider) sp)
                            .addPostSelectionChangedListener(postSelectionListener);
				} else {
					sp.addSelectionChangedListener(postSelectionListener);
				}
                if (notify) {
                    // get the selection to send below
                    sel = sp.getSelection();
                }
            }
        }
        if (notify) {
            fireSelection(part, sel);
            firePostSelection(part, sel);
        }
    }
}
