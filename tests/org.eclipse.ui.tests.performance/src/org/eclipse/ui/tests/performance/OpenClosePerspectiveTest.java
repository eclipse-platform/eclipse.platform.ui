/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/

package org.eclipse.ui.tests.performance;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OpenClosePerspectiveTest extends BasicPerformanceTest {

	private final String id;

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { EmptyPerspective.PERSP_ID2, BasicPerformanceTest.NONE }, {
				UIPerformanceTestSetup.PERSPECTIVE1,
				BasicPerformanceTest.LOCAL },
				{ "org.eclipse.ui.resourcePerspective", BasicPerformanceTest.NONE },
				{ "org.eclipse.jdt.ui.JavaPerspective", BasicPerformanceTest.NONE },
				{ "org.eclipse.debug.ui.DebugPerspective", BasicPerformanceTest.NONE } });
	}

	public OpenClosePerspectiveTest(String id, int tagging) {
		super("testOpenClosePerspectives:" + id, tagging);
		this.id = id;
	}

	@Test
	public void test() throws Throwable {
		// Get the two perspectives to switch between.
		final IPerspectiveRegistry registry = WorkbenchPlugin.getDefault()
				.getPerspectiveRegistry();
		final IPerspectiveDescriptor perspective1 = registry
				.findPerspectiveWithId(id);

		// Don't fail if we reference an unknown perspective ID. This can be
		// a normal occurrance since the test suites reference JDT perspectives, which
		// might not exist.
		if (perspective1 == null) {
			System.out.println("Unknown perspective id: " + id);
			return;
		}

		// create a nice clean window.
		IWorkbenchWindow window = openTestWindow();
		final IWorkbenchPage activePage = window.getActivePage();

		//causes creation of all views
		activePage.setPerspective(perspective1);
		IViewReference [] refs = activePage.getViewReferences();
		//get the IDs now - after we close hte perspective the view refs will be partiall disposed and their IDs will be null
		String [] ids = new String[refs.length];
		for (int i = 0; i < refs.length; i++) {
			ids[i] = refs[i].getId();
		}
		closePerspective(activePage);
		//populate the empty perspective with all view that will be shown in the test view
		for (String i : ids) {
			activePage.showView(i);
		}

		tagIfNecessary("UI - Open/Close " + perspective1.getLabel() + " Perspective", Dimension.ELAPSED_PROCESS);

		exercise(() -> {
			processEvents();
			EditorTestHelper.calmDown(500, 30000, 500);

			startMeasuring();
			activePage.setPerspective(perspective1);
			processEvents();
			closePerspective(activePage);
			processEvents();
			stopMeasuring();
		});

		commitMeasurements();
		assertPerformance();
	}

	private void closePerspective(IWorkbenchPage activePage) {
		IPerspectiveDescriptor persp = activePage.getPerspective();

		ICommandService commandService = getWorkbench().getService(ICommandService.class);
		Command command = commandService
				.getCommand("org.eclipse.ui.window.closePerspective");

		HashMap<String, String> parameters = new HashMap<>();
		parameters.put(IWorkbenchCommandConstants.WINDOW_CLOSE_PERSPECTIVE_PARM_ID,
				persp.getId());

		ParameterizedCommand pCommand = ParameterizedCommand.generateCommand(
				command, parameters);

		IHandlerService handlerService = getWorkbench().getService(IHandlerService.class);
		try {
			handlerService.executeCommand(pCommand, null);
		} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e1) {
		}

	}
}
