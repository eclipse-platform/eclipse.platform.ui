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
package org.eclipse.debug.ui;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;


/**
 * Common function for a deferred workbench adapter for a debug element. 
 * <p>
 * Clients may subclass this class to provide custom adapters for elements in a debug
 * model. The debug platform provides <code>IWorkbenchAdapters</code> for the standard debug
 * elements. Clients may override the default content in the debug view by providing an
 * <code>IWorkbenchAdapter</code> or <code>IDeferredWorkbenchAdapter</code> for a debug
 * element.
 * </p>
 * @since 3.1
 */
public abstract class DeferredDebugElementWorkbenchAdapter extends DebugElementWorkbenchAdapter implements IDeferredWorkbenchAdapter {
    
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


}
