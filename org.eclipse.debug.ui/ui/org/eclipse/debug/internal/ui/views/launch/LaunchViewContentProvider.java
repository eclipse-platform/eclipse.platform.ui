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

 
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides content for the launch view.
 */
public class LaunchViewContentProvider implements ITreeContentProvider {

	/**
	 * @see ITreeContentProvider#getChildren(Object)
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
			DebugUIPlugin.log(e);
		}
		if (parent instanceof ILaunch) {
			return ((ILaunch)parent).getChildren();
		}
		if (parent instanceof ILaunchManager) {
			return ((ILaunchManager) parent).getLaunches();
		}
		return new Object[0];
	}

	/**
	 * @see ITreeContentProvider#getParent(Object)
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

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof IStackFrame) {
			return false;
		}
		if (element instanceof IDebugTarget) {
			try {
				return ((IDebugTarget)element).hasThreads();
			} catch (DebugException e) {
				return false;
			}
		} 
		if (element instanceof IThread) {
			try {
				return ((IThread)element).hasStackFrames();
			} catch (DebugException e) {
				return false;
			}
		}
		if (element instanceof IProcess) {
			return false;
		}
		if (element instanceof ILaunch) {
			return ((ILaunch)element).hasChildren();
		}
		if (element instanceof ILaunchManager) {
			return ((ILaunchManager) element).getLaunches().length > 0;
		}
		return false;
	}

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/**
	 * Nothing to dispose.
	 * 
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
