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
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredExpression;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredExpressionManager;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredLaunch;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredLaunchManager;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredProcess;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredRegisterGroup;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredStackFrame;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredTarget;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredThread;
import org.eclipse.debug.internal.ui.elements.adapters.DeferredVariable;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

/**
 * DebugElementAdapterFactory
 */
public class DebugElementAdapterFactory implements IAdapterFactory {
    
    private static IDeferredWorkbenchAdapter fgLaunchManagerAdapter = new DeferredLaunchManager();
    private static IDeferredWorkbenchAdapter fgLaunchAdapter = new DeferredLaunch();
    private static IDeferredWorkbenchAdapter fgDebugTargetAdapter = new DeferredTarget();
    private static IDeferredWorkbenchAdapter fgProcessAdapter = new DeferredProcess();
    private static IDeferredWorkbenchAdapter fgThreadAdapter = new DeferredThread();
    private static IDeferredWorkbenchAdapter fgFrameAdapter = new DeferredStackFrame();
    private static IDeferredWorkbenchAdapter fgRegisterGroupAdapter = new DeferredRegisterGroup();
    private static IDeferredWorkbenchAdapter fgVariableAdapter = new DeferredVariable();
    private static IDeferredWorkbenchAdapter fgExpressionAdapter = new DeferredExpression();
    private static IDeferredWorkbenchAdapter fgExpressionManagerAdapter = new DeferredExpressionManager();

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType.isInstance(adaptableObject)) {
			return adaptableObject;
		}
        if (adapterType.equals(IWorkbenchAdapter.class) || adapterType.equals(IWorkbenchAdapter2.class) || adapterType.equals(IDeferredWorkbenchAdapter.class)) {
        	if (adaptableObject instanceof ILaunchManager) {
        		return fgLaunchManagerAdapter;
        	}
        	if (adaptableObject instanceof ILaunch) {
        		return fgLaunchAdapter;
        	}
        	if (adaptableObject instanceof IDebugTarget) {
        		return fgDebugTargetAdapter;
        	}
        	if (adaptableObject instanceof IProcess) {
        		return fgProcessAdapter;
        	}
        	if (adaptableObject instanceof IThread) {
        		return fgThreadAdapter;
        	}
        	if (adaptableObject instanceof IStackFrame) {
        		return fgFrameAdapter;
        	}
        	if (adaptableObject instanceof IVariable) {
        		return fgVariableAdapter;
        	}
        	if (adaptableObject instanceof IExpression) {
        		return fgExpressionAdapter;
        	}
        	if (adaptableObject instanceof IRegisterGroup) {
        		return fgRegisterGroupAdapter;
        	}
        	if (adaptableObject instanceof IExpressionManager) {
        		return fgExpressionManagerAdapter;
        	}
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
        return new Class[] {IWorkbenchAdapter.class, IWorkbenchAdapter2.class, IDeferredWorkbenchAdapter.class};
    }

}
