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

package org.eclipse.ui.tests.progress;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.progress.JobInfo;
import org.eclipse.ui.internal.progress.ProgressInfoItem;
import org.eclipse.ui.internal.progress.ProgressView;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.tests.TestPlugin;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.6
 * @author Prakash G.R. (grprakash@in.ibm.com)
 */
public class ProgressContantsTest extends UITestCase {

	/**
	 * @param testName
	 */
	public ProgressContantsTest(String testName) {
		super(testName);
	}

	public void testCommandProperty() throws Exception {

		IWorkbenchWindow window = openTestWindow("org.eclipse.ui.resourcePerspective");
		ProgressView progressView = (ProgressView) window.getActivePage().showView(IPageLayout.ID_PROGRESS_VIEW);
		assertNotNull(progressView);
		processEvents();

		DummyJob okJob = new DummyJob("OK Job", Status.OK_STATUS);

		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandService = (ICommandService) workbench.getService(ICommandService.class);
		String commandId = "org.eclipse.ui.tests.progressViewCommand";
		Command command = commandService.getCommand(commandId);
		ParameterizedCommand parameterizedCommand = new ParameterizedCommand(command, null);
		okJob.setProperty(IProgressConstants.COMMAND_PROPERTY, parameterizedCommand);
		okJob.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		okJob.schedule();

		IHandlerService service = (IHandlerService) workbench.getService(IHandlerService.class);
		CommandHandler handler = new CommandHandler();
		service.activateHandler(commandId, handler);

		okJob.join();

		processEvents();

		ProgressInfoItem[] progressInfoItems = progressView.getViewer().getProgressInfoItems();
		for (int i = 0; i < progressInfoItems.length; i++) {
			JobInfo[] jobInfos = progressInfoItems[i].getJobInfos();
			for (int j = 0; j < jobInfos.length; j++) {
				Job job = jobInfos[j].getJob();
				if (job.equals(okJob)) {
					progressInfoItems[i].executeTrigger();
				}
			}
		}

		assertTrue(handler.executed);
	}

	public void testKeepProperty() throws Exception {

		IWorkbenchWindow window = openTestWindow("org.eclipse.ui.resourcePerspective");
		ProgressView progressView = (ProgressView) window.getActivePage().showView(IPageLayout.ID_PROGRESS_VIEW);
		assertNotNull(progressView);
		processEvents();

		DummyJob okJob = new DummyJob("OK Job", Status.OK_STATUS);
		okJob.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		okJob.schedule();

		DummyJob warningJob = new DummyJob("Warning Job", new Status(IStatus.WARNING, TestPlugin.PLUGIN_ID, "Warning message"));
		warningJob.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		warningJob.schedule();

		processEvents();

		okJob.join();
		warningJob.join();

		processEvents();

		boolean okJobFound = false;
		boolean warningJobFound = false;

		ProgressInfoItem[] progressInfoItems = progressView.getViewer().getProgressInfoItems();
		for (int i = 0; i < progressInfoItems.length; i++) {
			JobInfo[] jobInfos = progressInfoItems[i].getJobInfos();
			for (int j = 0; j < jobInfos.length; j++) {
				Job job = jobInfos[j].getJob();
				if (job.equals(okJob)) {
					okJobFound = true;
				}
				if (job.equals(warningJob)) {
					warningJobFound = true;
				}
			}
		}

		assertTrue(okJobFound);
		assertTrue(warningJobFound);
	}
}
