/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;


/**
 * Common function for a deferred workbench adapter for a debug element. 
 * <p>
 * Clients may subclass this class to provide custom adapters for elements in a debug
 * model. The debug platform provides <code>IDeferredWorkbenchAdapters</code> for the standard debug
 * elements. Clients may override the default content in the debug view by providing an
 * <code>IWorkbenchAdapter</code> or <code>IDeferredWorkbenchAdapter</code> for a debug
 * element.
 * </p>
 * @since 3.1
 * @deprecated deferred custom content in the debug views is no longer supported by
 * 	 {@link IDeferredWorkbenchAdapter}. Deferred custom content is currently supported
 * 	 by a provisional internal viewer framework.
 */
public abstract class DeferredDebugElementWorkbenchAdapter extends DebugElementWorkbenchAdapter implements IDeferredWorkbenchAdapter {
    
	/**
	 * An empty collection of children
	 */
    protected static final Object[] EMPTY = new Object[0];

	/* (non-Javadoc)
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
     */
    public boolean isContainer() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#getRule(java.lang.Object)
     */
    public ISchedulingRule getRule(Object object) {
        return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object, org.eclipse.ui.progress.IElementCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return;
		}
	    Object[] children = getChildren(object);
	    if (monitor.isCanceled()) {
	    	return;
	    }
	    if (children.length > 0) {
	        collector.add(children, monitor);
	    }
	    collector.done();
	}


}
