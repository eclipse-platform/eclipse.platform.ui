/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.model;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

public abstract class CVSModelElement implements IWorkbenchAdapter, IAdaptable {
	
	private IRunnableContext runnableContext;
	private IWorkingSet workingSet;
		
	public IWorkingSet getWorkingSet() {
		return workingSet;
	}

	public void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class)
			return this;
		if ((adapter == IDeferredWorkbenchAdapter.class) && this instanceof IDeferredWorkbenchAdapter)
			return this;
		return null;
	}

	/**
	 * Returns the runnableContext.
	 * @return ITeamRunnableContext
	 */
	public IRunnableContext getRunnableContext() {
		if (runnableContext == null) {
			return PlatformUI.getWorkbench().getProgressService();
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

	public Object[] getChildren(Object o) {
		return getChildren(o, true);
	}
	
	/**
	 * Gets the children of the receiver by invoking the <code>internalGetChildren</code>.
	 * A appropriate progress indicator will be used if requested.
	 */
	protected Object[] getChildren(final Object o, boolean needsProgress) {
		try {
			if (needsProgress) {
				final Object[][] result = new Object[1][];
				IRunnableWithProgress runnable = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							result[0] = CVSModelElement.this.fetchChildren(o, monitor);
						} catch (TeamException e) {
							throw new InvocationTargetException(e);
						}
					}
				};
				getRunnableContext().run(true /*fork*/, true /*cancelable*/, runnable);
				return result[0];
			} else {
				return fetchChildren(o, null);
			}
		} catch (InterruptedException e) {
		} catch (InvocationTargetException e) {
			handle(e);
		} catch (TeamException e) {
			handle(e);
		}
		return new Object[0];
	}
	
	abstract protected Object[] fetchChildren(Object o, IProgressMonitor monitor) throws TeamException;
		
	/**
	 * Handle an exception that occurred in CVS model elements by displaying an error dialog.
	 * @param title the title of the error dialog
	 * @param description the description to be displayed
	 * @param e the exception that occurred
	 */
    protected void handle(final String title, final String description, final Throwable e) {
       CVSUIPlugin.openError(null, title, description, e, CVSUIPlugin.LOG_NONTEAM_EXCEPTIONS | CVSUIPlugin.PERFORM_SYNC_EXEC);
    }
    
    /**
     * Helper methed error handler that displayes a generic dialog title and message when displaying an error to the user.
     * @param t the exception that occurred.
     */
    protected void handle(Throwable t) {
        handle(CVSUIMessages.CVSModelElement_0, CVSUIMessages.CVSModelElement_1, t); // 
    }
    
    /**
     * Handle an exception that occurred while fetching the children for a deferred workbench adapter.
     * @param collector the collector for the adapter
     * @param e the exception that occurred
     */
    protected void handle(IElementCollector collector, Throwable t) {
        // TODO: For now, just display a dialog (see bug 65008 and 65741)
        handle(t);
    }

}
