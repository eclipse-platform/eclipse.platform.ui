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
package org.eclipse.debug.internal.ui.views.launch;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.internal.ui.views.DebugViewInterimLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.UIJob;

/**
 * The launch viewer displays a tree of launches.
 */
public class LaunchViewer extends TreeViewer {
    
    private SelectionJob fSelectionJob = null;
    private ExpansionJob fExpansionJob = null;
    
    class SelectionJob extends UIJob {
        
        private Object element;
        private List parents; // top down
        private Object lock;
        
        /**
         * Constucts a job to select the given element.
         * 
         * @param target the element to select
         */
        public SelectionJob(Object target, Object lock) {
            super(DebugUIViewsMessages.getString("LaunchViewer.0")); //$NON-NLS-1$
            element = target;
            parents = new ArrayList();
            this.lock = lock;
            addAllParents(parents, element);
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStatus runInUIThread(IProgressMonitor monitor) {
            if (getControl().isDisposed()) {
                return Status.OK_STATUS;
            }
            synchronized (lock) {
	            boolean allParentsExpanded = true;
	            Iterator iterator = parents.iterator();
	            while (iterator.hasNext() && !monitor.isCanceled()) {
	                Object parent = iterator.next();
	                TreeItem item = (TreeItem) doFindItem(parent);
	                if (item != null) {
	                    expandToLevel(parent, 1);
	                } else {
	                    allParentsExpanded = false;
	                    break;
	                }
	            }
	            if (allParentsExpanded) {
	                if (doFindItem(element) != null) {
	                    setSelection(new StructuredSelection(element), true);
	                    fSelectionJob = null;
	                    return Status.OK_STATUS;
	                }
	            }
	            return Status.OK_STATUS;
            }
        }
        
    }
    
    class ExpansionJob extends UIJob {
        
        private Object element;
        private List parents; // top down
        private Object lock;
        
        /**
         * Constucts a job to expand the given element.
         * 
         * @param target the element to expand
         */
        public ExpansionJob(Object target, Object lock) {
            super(DebugUIViewsMessages.getString("LaunchViewer.1")); //$NON-NLS-1$
            element = target;
            parents = new ArrayList();
            this.lock = lock;
            addAllParents(parents, element);
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        public IStatus runInUIThread(IProgressMonitor monitor) {
            if (getControl().isDisposed()) {
                return Status.OK_STATUS;
            }            
            synchronized (lock) {
	            boolean allParentsExpanded = true;
	            Iterator iterator = parents.iterator();
	            while (iterator.hasNext() && !monitor.isCanceled()) {
	                Object parent = iterator.next();
	                TreeItem item = (TreeItem) doFindItem(parent);
	                if (item != null) {
	                    expandToLevel(parent, 1);
	                } else {
	                    allParentsExpanded = false;
	                    break;
	                }
	            }
	            if (allParentsExpanded) {
	                TreeItem item = (TreeItem) doFindItem(element); 
	                if (item != null) {
	                    expandToLevel(element, 1);
	                    fExpansionJob = null;
	                    return Status.OK_STATUS;
	                }
	            }
	            return Status.OK_STATUS;
            }
        }
        
    }    
		
	/**
	 * Overridden to fix bug 39709 - duplicate items in launch viewer. The
	 * workaround is required since debug creation events (which result in
	 * additions to the tree) are processed asynchrnously with the expanding
	 * of a launch/debug target in the tree. 
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#add(java.lang.Object, java.lang.Object)
	 */
	public synchronized void add(Object parentElement, Object childElement) {
		if (doFindItem(childElement) == null) {
			super.add(parentElement, childElement);
			runDeferredUpdates();
		}
	}
		
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.AbstractTreeViewer#add(java.lang.Object, java.lang.Object[])
     */
    public synchronized void add(Object parentElement, Object[] childElements) {
        super.add(parentElement, childElements);
        runDeferredUpdates();
    }
    
    private void runDeferredUpdates() {
        if (fExpansionJob != null) {
            fExpansionJob.schedule();
        }
		if (fSelectionJob != null) {
		    fSelectionJob.schedule();
		}
    }
    
	public LaunchViewer(Composite parent) {
		super(new Tree(parent, SWT.MULTI));
		setUseHashlookup(true);
	}
			
	/**
	 * Update the images for all stack frame children of the given thread.
	 * 
	 * @param parentThread the thread whose frames should be updated
	 */	
	protected void updateStackFrameImages(IThread parentThread) {
		Widget parentItem= findItem(parentThread);
		if (parentItem != null) {
			Item[] items= getItems((Item)parentItem);
			for (int i = 0; i < items.length; i++) {
				updateTreeItemImage((TreeItem)items[i]);
			}
		}
	}
	
	/**
	 * Updates the image of the given tree item.
	 * 
	 * @param treeItem the item
	 */
	protected void updateTreeItemImage(TreeItem treeItem) {
		ILabelProvider provider = (ILabelProvider) getLabelProvider();
		Image image = provider.getImage(treeItem.getData());
		if (image != null) {
			treeItem.setImage(image);
		}			
	}
	
	/* (non-Javadoc)
	 * Method declared in AbstractTreeViewer.
	 */
	protected void doUpdateItem(Item item, Object element) {
		// update icon and label
		ILabelProvider provider= (ILabelProvider) getLabelProvider();
		String text= provider.getText(element);
		if ("".equals(item.getText()) || !DebugViewInterimLabelProvider.PENDING_LABEL.equals(text)) { //$NON-NLS-1$
			// If an element already has a label, don't set the label to
			// the pending label. This avoids labels flashing when they're
			// updated.
			item.setText(text);
		}
		Image image = provider.getImage(element);
		if (item.getImage() != image) {
			item.setImage(image);
		}
		if (provider instanceof IColorProvider) {
			IColorProvider cp = (IColorProvider) provider;
			TreeItem treeItem = (TreeItem) item;
			treeItem.setForeground(cp.getForeground(element));
			treeItem.setBackground(cp.getBackground(element));
		}
	}
	
	/**
	 * @see StructuredViewer#refresh(Object)
	 */
	public void refresh(Object element) {
		//@see bug 7965 - Debug view refresh flicker
		getControl().setRedraw(false);
		super.refresh(element);
		getControl().setRedraw(true);
	}
	
	/**
	 * If the element is in the tree, reveal and select if. Otherwise, reveal
	 * and select the item when it is added. Calling this method overrides
	 * any previous deferred selection.
	 * 
	 * @param element element to be selected if present or when added to the tree
	 */
	public synchronized void setDeferredSelection(Object element) {
	    if (doFindItem(element) == null) {
	        if (fSelectionJob != null) {
	            fSelectionJob.cancel();
	        }
	        fSelectionJob = new SelectionJob(element, this);
	        fSelectionJob.schedule();
	    } else {
	        setSelection(new StructuredSelection(element), true);
	    }
	}
	
	public synchronized void deferExpansion(Object element) {
	    TreeItem treeItem = (TreeItem) doFindItem(element);
        if (treeItem == null) {
	        if (fExpansionJob != null) {
	            fExpansionJob.cancel();
	        }
	        fExpansionJob = new ExpansionJob(element, this);
	        fExpansionJob.schedule();
	    } else {
	        if (!getExpanded(treeItem)) {
	            expandToLevel(element, 1);
	        }
	    }
	}
	
	private void addAllParents(List list, Object element) {
	    if (element instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) element;
            IWorkbenchAdapter adapter = (IWorkbenchAdapter) adaptable.getAdapter(IWorkbenchAdapter.class);
            if (adapter != null) {
                Object parent = adapter.getParent(element);
                if (parent != null) {
                    list.add(0, parent);
                    if (!(parent instanceof ILaunch))
                    addAllParents(list, parent);
                }
            }
        }
	}
	
	protected void cancelJobs() {
	    cancel(fSelectionJob);
	    cancel(fExpansionJob);
	}
	
	private void cancel(Job job) {
	    if (job != null) {
	        job.cancel();
	    }	    
	}
}

