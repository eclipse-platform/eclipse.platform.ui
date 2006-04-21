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
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousDisconnectAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Default disconnect adapter for standard debug model.
 * 
 * @since 3.2
 */
public class DisconnectAdapter implements IAsynchronousDisconnectAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousDisconnectAdapter#canDisconnect(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void canDisconnect(final Object element, final IBooleanRequestMonitor requestMonitor) {
		Job job = new Job("canDisconnect") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IDisconnect disconnect = getTarget(element);
				if (disconnect != null)
					requestMonitor.setResult(disconnect.canDisconnect());
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
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousDisconnectAdapter#isDisconnected(java.lang.Object, org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor)
	 */
	public void isDisconnected(final Object element, final IBooleanRequestMonitor requestMonitor) {
		Job job = new Job("isDisconnected") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IDisconnect disconnect = getTarget(element);
				if (disconnect != null)
				{
					requestMonitor.setResult(disconnect.isDisconnected());
				}
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
	 * @see org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousDisconnectAdapter#disconnect(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor)
	 */
	public void disconnect(final Object element, final IAsynchronousRequestMonitor requestMonitor) {
		Job job = new Job("isDisconnected") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IDisconnect disconnect = getTarget(element);
				
				if (disconnect == null)
				{
					requestMonitor.setStatus(new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID,
                			IDebugUIConstants.INTERNAL_ERROR,
                			"element must be an instance of or adapt to IDisconnect", //$NON-NLS-1$
                			null));
				}
				else
				{
					try {
						disconnect.disconnect();
					} catch (DebugException e) {
						requestMonitor.setStatus(e.getStatus());
					}
				}
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}
	
	private IDisconnect getTarget(Object element)
	{
        if (element instanceof IDisconnect) {
			return (IDisconnect) element;
		} else if (element instanceof IAdaptable) {
			return (IDisconnect) ((IAdaptable)element).getAdapter(IDisconnect.class);
		}
        return null;
	}

}
