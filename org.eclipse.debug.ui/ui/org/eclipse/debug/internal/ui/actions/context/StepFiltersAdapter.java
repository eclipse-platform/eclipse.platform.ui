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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IStepFilters;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFilters;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;

/**
 * Default step filters adapter for standard debug model.
 * 
 * @since 3.2
 */
public class StepFiltersAdapter implements IAsynchronousStepFilters {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFilters#supportsStepFilters(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void supportsStepFilters(final Object element, final IBooleanRequestMonitor requestMonitor) {
		Assert.isTrue(element instanceof IDisconnect, "element must be instance of IStepFilters"); //$NON-NLS-1$
		Job job = new Job("supportsStepFilters") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStepFilters filters = (IStepFilters) element;
				requestMonitor.setResult(filters.supportsStepFilters());
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFilters#isStepFiltersEnabled(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void isStepFiltersEnabled(final Object element, final IBooleanRequestMonitor requestMonitor) {
		Assert.isTrue(element instanceof IDisconnect, "element must be instance of IStepFilters"); //$NON-NLS-1$
		Job job = new Job("isStepFiltersEnabled") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStepFilters filters = (IStepFilters) element;
				requestMonitor.setResult(filters.isStepFiltersEnabled());
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepFilters#setStepFiltersEnabled(java.lang.Object, boolean, org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor)
	 */
	public void setStepFiltersEnabled(final Object element, final boolean enabled, final IAsynchronousRequestMonitor requestMonitor) {
		Assert.isTrue(element instanceof IDisconnect, "element must be instance of IStepFilters"); //$NON-NLS-1$
		Job job = new Job("setStepFiltersEnabled") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStepFilters filters = (IStepFilters) element;
				filters.setStepFiltersEnabled(enabled);
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

}
