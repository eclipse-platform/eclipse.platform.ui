/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

 
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.internal.ui.views.IRemoteTreeViewerUpdateListener;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.UIJob;

/**
 * Variables viewer. As the user steps through code, this
 * we ensure that newly added varibles are visible.
 */
public class VariablesViewer extends RemoteTreeViewer {
    
    private ArrayList fUpdateListeners = new ArrayList();
    private StateRestorationJob fStateRestorationJob = new StateRestorationJob(DebugUIViewsMessages.RemoteTreeViewer_0); //$NON-NLS-1$
    private VariablesView fView = null;
    
    private class StateRestorationJob extends UIJob {
        public StateRestorationJob(String name) {
            super(name);
            setSystem(true);
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStatus runInUIThread(IProgressMonitor monitor) {
            restoreExpansionState();
            return Status.OK_STATUS;
        }   
    }

	/**
	 * Constructor for VariablesViewer.
	 * @param parent
	 * @param style
	 * @param view containing view, or <code>null</code> if none
	 */
	public VariablesViewer(Composite parent, int style, VariablesView view) {
		super(parent, style);
		fView = view;
	}
	
	/**
	 * @see AbstractTreeViewer#newItem(Widget, int, int)
	 */
	protected Item newItem(Widget parent, int style, int index) {
		Item item = super.newItem(parent, style, index);
		if (index != -1 && getSelection(getControl()).length == 0) {
			//ignore the dummy items
			showItem(item);
		} 
		return item;
	}
	
	/**
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#setExpandedElements(Object[])
	 */
	public void setExpandedElements(Object[] elements) {
		getControl().setRedraw(false);
		super.setExpandedElements(elements);
		getControl().setRedraw(true);
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.RemoteTreeViewer#runDeferredUpdates()
     */
    protected void runDeferredUpdates() {
        super.runDeferredUpdates();
        fStateRestorationJob.schedule();
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#collapseAll()
	 */
	public void collapseAll() {
		//see https://bugs.eclipse.org/bugs/show_bug.cgi?id=39449
		if (getRoot() != null) {
			super.collapseAll();
		}
	}
	

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.RemoteTreeViewer#restoreExpansionState()
     */
    protected synchronized void restoreExpansionState() {
        cancelJobs();
        for (Iterator i = fUpdateListeners.iterator(); i.hasNext();) {
            IRemoteTreeViewerUpdateListener listener = (IRemoteTreeViewerUpdateListener) i.next();
            listener.treeUpdated();
        }
    }
    
    public void addUpdateListener(IRemoteTreeViewerUpdateListener listener) {
        fUpdateListeners.add(listener);
    }
    public void removeUpdateListener(IRemoteTreeViewerUpdateListener listener) {
        fUpdateListeners.remove(listener);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.RemoteTreeViewer#replace(java.lang.Object, java.lang.Object[], int)
	 */
	public synchronized void replace(Object parent, Object[] children, int offset) {
		if (fView != null) {
	    	if (children.length == 1 && children[0] instanceof DebugException) {
	    		IStatus status = ((DebugException)children[0]).getStatus();
	    		if (status != null) {
	    			String message = status.getMessage();
	    			if (message != null) {
	    				fView.showMessage(message);
	    			}
	    		}
	    		return;
	    	}
	    	fView.showViewer();
		}
		super.replace(parent, children, offset);
	}
    
    
    public boolean expandPath(IPath path) {
        String[] strings = path.segments();
        Item[] children = getChildren(getControl());
        return internalExpandPath(strings, 0, children);

    }
    private boolean internalExpandPath(String[] segments, int index, Item[] children) {
        try {
            String pathSegment = segments[index];
            for (int j = 0; j < children.length; j++) {
                Item child = children[j];
                Object data = child.getData();
                if (data instanceof IVariable) {
                    IVariable var = (IVariable) data;
                    if (pathSegment.equals(var.getName())) {
                        ITreeContentProvider provider = (ITreeContentProvider) getContentProvider();
                        provider.getChildren(child.getData());
                        setExpanded(child, true);
                        index++;
                        if (index < segments.length) {
                            Item[] newChildren = getChildren(child);
                            return internalExpandPath(segments, index, newChildren);
                        }
                        return true;
                    }
                }
            }
        } catch (DebugException e) {
            
        }
        return false;
    }
}
