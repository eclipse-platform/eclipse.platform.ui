/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
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
		service.activateHandler(commandId, handler);

		okJob.join();

		processEvents();

		ProgressInfoItem[] progressInfoItems = progressView.getViewer().getProgressInfoItems();
		for (ProgressInfoItem progressInfoItem : progressInfoItems) {
			JobInfo[] jobInfos = progressInfoItem.getJobInfos();
			for (JobInfo jobInfo : jobInfos) {
				Job job = jobInfo.getJob();
				if (job.equals(okJob)) {
					progressInfoItem.executeTrigger();
				}
			}
		}

		assertTrue(handler.executed);
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
