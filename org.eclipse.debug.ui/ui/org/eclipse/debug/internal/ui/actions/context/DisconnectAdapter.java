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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.ui.actions.provisional.IAsynchronousDisconnectAdapter;
import org.eclipse.debug.internal.ui.actions.provisional.IBooleanRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousRequestMonitor;

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
		Assert.isTrue(element instanceof IDisconnect, "element must be instance of IDisconnect"); //$NON-NLS-1$
		Job job = new Job("canDisconnect") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IDisconnect disconnect = (IDisconnect) element;
				requestMonitor.setResult(disconnect.canDisconnect());
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
		Assert.isTrue(element instanceof IDisconnect, "element must be instance of IDisconnect"); //$NON-NLS-1$
		Job job = new Job("isDisconnected") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IDisconnect disconnect = (IDisconnect) element;
				requestMonitor.setResult(disconnect.isDisconnected());
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
		Assert.isTrue(element instanceof IDisconnect, "element must be instance of IDisconnect"); //$NON-NLS-1$
		Job job = new Job("isDisconnected") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				IDisconnect disconnect = (IDisconnect) element;
				try {
					disconnect.disconnect();
				} catch (DebugException e) {
					requestMonitor.setStatus(e.getStatus());
				}
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

}
