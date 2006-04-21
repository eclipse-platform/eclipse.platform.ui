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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStep;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Default step adapter for standard debug model.
 * 
 * @since 3.2
 */
public class StepAdapter implements IAsynchronousStepAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepAdapter#canStepInto(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void canStepInto(final Object element, final IBooleanRequestMonitor requestMonitor) {
		Job job = new Job("canStepInto") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStep step = getTarget(element);
				if (step != null)
					requestMonitor.setResult(step.canStepInto());
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
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepAdapter#canStepOver(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void canStepOver(final Object element, final IBooleanRequestMonitor requestMonitor) {
		Job job = new Job("canStepOver") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStep step = getTarget(element);
				if (step != null)
					requestMonitor.setResult(step.canStepOver());
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
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepAdapter#canStepReturn(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void canStepReturn(final Object element, final IBooleanRequestMonitor requestMonitor) {
		Job job = new Job("canStepReturn") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStep step = getTarget(element);
				if (step != null)
					requestMonitor.setResult(step.canStepReturn());
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
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepAdapter#isStepping(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void isStepping(final Object element, final IBooleanRequestMonitor requestMonitor) {
		Job job = new Job("isStepping") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStep step = getTarget(element);
				if (step != null)
					requestMonitor.setResult(step.isStepping());
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
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepAdapter#stepInto(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor)
	 */
	public void stepInto(final Object element, final IAsynchronousRequestMonitor requestMonitor) {
		Job job = new Job("stepInto") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStep step = getTarget(element);
				if (step != null)
				{
					try {
						step.stepInto();
					} catch (DebugException e) {
						requestMonitor.setStatus(e.getStatus());
					}
				}
				else
				{
					requestMonitor.setStatus(new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID,
                			IDebugUIConstants.INTERNAL_ERROR,
                			"element must be an instance of or adapt to IStep", //$NON-NLS-1$
                			null));
				}
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepAdapter#stepOver(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor)
	 */
	public void stepOver(final Object element, final IAsynchronousRequestMonitor requestMonitor) {
		Job job = new Job("stepOver") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStep step = getTarget(element);
				if (step != null)
				{
					try {
						step.stepOver();
					} catch (DebugException e) {
						requestMonitor.setStatus(e.getStatus());
					}
				}
				else
				{
					requestMonitor.setStatus(new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID,
                			IDebugUIConstants.INTERNAL_ERROR,
                			"element must be an instance of or adapt to IStep", //$NON-NLS-1$
                			null));
				}
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousStepAdapter#stepReturn(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor)
	 */
	public void stepReturn(final Object element, final IAsynchronousRequestMonitor requestMonitor) {
		Job job = new Job("stepReturn") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IStep step = getTarget(element);
				if (step != null)
				{
					try {
						step.stepReturn();
					} catch (DebugException e) {
						requestMonitor.setStatus(e.getStatus());
					}
				}
				else
				{
					requestMonitor.setStatus(new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID,
                			IDebugUIConstants.INTERNAL_ERROR,
                			"element must be an instance of or adapt to IStep", //$NON-NLS-1$
                			null));
				}
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();

	}
	
	private IStep getTarget(Object element)
	{
		 if (element instanceof IStep) {
				return (IStep) element;
			} else if (element instanceof IAdaptable) {
				return (IStep) ((IAdaptable)element).getAdapter(IStep.class);
			}
	        return null;		
	}

}
