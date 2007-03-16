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
package org.eclipse.ui.tests.adaptable;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Provides tree contents for objects that have the IWorkbenchAdapter
 * adapter registered. 
 */
public class TestAdaptableContentProvider implements ITreeContentProvider,
        IResourceChangeListener {
    protected Viewer viewer;

    /* (non-Javadoc)
     * Method declared on IContentProvider.
     */
    public void dispose() {
        if (viewer != null) {
            Object obj = viewer.getInput();
            if (obj instanceof IWorkspace) {
                IWorkspace workspace = (IWorkspace) obj;
                workspace.removeResourceChangeListener(this);
            } else if (obj instanceof IContainer) {
                IWorkspace workspace = ((IContainer) obj).getWorkspace();
                workspace.removeResourceChangeListener(this);
            }
        }
    }

    /**
     * Returns the implementation of IWorkbenchAdapter for the given
     * object.  Returns null if the adapter is not defined or the
     * object is not adaptable.
     */
    protected IWorkbenchAdapter getAdapter(Object o) {
        return TestAdaptableWorkbenchAdapter.getInstance();
    }

    /* (non-Javadoc)
     * Method declared on ITreeContentProvider.
     */
    public Object[] getChildren(Object element) {
        IWorkbenchAdapter adapter = getAdapter(element);
        if (adapter != null) {
            return adapter.getChildren(element);
        }
        return new Object[0];
    }

    /* (non-Javadoc)
     * Method declared on IStructuredContentProvider.
     */
    public Object[] getElements(Object element) {
        return getChildren(element);
    }

    /* (non-Javadoc)
     * Method declared on ITreeContentProvider.
     */
    public Object getParent(Object element) {
        IWorkbenchAdapter adapter = getAdapter(element);
        if (adapter != null) {
            return adapter.getParent(element);
        }
        return null;
    }

    /* (non-Javadoc)
     * Method declared on ITreeContentProvider.
     */
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }

    /* (non-Javadoc)
     * Method declared on IContentProvider.
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = viewer;
        IWorkspace oldWorkspace = null;
        IWorkspace newWorkspace = null;
        if (oldInput instanceof IWorkspace) {
            oldWorkspace = (IWorkspace) oldInput;
        } else if (oldInput instanceof IContainer) {
            oldWorkspace = ((IContainer) oldInput).getWorkspace();
        }
        if (newInput instanceof IWorkspace) {
            newWorkspace = (IWorkspace) newInput;
        } else if (newInput instanceof IContainer) {
            newWorkspace = ((IContainer) newInput).getWorkspace();
        }
        if (oldWorkspace != newWorkspace) {
            if (oldWorkspace != null) {
                oldWorkspace.removeResourceChangeListener(this);
            }
            if (newWorkspace != null) {
                newWorkspace.addResourceChangeListener(this,
                        IResourceChangeEvent.POST_CHANGE);
            }
        }
    }

    /**
     * Process a resource delta.  
     */
    protected void processDelta(IResourceDelta delta) {
        // This method runs inside a syncExec.  The widget may have been destroyed
        // by the time this is run.  Check for this and do nothing if so.
        Control ctrl = viewer.getControl();
        if (ctrl == null || ctrl.isDisposed())
            return;

        // Get the affected resource
        IResource resource = delta.getResource();

        // If any children have changed type, just do a full refresh of this parent,
        // since a simple update on such children won't work, 
        // and trying to map the change to a remove and add is too dicey.
        // The case is: folder A renamed to existing file B, answering yes to overwrite B.
        IResourceDelta[] affectedChildren = delta
                .getAffectedChildren(IResourceDelta.CHANGED);
        for (int i = 0; i < affectedChildren.length; i++) {
            if ((affectedChildren[i].getFlags() & IResourceDelta.TYPE) != 0) {
                ((StructuredViewer) viewer).refresh(resource);
                return;
            }
        }

        // Check the flags for changes the Navigator cares about.
        // See ResourceLabelProvider for the aspects it cares about.
        // Notice we don't care about F_CONTENT or F_MARKERS currently.
        int changeFlags = delta.getFlags();
        if ((changeFlags & (IResourceDelta.OPEN | IResourceDelta.SYNC)) != 0) {
            ((StructuredViewer) viewer).update(resource, null);
        }

        // Handle changed children .
        for (int i = 0; i < affectedChildren.length; i++) {
            processDelta(affectedChildren[i]);
        }

        // Process removals before additions, to avoid multiple equal elements in the viewer.

        // Handle removed children. Issue one update for all removals.
        affectedChildren = delta.getAffectedChildren(IResourceDelta.REMOVED);
        if (affectedChildren.length > 0) {
            Object[] affected = new Object[affectedChildren.length];
            for (int i = 0; i < affectedChildren.length; i++)
                affected[i] = affectedChildren[i].getResource();
            if (viewer instanceof AbstractTreeViewer) {
                ((AbstractTreeViewer) viewer).remove(affected);
            } else {
                ((StructuredViewer) viewer).refresh(resource);
            }
        }

        // Handle added children. Issue one update for all insertions.
        affectedChildren = delta.getAffectedChildren(IResourceDelta.ADDED);
        if (affectedChildren.length > 0) {
            Object[] affected = new Object[affectedChildren.length];
            for (int i = 0; i < affectedChildren.length; i++)
                affected[i] = affectedChildren[i].getResource();
            if (viewer instanceof AbstractTreeViewer) {
                ((AbstractTreeViewer) viewer).add(resource, affected);
            } else {
                ((StructuredViewer) viewer).refresh(resource);
            }
        }
    }

    /**
     * The workbench has changed.  Process the delta and issue updates to the viewer,
     * inside the UI thread.
     *
     * @see IResourceChangeListener#resourceChanged
     */
    public void resourceChanged(final IResourceChangeEvent event) {
        final IResourceDelta delta = event.getDelta();
        Control ctrl = viewer.getControl();
        if (ctrl != null && !ctrl.isDisposed()) {
            // Do a sync exec, not an async exec, since the resource delta
            // must be traversed in this method.  It is destroyed
            // when this method returns.
            ctrl.getDisplay().syncExec(new Runnable() {
                public void run() {
                    processDelta(delta);
                }
            });
        }
    }
}
