/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.scheduler;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.UpdateUIMessages;
import org.eclipse.update.internal.ui.wizards.InstallWizard;
import org.eclipse.update.internal.ui.wizards.InstallWizard2;
import org.eclipse.update.internal.ui.wizards.ResizableInstallWizardDialog;

class UpdateJobChangeAdapter extends JobChangeAdapter {
	private SchedulerStartup startup;

	public UpdateJobChangeAdapter(SchedulerStartup startup) {
		this.startup = startup;
	}

	public void done(IJobChangeEvent event) {
		if (event.getJob() == startup.getJob()) {

			// prompt the user
			if (((AutomaticUpdateJob) startup.getJob()).getUpdates().length > 0
					&& !InstallWizard.isRunning()) {
				if (UpdateSchedulerPlugin.getDefault().getPluginPreferences()
						.getBoolean(UpdateSchedulerPlugin.P_DOWNLOAD)) {
					UpdateUI.getStandardDisplay().asyncExec(new Runnable() {
						public void run() {
							asyncNotifyDownloadUser();
							startup.scheduleUpdateJob();
						}
					});
				} else {
					UpdateUI.getStandardDisplay().asyncExec(new Runnable() {
						public void run() {
							asyncNotifyUser();
							startup.scheduleUpdateJob();
						}
					});
				}
			}
		}
	}

	private void asyncNotifyUser() {
		// ask the user to install updates
		UpdateUI.getStandardDisplay().beep();
		if (MessageDialog.openQuestion(UpdateUI.getActiveWorkbenchShell(),
				UpdateUIMessages.AutomaticUpdatesJob_EclipseUpdates1,
				UpdateUIMessages.AutomaticUpdatesJob_UpdatesAvailable)) {
			BusyIndicator.showWhile(UpdateUI.getStandardDisplay(),
					new Runnable() {
						public void run() {
							openInstallWizard2();
						}
					});
		}
		// notify the manager that the job is done
		// job.done(Status.OK_STATUS);
	}

	private void asyncNotifyDownloadUser() {
		// ask the user to install updates
		UpdateUI.getStandardDisplay().beep();
		if (MessageDialog.openQuestion(UpdateUI.getActiveWorkbenchShell(),
				UpdateUIMessages.AutomaticUpdatesJob_EclipseUpdates2,
				UpdateUIMessages.AutomaticUpdatesJob_UpdatesDownloaded)) {
			BusyIndicator.showWhile(UpdateUI.getStandardDisplay(),
					new Runnable() {
						public void run() {
							openInstallWizard2();
						}
					});
		} else {
			// Don't discard downloaded data, as next time we compare
			// timestamps.

			// discard all the downloaded data from cache (may include old
			// data as well)
			// Utilities.flushLocalFile();
		}
		// notify the manager that the job is done
		// job.done(Status.OK_STATUS);
	}

	private void openInstallWizard2() {
		if (InstallWizard.isRunning())
			// job ends and a new one is rescheduled
			return;
		AutomaticUpdateJob ujob = (AutomaticUpdateJob) startup.getJob();
		InstallWizard2 wizard = new InstallWizard2(ujob.getSearchRequest(),
				ujob.getUpdates(), true);
		WizardDialog dialog = new ResizableInstallWizardDialog(UpdateUI
				.getActiveWorkbenchShell(), wizard,
				UpdateUIMessages.AutomaticUpdatesJob_Updates);
		dialog.create();
		dialog.open();
	}

}
