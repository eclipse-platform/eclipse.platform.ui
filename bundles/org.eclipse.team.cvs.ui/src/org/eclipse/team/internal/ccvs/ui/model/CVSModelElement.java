/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.model;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;

public abstract class CVSModelElement implements IWorkbenchAdapter {
	
	private IRunnableContext runnableContext;
	
	/**
	 * Handles exceptions that occur in CVS model elements.
	 */
	protected void handle(Throwable t) {
		CVSUIPlugin.openError(null, null, null, t, CVSUIPlugin.LOG_NONTEAM_EXCEPTIONS);
	}
	
	/**
	 * Gets the children of the receiver by invoking the <code>internalGetChildren</code>.
	 * A appropriate progress indicator will be used if requested.
	 */
	public Object[] getChildren(final Object o, boolean needsProgress, final IWorkingSet set) {
		try {
			if (needsProgress) {
				final Object[][] result = new Object[1][];
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							result[0] = CVSModelElement.this.internalGetChildren(o, set, monitor);
						} catch (TeamException e) {
							throw new InvocationTargetException(e);
						}
					}
				};
				getRunnableContext().run(true /*fork*/, true /*cancelable*/, runnable);
				return result[0];
			} else {
				return internalGetChildren(o, set, null);
			}
		} catch (InterruptedException e) {
		} catch (InvocationTargetException e) {
			handle(e);
		} catch (TeamException e) {
			handle(e);
		}
		return new Object[0];
	}
	
	/**
	 * Method internalGetChildren.
	 * @param o
	 * @return Object[]
	 */
	public Object[] internalGetChildren(Object o, IWorkingSet set, IProgressMonitor monitor) throws TeamException {
		return internalGetChildren(o, monitor);
	}
	
	/**
	 * Method internalGetChildren.
	 * @param o
	 * @return Object[]
	 */
	public abstract Object[] internalGetChildren(Object o, IProgressMonitor monitor) throws TeamException;
	
	/**
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		return getChildren(o, null);
	}

	/**
	 * Get the childen filtered by the given working set.
	 */
	public Object[] getChildren(Object o, IWorkingSet set) {
		return getChildren(o, isRemoteElement(), set);
	}
	
	/**
	 * This method should return true for any subclass that represents a remote element
	 * that requires network I/O to be fetched.
	 * 
	 * @return
	 */
	public boolean isRemoteElement() {
		return false;
	}
	
	/**
	 * Returns the runnableContext.
	 * @return IRunnableContext
	 */
	public IRunnableContext getRunnableContext() {
		if (runnableContext == null) {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		}
		return runnableContext;
	}

	/**
	 * Sets the runnableContext.
	 * @param runnableContext The runnableContext to set
	 */
	public void setRunnableContext(IRunnableContext runnableContext) {
		this.runnableContext = runnableContext;
	}

}

