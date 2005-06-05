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
package org.eclipse.debug.internal.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.progress.PendingUpdateAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.WorkbenchJob;
import org.osgi.framework.Bundle;
/**
 * A remote content manager that merges content into a tree rather then replacing
 * its children with a "pending" node, and then the real children when they are available.
 * This avoids collapsing the viewer when a refresh is performed. This implementation is
 * currently tied to the <code>RemoteTreeViewer</code>.
 * 
 * @since 3.1
 */
public class RemoteTreeContentManager extends DeferredTreeContentManager {

    private RemoteTreeViewer fViewer;
    private IWorkbenchSiteProgressService progressService;
    
    /**
     * Job to fetch children
     */
    private Job fFetchJob = new FetchJob();
    
    /**
     * Queue of parents to fetch children for, and
     * associated element collectors and deferred adapters.
     */
    private List fElementQueue = new ArrayList();
    private List fCollectors = new ArrayList();
    private List fAdapaters = new ArrayList();
    
    /**
     * Fetching children is done in a single background job.
     * This makes fetching single threaded/serial per view.
     */
    class FetchJob extends Job {
    	
        public FetchJob() {
            super(DebugUIViewsMessages.RemoteTreeContentManager_0);
            setSystem(true);
        }

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			while (!fElementQueue.isEmpty() && !monitor.isCanceled()) {
				Object element = null;
				IElementCollector collector = null;
				IDeferredWorkbenchAdapter adapter = null;
				synchronized (fElementQueue) {
					// could have been cancelled after entering the while loop
					if (fElementQueue.isEmpty()) {
						return Status.CANCEL_STATUS;
					}
					element = fElementQueue.remove(0);
					collector = (IElementCollector) fCollectors.remove(0);
					adapter = (IDeferredWorkbenchAdapter) fAdapaters.remove(0);
				}
				adapter.fetchDeferredChildren(element, collector, monitor);
			}
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
    	
    }
    
    /**
     * Element collector
     */
    public class Collector implements IElementCollector {            
        // number of children added to the tree
        int offset = 0;
        Object fParent;
        
        public Collector(Object parent) {
        	fParent = parent;
        }
        /*
         *  (non-Javadoc)
         * @see org.eclipse.jface.progress.IElementCollector#add(java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
         */
        public void add(Object element, IProgressMonitor monitor) {
            add(new Object[] { element }, monitor);
        }

        /*
         *  (non-Javadoc)
         * @see org.eclipse.jface.progress.IElementCollector#add(java.lang.Object[], org.eclipse.core.runtime.IProgressMonitor)
         */
        public void add(Object[] elements, IProgressMonitor monitor) {
            Object[] filtered = fViewer.filter(elements);
            if (filtered.length > 0) {
                replaceChildren(fParent, filtered, offset, monitor);
                offset = offset + filtered.length;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.progress.IElementCollector#done()
         */
        public void done() {
            prune(fParent, offset);
        }
    }
    
    /**
     * Contructs a new content manager.
     * 
     * @param provider content provider
     * @param viewer viewer
     * @param site part site
     */
    public RemoteTreeContentManager(ITreeContentProvider provider, RemoteTreeViewer viewer, IWorkbenchPartSite site) {
        super(provider, viewer, site);
        fViewer = viewer;
        Object siteService = site.getAdapter(IWorkbenchSiteProgressService.class);
        if (siteService != null) {
        	progressService = (IWorkbenchSiteProgressService) siteService;
        }
    }
    
    /**
     * Create the element collector for the receiver.
     *@param parent
     *            The parent object being filled in,
     * @param placeholder
     *            The adapter that will be used to indicate that results are
     *            pending, possibly <code>null</code>
     * @return IElementCollector
     */
    protected IElementCollector createElementCollector(Object parent, PendingUpdateAdapter placeholder) {
        return new Collector(parent);
    }
    
    /**
     * Returns the child elements of the given element, or in the case of a
     * deferred element, returns a placeholder. If a deferred element is used, a
     * job is created to fetch the children in the background.
     * 
     * @param parent
     *            The parent object.
     * @return Object[] or <code>null</code> if parent is not an instance of
     *         IDeferredWorkbenchAdapter.
     */
    public Object[] getChildren(final Object parent) {
        IDeferredWorkbenchAdapter element = getAdapter(parent);
        if (element == null)
            return null;
        Object[] currentChildren = fViewer.getCurrentChildren(parent);
        PendingUpdateAdapter placeholder = null;
        if (currentChildren == null || currentChildren.length == 0) {
            placeholder = new PendingUpdateAdapter();
        }
        startFetchingDeferredChildren(parent, element, placeholder);
        if (placeholder == null) {
            return currentChildren;
        }
        return new Object[] { placeholder };
    }
    
    /**
     * Create a UIJob to replace the children of the parent in the tree viewer.
     * 
     * @param parent the parent for which children are to be replaced
     * @param children the replacement children
     * @param offset the offset at which to start replacing children
     * @param monitor progress monitor
     */
    protected void replaceChildren(final Object parent, final Object[] children, final int offset, IProgressMonitor monitor) {
    	if (monitor.isCanceled()) {
    		return;
    	}
        WorkbenchJob updateJob = new WorkbenchJob(DebugUIViewsMessages.IncrementalDeferredTreeContentManager_0) { //$NON-NLS-1$
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
            public IStatus runInUIThread(IProgressMonitor updateMonitor) {
                //Cancel the job if the tree viewer got closed
                if (fViewer.getControl().isDisposed())
                    return Status.CANCEL_STATUS;
                fViewer.replace(parent, children, offset);
                return Status.OK_STATUS;
            }
        };
        updateJob.setSystem(true);
        updateJob.setPriority(Job.INTERACTIVE);
        updateJob.schedule();
    } 
    
    /**
     * Create a UIJob to prune the children of the parent in the tree viewer, starting
     * at the given offset.
     * 
     * @param parent the parent for which children should be pruned
     * @param offset the offset at which children should be pruned. All children at and after
     *  this index will be removed from the tree. 
     * @param monitor
     */
    protected void prune(final Object parent, final int offset) {
        WorkbenchJob updateJob = new WorkbenchJob(DebugUIViewsMessages.IncrementalDeferredTreeContentManager_1) { //$NON-NLS-1$
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
            public IStatus runInUIThread(IProgressMonitor updateMonitor) {
                //Cancel the job if the tree viewer got closed
                if (fViewer.getControl().isDisposed())
                    return Status.CANCEL_STATUS;
                fViewer.prune(parent, offset);
                return Status.OK_STATUS;
            }
        };
        updateJob.setSystem(true);
        updateJob.setPriority(Job.INTERACTIVE);
        updateJob.schedule();
    }     
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.DeferredTreeContentManager#runClearPlaceholderJob(org.eclipse.ui.internal.progress.PendingUpdateAdapter)
	 */
	protected void runClearPlaceholderJob(PendingUpdateAdapter placeholder) {
	    // the placeholder is not used when there were already children in the tree (null)
		if (placeholder != null) {
			super.runClearPlaceholderJob(placeholder);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.DeferredTreeContentManager#getFetchJobName(java.lang.Object, org.eclipse.ui.progress.IDeferredWorkbenchAdapter)
	 */
	protected String getFetchJobName(Object parent, IDeferredWorkbenchAdapter adapter) {
		return DebugUIViewsMessages.RemoteTreeContentManager_0; //$NON-NLS-1$
	}
	
	
    /**
     * Returns the IDeferredWorkbenchAdapter for the element, or <code>null</code>.
     * If a client has contributed an IWorkbenchAdapter for the element, it 
     * should be used in place of the debug platform's IDeferredWorkbenchAdapter,
     * in which case, <code>null</code> is returned.
     * 
     * @param element
     * @return IDeferredWorkbenchAdapter or <code>null</code>
     */
    protected IDeferredWorkbenchAdapter getAdapter(Object element) {
        if (element instanceof IDeferredWorkbenchAdapter)
            return (IDeferredWorkbenchAdapter) element;
        if (!(element instanceof IAdaptable))
            return null;
        IAdaptable adaptable = (IAdaptable) element;
		IDeferredWorkbenchAdapter deferred = (IDeferredWorkbenchAdapter) adaptable.getAdapter(IDeferredWorkbenchAdapter.class);
        if (deferred == null)
            return null;
        
        DebugUIPlugin plugin = DebugUIPlugin.getDefault();
    	Bundle bundle = plugin.getBundle(deferred.getClass());
		Bundle debugBundle = plugin.getBundle();
		if (!debugBundle.equals(bundle)) {
    		// if client contributed, use it 
    		return deferred;
    	}
    	// if the client provided an IWorkbenchAdapter, use it
    	IWorkbenchAdapter nonDeferred = (IWorkbenchAdapter) adaptable.getAdapter(IWorkbenchAdapter.class);
    	if (nonDeferred != null) {
    		bundle = plugin.getBundle(nonDeferred.getClass());
    		if (!debugBundle.equals(bundle)) {
    			// by returning null, we'll revert to using the the object's workbench adapter
    			// by pretending it has no deffered adapter
    			return null;
    		}
    	}
        return deferred;
    }	
	
    protected void startFetchingDeferredChildren(final Object parent, final IDeferredWorkbenchAdapter adapter, PendingUpdateAdapter placeholder) {
		final IElementCollector collector = createElementCollector(parent, placeholder);
		synchronized (fElementQueue) {
			if (!fElementQueue.contains(parent)) {
				fElementQueue.add(parent);
				fCollectors.add(collector);
				fAdapaters.add(adapter);
			}
		}
		if (progressService == null)
			fFetchJob.schedule();
		else
			progressService.schedule(fFetchJob);
	}
    
    /**
     * Cancels any content this provider is currently fetching.
     */
    public void cancel() {
    	synchronized (fElementQueue) {
    		fFetchJob.cancel();
    		fElementQueue.clear();
    		fAdapaters.clear();
    		fCollectors.clear();
    	}
    }
}
