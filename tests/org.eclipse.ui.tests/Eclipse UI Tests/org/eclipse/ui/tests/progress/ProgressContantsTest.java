/*******************************************************************************
 * Copyright (c) 2009, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Manumitting Technologies - bug 394036
 ******************************************************************************/

package org.eclipse.ui.tests.progress;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEventsUntil;
import static org.eclipse.ui.tests.harness.util.UITestUtil.waitForJobs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IStatus;
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
import org.eclipse.ui.tests.TestPlugin;
import org.junit.Test;

public class ProgressContantsTest extends ProgressTestCase {

	/**
	 * Like {@link DummyJob} but implements {@link Job#belongsTo(Object)} so that
	 * each instance belongs to each other.
	 */
	private static class DummyFamilyJob extends DummyJob {

		public DummyFamilyJob(String name, IStatus status) {
			super(name, status);
		}

		@Override
		public boolean belongsTo(Object family) {
			if (family == null) {
				return false;
			}
			Class<?> clazz = family instanceof Class ? (Class<?>) family : family.getClass();
			return DummyFamilyJob.class.equals(clazz);
		}
	}

	@Test
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

		ProgressInfoItem item;
		int n = 5;
		while ((item = findProgressInfoItem(okJob)) == null && n-- > 0) {
			waitForJobs(100, 1000);
		}
		assertNotNull(item);
		item.executeTrigger();
		assertTrue(handler.executed);

		service.deactivateHandler(record);
	}

	@Test
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

		processEventsUntil(() -> findProgressInfoItem(okJob) != null, 3000);
		ProgressInfoItem item = findProgressInfoItem(okJob);

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

	@Test
	public void testKeepProperty() throws Exception {
		openProgressView();

		DummyJob okJob = new DummyJob("OK Job", Status.OK_STATUS);
		okJob.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		okJob.schedule();

		DummyJob warningJob = new DummyJob("Warning Job",
				new Status(IStatus.WARNING, TestPlugin.PLUGIN_ID, "Warning message"));
		warningJob.setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
		warningJob.schedule();

		processEvents();

		okJob.join();
		warningJob.join();

		processEventsUntil(() -> findProgressInfoItem(okJob) != null && findProgressInfoItem(warningJob) != null, 3000);

		boolean okJobFound = false;
		boolean warningJobFound = false;

		ProgressInfoItem[] progressInfoItems = progressView.getViewer().getProgressInfoItems();
		for (ProgressInfoItem progressInfoItem : progressInfoItems) {
			JobInfo[] jobInfos = progressInfoItem.getJobInfos();
			for (JobInfo jobInfo : jobInfos) {
				Job job = jobInfo.getJob();
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

	@Test
	public void testKeepOneProperty() throws Exception {
		openProgressView();

		List<DummyJob> jobs = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			DummyFamilyJob job = new DummyFamilyJob("OK Job " + i, Status.OK_STATUS);
			job.setProperty(IProgressConstants.KEEPONE_PROPERTY, Boolean.TRUE);
			jobs.add(job);
		}
		for (Job job : jobs) {
			job.schedule();
		}
		joinJobs(jobs, 10, TimeUnit.SECONDS);
		{
			// It is rather hard to find the perfect time to check the test result. The UI
			// updates are throttled so the kept job is not immediate available after the
			// job ends. A fixed wait time might be to short or unnecessary slow down the
			// test. Waiting for the end condition with timeout may produce false positive
			// results. It could be that we test in a moment there's only one kept element
			// and the test succeeds but UI has more pending updates and might add more kept
			// jobs which should be a test failure.
			// Solution is to add an unique job after the 'keep one' jobs are finished and
			// wait for it's appearance. The UI updates are ordered enough to be sure that
			// kept job processing is finished when this error job appears.
			DummyJob errorJob = new DummyJob("Last Job", new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, "error"));
			errorJob.schedule();
			processEventsUntil(() -> findProgressInfoItem(errorJob) != null, 3000);
		}

		assertEquals("Only one finished job should be kept in view", 1,
				countBelongingProgressItems(DummyFamilyJob.class));
		for (Job job : jobs) {
			assertTrue(job.getResult().isOK());
		}

		// This variant is optimized to test a ConcurrentModificationException or NPE.
		// It tries to stop multiple jobs with KEEPONE_PROPERTY at the same time.
		jobs.clear();
		progressView.getViewer().setMaxDisplayed(100);
		for (int i = 0; i < 20; i++) {
			DummyFamilyJob job = new DummyFamilyJob("OK Job " + i, Status.OK_STATUS);
			job.setProperty(IProgressConstants.KEEPONE_PROPERTY, Boolean.TRUE);
			job.shouldFinish = false;
			jobs.add(job);
			job.schedule();
		}
		// ensure all jobs are started before ending all at the same time
		processEventsUntil(null, 500);
		for (DummyJob job : jobs) {
			job.shouldFinish = true;
		}
		joinJobs(jobs, 10, TimeUnit.SECONDS);
		{
			DummyJob errorJob = new DummyJob("Last Job", new Status(IStatus.ERROR, TestPlugin.PLUGIN_ID, "error"));
			errorJob.schedule();
			processEventsUntil(() -> findProgressInfoItem(errorJob) != null, 3000);
		}

		assertEquals("Only one finished job should be kept in view", 1,
				countBelongingProgressItems(DummyFamilyJob.class));
		for (Job job : jobs) {
			assertTrue(job.getResult().isOK());
		}
	}

	/**
	 * Count the number of items in progress view whose represented job
	 * {@link Job#belongsTo(Object) belongs} to the given family.
	 */
	private int countBelongingProgressItems(Object family) {
		int count = 0;
		ProgressInfoItem[] progressInfoItems = progressView.getViewer().getProgressInfoItems();
		for (ProgressInfoItem progressInfoItem : progressInfoItems) {
			JobInfo[] jobInfos = progressInfoItem.getJobInfos();
			for (JobInfo jobInfo : jobInfos) {
				if (jobInfo.getJob().belongsTo(family)) {
					count++;
				}
			}
		}
		return count;
	}
}
