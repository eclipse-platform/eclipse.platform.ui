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
package org.eclipse.update.internal.scheduler;

import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.operations.UpdateUtils;
import org.eclipse.update.internal.search.UpdatesSearchCategory;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.operations.*;
import org.eclipse.update.search.*;

public class AutomaticUpdatesJob
	extends Job
	implements IUpdateSearchResultCollector {
	private static final IStatus OK_STATUS =
		new Status(
			IStatus.OK,
			UpdateScheduler.getPluginId(),
			IStatus.OK,
			"",
			null);
	private UpdateSearchRequest searchRequest;
	private ArrayList updates;

	public AutomaticUpdatesJob() {
		updates = new ArrayList();
		setPriority(Job.DECORATE);
	}

	public void accept(IFeature feature) {
		IInstallFeatureOperation operation =
			(IInstallFeatureOperation) OperationsManager
				.getOperationFactory()
				.createInstallOperation(null, null, feature, null, null, null);
		updates.add(operation);
	}

	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated disaply. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display;
		display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}

	public IStatus run(IProgressMonitor monitor) {
		System.out.println("Automatic update search started.");
		UpdateSearchScope scope = new UpdateSearchScope();
		scope.setUpdateMapURL(UpdateUtils.getUpdateMapURL());
		UpdatesSearchCategory category = new UpdatesSearchCategory();
		searchRequest = new UpdateSearchRequest(category, scope);
		searchRequest.addFilter(new EnvironmentFilter());
		try {
			searchRequest.performSearch(this, monitor);
			System.out.println(
				"Automatic update search finished - "
					+ updates.size()
					+ " results.");
			if (updates.size() > 0) {
				// prompt the user
				getStandardDisplay().asyncExec(new Runnable() {
					public void run() {
						asyncNotifyUser();
					}
				});
				return Job.ASYNC_FINISH;
			}
		} catch (CoreException e) {
			return e.getStatus();
		}
		return OK_STATUS;
	}

	private void asyncNotifyUser() {
		// ask the user to install updates
		getStandardDisplay().beep();
		if (MessageDialog
			.openQuestion(
				UpdateScheduler.getActiveWorkbenchShell(),
				"Eclipse Updates",
				"New updates are available. Do you want to review and install them now?")) {
			BusyIndicator.showWhile(getStandardDisplay(), new Runnable() {
				public void run() {
					openInstallWizard();
				}
			});
		}
		// notify the manager that the job is done
		done(OK_STATUS);
	}

	private void openInstallWizard() {
		InstallWizard wizard = new InstallWizard(searchRequest, updates);
		WizardDialog dialog =
			new ResizableWizardDialog(
				UpdateScheduler.getActiveWorkbenchShell(),
				wizard);
		dialog.create();
		dialog.getShell().setText("Updates");
		dialog.getShell().setSize(600, 500);
		dialog.open();
		if (wizard.isSuccessfulInstall())
			UpdateUI.requestRestart();
	}
}