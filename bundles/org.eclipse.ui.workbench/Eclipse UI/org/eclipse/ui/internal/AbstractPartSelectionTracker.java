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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Provides per-part selection tracking for the selection service.
 */
public abstract class AbstractPartSelectionTracker {
    /**
     * List of selection listeners for this tracker
     */
    private ListenerList fListeners = new ListenerList();

    /**
     * List of post selection listeners for this tracker
     */
    private ListenerList postListeners = new ListenerList();

    /**
     * The id of the part this tracls
     */
    private String fPartId;

    /**
     * Constructs a part selection tracker for the part with the given id.
     * 
     * @param id part identifier
     */
    public AbstractPartSelectionTracker(String partId) {
        setPartId(partId);
    }

    /**
     * Adds a selection listener to this tracker
     * 
     * @param listener the listener to add
     */
    public void addSelectionListener(ISelectionListener listener) {
        fListeners.add(listener);
    }

    /**
     * Adds a post selection listener to this tracker
     * 
     * @param listener the listener to add
     */
    public void addPostSelectionListener(ISelectionListener listener) {
        postListeners.add(listener);
    }

    /**
     * Returns the selection from the part being tracked, 
     * or <code>null</code> if the part is closed or has no selection.
     */
    public abstract ISelection getSelection();

    /**
     * Removes a selection listener from this tracker.
     * 
     * @param listener the listener to remove
     */
    public void removeSelectionListener(ISelectionListener listener) {
        fListeners.remove(listener);
    }

    /**
     * Removes a post selection listener from this tracker.
     * 
     * @param listener the listener to remove
     */
    public void removePostSelectionListener(ISelectionListener listener) {
        postListeners.remove(listener);
    }

    /**
     * Disposes this selection tracker.  This removes all listeners currently registered.
     */
    public void dispose() {
        synchronized (fListeners) {
            Object[] listeners = fListeners.getListeners();
            for (int i = 0; i < listeners.length; i++) {
                fListeners.remove(listeners[i]);
                postListeners.remove(listeners[i]);
            }
        }
    }

    /**
     * Fires a selection event to the listeners.
     * 
     * @param part the part or <code>null</code> if no active part
     * @param sel the selection or <code>null</code> if no active selection
     * @param listeners the list of listeners to notify
     */
    protected void fireSelection(final IWorkbenchPart part, final ISelection sel) {
        Object[] array = fListeners.getListeners();
        for (int i = 0; i < array.length; i++) {
            final ISelectionListener l = (ISelectionListener) array[i];
            if ((part != null && sel != null)
                    || l instanceof INullSelectionListener) {
                Platform.run(new SafeRunnable() {
                    public void run() {
                        l.selectionChanged(part, sel);
                    }
                });
            }
        }
    }

    /**
     * Fires a post selection event to the listeners.
     * 
     * @param part the part or <code>null</code> if no active part
     * @param sel the selection or <code>null</code> if no active selection
     * @param listeners the list of listeners to notify
     */
    protected void firePostSelection(final IWorkbenchPart part,
            final ISelection sel) {
        Object[] array = postListeners.getListeners();
        for (int i = 0; i < array.length; i++) {
            final ISelectionListener l = (ISelectionListener) array[i];
            if ((part != null && sel != null)
                    || l instanceof INullSelectionListener) {
                Platform.run(new SafeRunnable() {
                    public void run() {
                        l.selectionChanged(part, sel);
                    }
                });
            }
        }
    }

    /**
     * Sets the id of the part that this tracks.
     * 
     * @param id view identifier
     */
    private void setPartId(String partId) {
        fPartId = partId;
    }

    /**
     * Returns the id of the part that this tracks.
     * 
     * @return part identifier
     */
    protected String getPartId() {
        return fPartId;
    }

}
