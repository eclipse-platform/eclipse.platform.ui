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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;


/**
 * Default deferred content provider for a debug target 
 */
public class DeferredThread extends DeferredDebugElementWorkbenchAdapter implements IDeferredWorkbenchAdapter {
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object, org.eclipse.ui.progress.IElementCollector, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
        IThread thread = (IThread) object;
        try {
            IStackFrame[] stackFrames = thread.getStackFrames();
            collector.add(stackFrames, monitor);
        } catch (DebugException e) {
        }
        collector.done();
    }

}
