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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.variables.DeferredExpression;
import org.eclipse.debug.internal.ui.views.variables.DeferredStackFrame;
import org.eclipse.debug.internal.ui.views.variables.DeferredVariable;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

/**
 * Creates default adapaters for deferred content for debug elements.
 * @since 3.1
 */
public class DeferredContentAdapterFactory implements IAdapterFactory {

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType.equals(IDeferredWorkbenchAdapter.class)) {
            if (adaptableObject instanceof IDebugTarget) {
                return new DeferredTarget();
            }
            if (adaptableObject instanceof IThread) {
                return new DeferredThread();
            }
            if (adaptableObject instanceof IStackFrame) {
                return new DeferredStackFrame();
            }
            if (adaptableObject instanceof IVariable) {
                return new DeferredVariable();
            }
            if (adaptableObject instanceof IExpression) {
                return new DeferredExpression();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
        return new Class[]{IDeferredWorkbenchAdapter.class};
    }

}
