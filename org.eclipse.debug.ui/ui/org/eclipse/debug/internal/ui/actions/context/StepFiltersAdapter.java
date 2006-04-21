/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.IStepFilters;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFiltersAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Default step filters adapter for standard debug model.
 * 
 * @since 3.2
 */
public class StepFiltersAdapter implements IAsynchronousStepFiltersAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFiltersAdapter#supportsStepFilters(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void supportsStepFilters(final Object element, final IBooleanRequestMonitor requestMonitor) {
		Job job = new Job("supportsStepFilters") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStepFilters filters = getTarget(element);
				
				if (filters != null)
					requestMonitor.setResult(filters.supportsStepFilters());
				else
					requestMonitor.setResult(false);
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFiltersAdapter#isStepFiltersEnabled(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void isStepFiltersEnabled(final Object element, final IBooleanRequestMonitor requestMonitor) {
		Job job = new Job("isStepFiltersEnabled") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStepFilters filters = getTarget(element);
				
				if (filters != null)
					requestMonitor.setResult(filters.isStepFiltersEnabled());
				else
				{
					requestMonitor.setResult(false);
				}
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFiltersAdapter#setStepFiltersEnabled(java.lang.Object, boolean, org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor)
	 */
	public void setStepFiltersEnabled(final Object element, final boolean enabled, final IAsynchronousRequestMonitor requestMonitor) {
		Job job = new Job("setStepFiltersEnabled") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStepFilters filters = getTarget(element);
				
				if (filters != null)
					filters.setStepFiltersEnabled(enabled);
				else
				{
					requestMonitor.setStatus(new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID,
                			IDebugUIConstants.INTERNAL_ERROR,
                			"element must be an instance of or adapt to IStepFilters", //$NON-NLS-1$
                			null));
				}
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}
	
	private IStepFilters getTarget(Object element)
	{
		 if (element instanceof IStepFilters) {
			return (IStepFilters) element;
		} else if (element instanceof IAdaptable) {
			return (IStepFilters) ((IAdaptable)element).getAdapter(IStepFilters.class);
		}
        return null;
	}

}
