/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.MDirtyable;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

public class SaveHandler {

	public boolean canExecute(EPartService partService) {
		MPart details = partService.findPart("DetailsView");
		return details.isDirty();
	}

	public void execute(
			IEclipseContext context, @Optional IStylingEngine engine,
			@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			final EPartService partService)
			throws InvocationTargetException, InterruptedException {
		final IEclipseContext pmContext = EclipseContextFactory.create(context,
				null);

		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		dialog.open();
		
		ThemeUtil.applyDialogStyles(engine, dialog.getShell());
		
		dialog.run(true, true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				pmContext.set(IProgressMonitor.class.getName(), monitor);
				MPart details = partService.findPart("DetailsView");
				Object clientObject = details.getObject();
				ContextInjectionFactory.invoke(clientObject, "doSave", //$NON-NLS-1$
						pmContext, null);
			}
		});
		
		if (pmContext instanceof IDisposable) {
			((IDisposable) pmContext).dispose();
		}
	}

}
