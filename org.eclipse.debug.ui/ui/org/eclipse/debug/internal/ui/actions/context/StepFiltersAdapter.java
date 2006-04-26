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
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStepFilters;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFiltersAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;

/**
 * Default step filters adapter for standard debug model.
 * 
 * @since 3.2
 */
public class StepFiltersAdapter extends StandardActionAdapter implements IAsynchronousStepFiltersAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFiltersAdapter#supportsStepFilters(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void supportsStepFilters(final Object element, final IBooleanRequestMonitor requestMonitor) {
		Job job = new Job("supportsStepFilters") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				boolean supported = false;
				IDebugTarget[] debugTargets = getDebugTargets(element);
				for (int i=0; i<debugTargets.length; i++)
				{
					IStepFilters filters = getTarget(debugTargets[i]);
					
					if (filters != null)
					{
						if (filters.supportsStepFilters())
							supported = true;
					}
					
					// only one needs to support it
					if (supported)
						break;
						
				}
				
				requestMonitor.setResult(supported);
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setRule(createUpdateSchedulingRule());
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFiltersAdapter#isStepFiltersEnabled(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void isStepFiltersEnabled(final Object element, final IBooleanRequestMonitor requestMonitor) {
		Job job = new Job("isStepFiltersEnabled") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IDebugTarget[] debugTargets = getDebugTargets(element);
				
				for (int i=0; i<debugTargets.length; i++)
				{
					IStepFilters filters = getTarget(element);
					
					if (filters != null)
						requestMonitor.setResult(filters.isStepFiltersEnabled());
					else
					{
						requestMonitor.setResult(false);
					}
				}
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setRule(createUpdateSchedulingRule());
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFiltersAdapter#setStepFiltersEnabled(java.lang.Object, boolean, org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor)
	 */
	public void setStepFiltersEnabled(final Object element, final boolean enabled, final IAsynchronousRequestMonitor requestMonitor) {
		Job job = new Job("setStepFiltersEnabled") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				
				IDebugTarget[] debugTargets = getDebugTargets(element);
				for (int i=0; i<debugTargets.length; i++)
				{
					IStepFilters filters = getTarget(debugTargets[i]);
					
					if (filters != null && filters.isStepFiltersEnabled() != enabled)
						filters.setStepFiltersEnabled(enabled);
	
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
	
    private IDebugTarget[] getDebugTargets(Object element) {
        if (element instanceof IDebugElement) {
            IDebugElement debugElement = (IDebugElement) element;
            return new IDebugTarget[] {debugElement.getDebugTarget()};
        } else if (element instanceof ILaunch) {
            ILaunch launch = (ILaunch) element;
            return launch.getDebugTargets();
        } else if (element instanceof IProcess) {
            IProcess process = (IProcess) element;
            return process.getLaunch().getDebugTargets();
        } else {
            return new IDebugTarget[0];
        }
    }

}
