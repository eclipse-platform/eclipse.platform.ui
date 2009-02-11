/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.ui.ConfigurationManagerWindow;
import org.eclipse.update.internal.ui.UpdateUIMessages;
import org.eclipse.update.internal.ui.wizards.InstallWizard;
import org.eclipse.update.internal.ui.wizards.InstallWizardOperation;
import org.eclipse.update.operations.OperationsManager;
import org.eclipse.update.ui.UpdateJob;

public class FindUpdatesAction extends Action {

	private IFeature feature;

	private ConfigurationManagerWindow window;

	private class TrackingProgressMonitor extends ProgressMonitorWrapper {
		private String name;

		private String subname;

		private int totalWork;

		private double workSoFar;

		protected TrackingProgressMonitor(IProgressMonitor monitor) {
			super(monitor);
		}

		public void beginTask(String name, int totalWork) {
			this.name = name;
			this.totalWork = totalWork;
			super.beginTask(name, totalWork);
			updateStatus();
		}

		public void internalWorked(double ticks) {
			super.internalWorked(ticks);
			workSoFar += ticks;
			updateStatus();
		}

		public void subTask(String subTask) {
			subname = subTask;
			super.subTask(subTask);
			updateStatus();
		}

		private void updateStatus() {
			if (window.getShell()==null || window.getShell().isDisposed())
				return;
			if (window.isProgressCanceled())
				setCanceled(true);
			if (totalWork <= 0)
				return;
			String perc = ((int) (workSoFar * 100.0) / totalWork) + ""; //$NON-NLS-1$
			final String message = NLS.bind(UpdateUIMessages.FindUpdatesAction_trackedProgress, new String[] {
					name, subname, perc });
			Shell shell = window.getShell();
			if (shell==null || shell.isDisposed()) return;
			window.getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					window.updateStatusLine(message, null);
				}
			});
		}
	}

	private class TrackedUpdateJob extends UpdateJob {
		public TrackedUpdateJob(String name, boolean isAutomatic,
				boolean download, IFeature[] features) {
			super(name, isAutomatic, download, features);
		}

		public IStatus run(IProgressMonitor monitor) {
			return super.run(new TrackingProgressMonitor(monitor));
		}
	}

	public FindUpdatesAction(ConfigurationManagerWindow window, String text) {
		super(text);
		this.window = window;
	}

	public void setFeature(IFeature feature) {
		this.feature = feature;
	}

	public void run() {

		IStatus status = OperationsManager.getValidator()
				.validatePlatformConfigValid();
		if (status != null) {
			ErrorDialog.openError(window.getShell(), null, null, status);
			return;
		}

		// If current config is broken, confirm with the user to continue
		if (OperationsManager.getValidator().validateCurrentState() != null
				&& !confirm(UpdateUIMessages.Actions_brokenConfigQuestion))
			return;

		if (InstallWizard.isRunning()) {
			MessageDialog.openInformation(window.getShell(),
					UpdateUIMessages.InstallWizard_isRunningTitle,
					UpdateUIMessages.InstallWizard_isRunningInfo);
			return;
		}

		IFeature[] features = null;
		if (feature != null)
			features = new IFeature[] { feature };

		UpdateJob job = new TrackedUpdateJob(
				UpdateUIMessages.InstallWizard_jobName, false, false, features);

		job.setUser(true);
		job.setPriority(Job.INTERACTIVE);

		String name = feature!=null?feature.getLabel():UpdateUIMessages.FindUpdatesAction_allFeaturesSearch;
		window.trackUpdateJob(job, name);
		InstallWizardOperation operation = new InstallWizardOperation();

		operation.run(window.getShell(), job);

	}

	private boolean confirm(String message) {
		return MessageDialog.openConfirm(window.getShell(),
				UpdateUIMessages.FeatureStateAction_dialogTitle, message);
	}
}
