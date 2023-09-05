/*******************************************************************************
 * Copyright (c) 2023 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.tests.rcp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IActivityManagerListener;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.tests.harness.util.RCPTestWorkbenchAdvisor;
import org.junit.Test;

public class WorkbenchTest {
	/**
	 * Tests activity manager behavior during workbench shutdown.
	 *
	 * See https://github.com/eclipse-platform/eclipse.platform.ui/issues/1084
	 */
	@Test
	public void testWorkbenchShutdownProducesNoActivityManagerEvents() {
		IActivityManagerListener activityListener = mock(IActivityManagerListener.class);
		WorkbenchAdvisor closeAfterStartupAdvisor = new RCPTestWorkbenchAdvisor() {
			@Override
			public void postStartup() {
				IWorkbench workbench = getWorkbenchConfigurer().getWorkbench();
				IActivityManager activityManager = workbench.getActivitySupport().getActivityManager();
				activityManager.addActivityManagerListener(activityListener);
				workbench.close();
			};
		};

		runWorkbench(closeAfterStartupAdvisor);

		verify(activityListener, never()).activityManagerChanged(any());
	}

	private void runWorkbench(WorkbenchAdvisor workbenchAdvisor) {
		Display display = PlatformUI.createDisplay();
		PlatformUI.createAndRunWorkbench(display, workbenchAdvisor);
	}

}
