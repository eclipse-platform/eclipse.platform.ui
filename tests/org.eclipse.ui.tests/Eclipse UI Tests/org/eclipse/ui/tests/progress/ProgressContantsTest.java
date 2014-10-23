/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Manumitting Technologies - bug 394036
 ******************************************************************************/

package org.eclipse.ui.tests.progress;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.progress.JobInfo;
import org.eclipse.ui.internal.progress.ProgressInfoItem;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.IProgressConstants2;

/**
 * @since 3.6
 * @author Prakash G.R. (grprakash@in.ibm.com)
 */
public class ProgressContantsTest extends ProgressTestCase {

	/**
	 *
	 * @param testName
	 *
	 */
	public ProgressContantsTest(String testName) {
		super(testName);
	}

	public void testCommandProperty() throws Exception {

		openProgressView();

		DummyJob okJob = new DummyJob("OK Job", Status.OK_STATUS);

		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandService = workbench.getService(ICommandService.class);
		String commandId = "org.eclipse.ui.tests.progressViewCommand";
		Command command = commandService.getCommand(commandId);
		ParameterizedCommand parameterizedCommand = new ParameterizedCommand(command, null);
		okJob.setProperty(IProgressConstants2.COMMAND_PROPERTY, parameterizedCommand);
		okJob.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		okJob.schedule();

		IHandlerService service = workbench.getService(IHandlerService.class);
		CommandHandler handler = new CommandHandler();
		IHandlerActivation record = service.activateHandler(commandId, handler);

		okJob.join();

		processEvents();

		ProgressInfoItem item = findProgressInfoItem(okJob);
		assertNotNull(item);
		item.executeTrigger();
		assertTrue(handler.executed);

		service.deactivateHandler(record);
	}

	public void testCommandPropertyEnablement() throws Exception {
		openProgressView();

		DummyJob okJob = new DummyJob("OK Job", Status.OK_STATUS);
		okJob.shouldFinish = false;

		IWorkbench workbench = PlatformUI.getWorkbench();
		ECommandService commandService = workbench.getService(ECommandService.class);
		String commandId = "org.eclipse.ui.tests.progressEnableViewCommand";
		// Must use ECommandService#defineCommand to have the proper legacy
		// handler hookup beforehand, such as for handler changes
		Category category = commandService.defineCategory("org.eclipse.ui.tests.progress.category", "test", "test");
		Command command = commandService.defineCommand(commandId, "test", "test", category, new IParameter[0]);
		ParameterizedCommand parameterizedCommand = new ParameterizedCommand(command, null);
		okJob.setProperty(IProgressConstants2.COMMAND_PROPERTY, parameterizedCommand);
		okJob.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		okJob.schedule();

		ProgressInfoItem item = null;
		while ((item = findProgressInfoItem(okJob)) == null) {
			// wait for the job to show up in the progress view
			processEvents();
		}

		assertNotNull(item);
		assertFalse(item.isTriggerEnabled());

		IHandlerService service = workbench.getService(IHandlerService.class);
		CommandHandler handler = new CommandHandler();
		IHandlerActivation activation = service.activateHandler(commandId, handler);
		assertTrue(item.isTriggerEnabled());

		service.deactivateHandler(activation);
		assertFalse(item.isTriggerEnabled());

		okJob.cancel();
		okJob.join();
	}

	private ProgressInfoItem findProgressInfoItem(Job job) {
		for (ProgressInfoItem progressInfoItem : progressView.getViewer().getProgressInfoItems()) {
			JobInfo[] jobInfos = progressInfoItem.getJobInfos();
			for (JobInfo jobInfo : jobInfos) {
				if (job.equals(jobInfo.getJob())) {
					return progressInfoItem;
				}
			}
		}
		return null;
	}

	// Commented out due to https://bugs.eclipse.org/bugs/show_bug.cgi?id=288358

//	public void testKeepProperty() throws Exception {
//
//		openProgressView();
//
//		DummyJob okJob = new DummyJob("OK Job", Status.OK_STATUS);
//		okJob.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
//		okJob.schedule();
//
//		DummyJob warningJob = new DummyJob("Warning Job", new Status(IStatus.WARNING, TestPlugin.PLUGIN_ID, "Warning message"));
//		warningJob.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
//		warningJob.schedule();
//
//		processEvents();
//
//		okJob.join();
//		warningJob.join();
//
//		processEvents();
//
//		boolean okJobFound = false;
//		boolean warningJobFound = false;
//
//		ProgressInfoItem[] progressInfoItems = progressView.getViewer().getProgressInfoItems();
//		for (int i = 0; i < progressInfoItems.length; i++) {
//			JobInfo[] jobInfos = progressInfoItems[i].getJobInfos();
//			for (int j = 0; j < jobInfos.length; j++) {
//				Job job = jobInfos[j].getJob();
//				if (job.equals(okJob)) {
//					okJobFound = true;
//				}
//				if (job.equals(warningJob)) {
//					warningJobFound = true;
//				}
//			}
//		}
//
//		assertTrue(okJobFound);
//		assertTrue(warningJobFound);
//	}
}
