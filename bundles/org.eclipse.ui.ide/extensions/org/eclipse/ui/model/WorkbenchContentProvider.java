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
package org.eclipse.ui.model;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

/**
 * Tree content provider for resource objects that can be adapted to the interface
 * {@link org.eclipse.ui.model.IWorkbenchAdapter IWorkbenchAdapter}. This provider
 * will listen for resource changes within the workspace and update the viewer
 * as necessary.
 * <p>
 * This class may be instantiated, or subclassed by clients.
 * </p>
 */
public class WorkbenchContentProvider extends BaseWorkbenchContentProvider
        implements IResourceChangeListener {
    private Viewer viewer;

    /**
     * Creates the resource content provider.
     */
    public WorkbenchContentProvider() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on IContentProvider.
     */
    public void dispose() {
        if (viewer != null) {
            IWorkspace workspace = null;
            Object obj = viewer.getInput();
            if (obj instanceof IWorkspace) {
                workspace = (IWorkspace) obj;
            } else if (obj instanceof IContainer) {
                workspace = ((IContainer) obj).getWorkspace();
            }
            if (workspace != null) {
                workspace.removeResourceChangeListener(this);
            }
        }

        super.dispose();
    }

    /* (non-Javadoc)
     * Method declared on IContentProvider.
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        super.inputChanged(viewer, oldInput, newInput);

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

    /* (non-Javadoc)
     * Method declared on IResourceChangeListener.
     */
    public final void resourceChanged(final IResourceChangeEvent event) {
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
        if ((changeFlags & (IResourceDelta.OPEN | IResourceDelta.SYNC
                | IResourceDelta.TYPE | IResourceDelta.DESCRIPTION)) != 0) {
            ((StructuredViewer) viewer).update(resource, null);
        }
        // Replacing a resource may affect its label and its children
        if ((changeFlags & IResourceDelta.REPLACED) != 0) {
            ((StructuredViewer) viewer).refresh(resource, true);
            return;
        }

        // Handle changed children .
        for (int i = 0; i < affectedChildren.length; i++) {
            processDelta(affectedChildren[i]);
        }

        // @issue several problems here:
        //  - should process removals before additions, to avoid multiple equal elements in viewer
        //   - Kim: processing removals before additions was the indirect cause of 44081 and its varients
        //   - Nick: no delta should have an add and a remove on the same element, so processing adds first is probably OK
        //  - using setRedraw will cause extra flashiness
        //  - setRedraw is used even for simple changes
        //  - to avoid seeing a rename in two stages, should turn redraw on/off around combined removal and addition
        //   - Kim: done, and only in the case of a rename (both remove and add changes in one delta).

        boolean addedAndRemoved = false;
        try {
            IResourceDelta[] addedChildren = delta
                    .getAffectedChildren(IResourceDelta.ADDED);
            IResourceDelta[] removedChildren = delta
                    .getAffectedChildren(IResourceDelta.REMOVED);
            addedAndRemoved = addedChildren.length > 0
                    & removedChildren.length > 0;

            // Disable redraw until the operation is finished so we don't get a flash of both the new and old item (in the case of rename)
            // Only do this if we're both adding and removing files (the rename case)
            if (addedAndRemoved)
                viewer.getControl().setRedraw(false);

            // Process additions before removals as to not cause selection preservation prior to new objects being added    
            // Handle added children. Issue one update for all insertions.
            if (addedChildren.length > 0) {
                Object[] affected = new Object[addedChildren.length];
                for (int i = 0; i < addedChildren.length; i++)
                    affected[i] = addedChildren[i].getResource();
                if (viewer instanceof AbstractTreeViewer) {
                    ((AbstractTreeViewer) viewer).add(resource, affected);
                } else {
                    ((StructuredViewer) viewer).refresh(resource);
                }
            }

            // Handle removed children. Issue one update for all removals.
            if (removedChildren.length > 0) {
                Object[] affected = new Object[removedChildren.length];
                for (int i = 0; i < removedChildren.length; i++)
                    affected[i] = removedChildren[i].getResource();
                if (viewer instanceof AbstractTreeViewer) {
                    ((AbstractTreeViewer) viewer).remove(affected);
                } else {
                    ((StructuredViewer) viewer).refresh(resource);
                }
            }
        } finally {
            if (addedAndRemoved)
                viewer.getControl().setRedraw(true);
        }

    }
}