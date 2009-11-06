/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.demo.contacts.handlers;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.MDirtyable;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

public class SaveHandler {

	public boolean canExecute(
			@Named(IServiceConstants.ACTIVE_PART) MDirtyable dirtyable) {
		return dirtyable.isDirty();
	}

	public void execute(
			IEclipseContext context,
			@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			@Named(IServiceConstants.ACTIVE_PART) final MContribution contribution)
			throws InvocationTargetException, InterruptedException {
		final IEclipseContext pmContext = EclipseContextFactory.create(context,
				null);
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		dialog.open();
		dialog.run(true, true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				pmContext.set(IProgressMonitor.class.getName(), monitor);
				Object clientObject = contribution.getObject();
				ContextInjectionFactory.invoke(clientObject, "doSave", //$NON-NLS-1$
						pmContext, null);
			}
		});
		
		if (pmContext instanceof IDisposable) {
			((IDisposable) pmContext).dispose();
		}
	}

}
