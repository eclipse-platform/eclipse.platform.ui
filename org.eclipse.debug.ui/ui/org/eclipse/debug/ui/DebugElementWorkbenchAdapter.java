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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.views.launch.DebugElementHelper;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

/**
 * A workbench adapter for standard elements displayed in the debug view.
 * <p>
 * The adapter works for the following type of elements:
 * <ul>
 * <li>ILaunchManager</li>
 * <li>ILaunch</li>
 * <li>IDebugTarget</li>
 * <li>IProcess</li>
 * <li>IDebugThread</li>
 * <li>IStackFrame</li>
 * </ul> 
 * </p>
 * <p>
 * Clients may subclass this class to provide custom adapters for elements in a debug
 * model. The debug platform provides <code>IWorkbenchAdapters</code> for the standard debug
 * elements. Clients may override the default content in the debug view by providing an
 * <code>IWorkbenchAdapter</code> or <code>IDeferredWorkbenchAdapter</code> for a debug
 * element.
 * </p>
 * @since 3.1
 */
public class DebugElementWorkbenchAdapter implements IWorkbenchAdapter, IWorkbenchAdapter2 {
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parent) {
		try {
			if (parent instanceof IDebugTarget) {
				return ((IDebugTarget)parent).getThreads();
			}
			if (parent instanceof IThread) {
				return ((IThread)parent).getStackFrames();
			}			
		} catch (DebugException e) {
		}
		if (parent instanceof ILaunch) {
			return ((ILaunch)parent).getChildren();
		}
		if (parent instanceof ILaunchManager) {
			return ((ILaunchManager) parent).getLaunches();
		}
		return new Object[0];
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return DebugElementHelper.getImageDescriptor(object);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return DebugElementHelper.getLabel(o);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
		if (element instanceof IStackFrame) {
			return ((IStackFrame)element).getThread();
		}
		if (element instanceof IThread) {
			return ((IThread)element).getDebugTarget();
		}
		if (element instanceof IDebugTarget) {
			return ((IDebugElement)element).getLaunch();
		}
		if (element instanceof IProcess) {
			return ((IProcess)element).getLaunch();
		}
		if (element instanceof ILaunch) {
			return DebugPlugin.getDefault().getLaunchManager();
		}
		return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter2#getForeground(java.lang.Object)
     */
    public RGB getForeground(Object element) {
        return DebugElementHelper.getForeground(element);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter2#getBackground(java.lang.Object)
     */
    public RGB getBackground(Object element) {
        return DebugElementHelper.getBackground(element);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter2#getFont(java.lang.Object)
     */
    public FontData getFont(Object element) {
        return DebugElementHelper.getFont(element);
    }

}
