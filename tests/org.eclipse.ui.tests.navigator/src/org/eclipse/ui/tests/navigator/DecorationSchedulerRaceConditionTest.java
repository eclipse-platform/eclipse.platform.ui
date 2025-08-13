/*******************************************************************************
 * Copyright (c) 2019, 2023 Stefan Winkler and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Winkler <stefan@winklerweb.net> - Initial contribution (bug 417255)
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Semaphore;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.eclipse.ui.tests.navigator.extension.DecorationSchedulerRaceConditionTestDecorator;
import org.eclipse.ui.tests.navigator.util.TestWorkspace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.3
 */
public class DecorationSchedulerRaceConditionTest extends NavigatorTestBase {

	private static final long TIMEOUT_DECORATOR = 2000;
	private static final long TIMEOUT_UPDATE_JOB = 500;

	private static final String DECORATION_TEXT_1 = "**1**";
	private static final String DECORATION_TEXT_2 = "**2**";
	private static final String DECORATION_TEXT_3 = "**3**";

	private static final DisplayHelper waitForP1Decoration = new DisplayHelper() {
		@Override
		protected boolean condition() {
			try {
				return DecorationSchedulerRaceConditionTestDecorator.hasP1Run(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
	};
	private static final DisplayHelper waitForP2Decoration = new DisplayHelper() {
		@Override
		protected boolean condition() {
			try {
				return DecorationSchedulerRaceConditionTestDecorator.hasP2Run(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
	};

	private IProject p1Project;
	private IProject p2Project;

	public DecorationSchedulerRaceConditionTest() {
		_navigatorInstanceId = "org.eclipse.ui.tests.navigator.OverrideTestView";
	}

	@Override
	@Before
	public void setUp() throws CoreException {
		super.setUp();

		_contentService.bindExtensions(new String[] { COMMON_NAVIGATOR_RESOURCE_EXT }, true);
		_contentService.getActivationService().activateExtensions(new String[] { COMMON_NAVIGATOR_RESOURCE_EXT }, true);

		p1Project = ResourcesPlugin.getWorkspace().getRoot().getProject(TestWorkspace.P1_PROJECT_NAME);
		p2Project = ResourcesPlugin.getWorkspace().getRoot().getProject(TestWorkspace.P2_PROJECT_NAME);
		p1Project.setSessionProperty(DecorationSchedulerRaceConditionTestDecorator.DECO_PROP, DECORATION_TEXT_1);

		IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
		manager.setEnabled("org.eclipse.ui.tests.navigator.bug417255Decorator", true);

		waitForP1Decoration.waitForCondition(Display.getCurrent(), TIMEOUT_DECORATOR); // wait for decorator to run
		DisplayHelper.sleep(Display.getCurrent(), TIMEOUT_UPDATE_JOB); // wait for update job following decoration to
																		// run

		// make sure that initial decoration ran successfully
		TreeItem[] rootItems = _viewer.getTree().getItems();
		assertEquals(TestWorkspace.P1_PROJECT_NAME + DECORATION_TEXT_1, rootItems[0].getText());
	}

	@After
	public void resetDecoratorEnablement() throws CoreException {
		IDecoratorManager manager = PlatformUI.getWorkbench().getDecoratorManager();
		manager.setEnabled("org.eclipse.ui.tests.navigator.bug417255Decorator", false);
	}

	@Test
	public void testBug417255raceConditionDuringDecoration() throws Exception {

		// now create the race condition. Change data value once
		p1Project.setSessionProperty(DecorationSchedulerRaceConditionTestDecorator.DECO_PROP, DECORATION_TEXT_2);
		p2Project.setSessionProperty(DecorationSchedulerRaceConditionTestDecorator.DECO_PROP, DECORATION_TEXT_2);

		// when the decorator is called, it will not finish until unblocked
		DecorationSchedulerRaceConditionTestDecorator.resetWait();
		DecorationSchedulerRaceConditionTestDecorator.blockDecoration();

		// this will schedule the decoration
		_viewer.update(p1Project, null);
		_viewer.update(p2Project, null);

		// -- at this point decorations **2** for p1 and p2 are scheduled.

		// wait for the decorator to run...
		waitForP1Decoration.waitForCondition(Display.getCurrent(), TIMEOUT_DECORATOR);

		// -- at this point p1 **2** decoration is calculated but the result is not yet
		// -- cached and the decorator is blocked by the semaphore in Bug417255Decorator

		// now reset the p1/p2 semaphores
		DecorationSchedulerRaceConditionTestDecorator.resetWait();
		// and unblock the decorator (it will next decorate p2 **2**)
		DecorationSchedulerRaceConditionTestDecorator.unblockDecorationOnce();

		// wait for the decorator to run...
		waitForP2Decoration.waitForCondition(Display.getCurrent(), TIMEOUT_DECORATOR);
		// -- at this point p1 **2** decoration result is cached, p2 **2** decoration is
		// -- calculated but the result is not yet cached.
		// -- and the decorator is blocked by the semaphore again

		// Now add another decoration request for p1 (which has a decoration result
		// cached)
		p1Project.setSessionProperty(DecorationSchedulerRaceConditionTestDecorator.DECO_PROP, DECORATION_TEXT_3);
		_viewer.update(p1Project, null); // this causes another decoration request to be scheduled

		// now continue with decorating
		DecorationSchedulerRaceConditionTestDecorator.resetWait();
		DecorationSchedulerRaceConditionTestDecorator.unblockDecoration();

		// we expect that the decorator now processes p1 **3**. Wait for that
		waitForP1Decoration.waitForCondition(Display.getCurrent(), TIMEOUT_DECORATOR);

		// And finally wait for decorator job to finish and the update job following
		// decoration to run
		DisplayHelper.sleep(Display.getCurrent(), TIMEOUT_UPDATE_JOB);

		TreeItem[] rootItemsAfter = _viewer.getTree().getItems();
		assertEquals(TestWorkspace.P1_PROJECT_NAME + DECORATION_TEXT_3, rootItemsAfter[0].getText());
	}

	@Test
	public void testBug417255raceConditionBeforeUpdate() throws Exception {
		Semaphore updateJobScheduled = new Semaphore(0);
		IJobChangeListener listener = new JobChangeAdapter() {
			@Override
			public void scheduled(IJobChangeEvent event) {
				event.getJob().getName().equals(WorkbenchMessages.DecorationScheduler_UpdateJobName);
				updateJobScheduled.release();
			}
		};
		Job.getJobManager().addJobChangeListener(listener);

		// now create the race condition. Change data value once
		p1Project.setSessionProperty(DecorationSchedulerRaceConditionTestDecorator.DECO_PROP, DECORATION_TEXT_2);
		// this will schedule the decoration
		_viewer.update(p1Project, null);

		// wait for decorator to run ...
		waitForP1Decoration.waitForCondition(Display.getCurrent(), TIMEOUT_DECORATOR);

		// wait for the update job to be scheduled
		updateJobScheduled.acquire();

		// now before the update job has a chance to run, update again
		p1Project.setSessionProperty(DecorationSchedulerRaceConditionTestDecorator.DECO_PROP, DECORATION_TEXT_3);
		_viewer.update(p1Project, null); // this causes another decoration request to be scheduled

		Job.getJobManager().removeJobChangeListener(listener);

		// wait for decorator to run ...
		waitForP1Decoration.waitForCondition(Display.getCurrent(), TIMEOUT_DECORATOR); // wait for decorator to run

		// wait for update job following decoration to run
		DisplayHelper.sleep(Display.getCurrent(), TIMEOUT_UPDATE_JOB);

		TreeItem[] rootItemsAfter = _viewer.getTree().getItems();
		assertEquals(TestWorkspace.P1_PROJECT_NAME + DECORATION_TEXT_3, rootItemsAfter[0].getText());
	}
}
