/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.stress;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test opening and closing of items.
 */
public class OpenCloseTest {
	private static final String ORG_ECLIPSE_RESOURCE_PERSPECTIVE = "org.eclipse.ui.resourcePerspective";

	private static final int numIterations = 10;

	private IWorkbenchWindow workbenchWindow;
	private IWorkbench workbench;
	private IWorkbenchPage page;

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Before
	public void setup() {
		workbench = PlatformUI.getWorkbench();
		workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IIntroPart intro = PlatformUI.getWorkbench().getIntroManager().getIntro();
		if (intro != null) {
			PlatformUI.getWorkbench().getIntroManager().closeIntro(intro);
		}

		try {
			PlatformUI.getWorkbench().showPerspective(ORG_ECLIPSE_RESOURCE_PERSPECTIVE, workbenchWindow);
		} catch (WorkbenchException e) {
			e.printStackTrace();
		}

		page = workbenchWindow.getActivePage();
		assertNotNull(page);
	}


	/**
	 * Test the opening and closing of a file.
	 */
	@Test
	public void testOpenCloseFile() throws CoreException {
		IWorkbenchPage page = workbenchWindow.getActivePage();
		FileUtil.createProject("TestProject");
		IProject testProject = ResourcesPlugin.getWorkspace().getRoot().getProject("TestProject"); //$NON-NLS-1$
		FileUtil.createFile("tempFile.txt", testProject);
		testProject.open(null);
		IEditorInput editorInput = new FileEditorInput(testProject.getFile("tempFile.txt"));
		IEditorPart editorPart = null;
		for (int index = 0; index < numIterations; index++) {
			editorPart = page.openEditor(editorInput, "org.eclipse.ui.DefaultTextEditor"); //$NON-NLS-1$
			page.closeEditor(editorPart, false);
		}
		FileUtil.deleteProject(testProject);
	}

	/**
	 * Test opening and closing of workbench window.
	 */
	@Test
	public void testOpenCloseWorkbenchWindow() throws WorkbenchException {
		IWorkbenchWindow secondWorkbenchWindow;
		for (int index = 0; index < numIterations; index++) {
			secondWorkbenchWindow = PlatformUI.getWorkbench()
					.openWorkbenchWindow(ResourcesPlugin.getWorkspace().getRoot());
			secondWorkbenchWindow.close();
		}
	}

	/**
	 * Test open and close of perspective.
	 */
	@Test
	public void testOpenClosePerspective() {
		ICommandService commandService = workbench.getService(ICommandService.class);
		Command command = commandService.getCommand("org.eclipse.ui.window.closePerspective");

		HashMap<String, String> parameters = new HashMap<>();
		parameters.put(IWorkbenchCommandConstants.WINDOW_CLOSE_PERSPECTIVE_PARM_ID,
				ORG_ECLIPSE_RESOURCE_PERSPECTIVE);

		ParameterizedCommand pCommand = ParameterizedCommand.generateCommand(command, parameters);

		IHandlerService handlerService = workbenchWindow.getService(IHandlerService.class);

		for (int index = 0; index < numIterations; index++) {
			try {
				PlatformUI.getWorkbench().showPerspective(ORG_ECLIPSE_RESOURCE_PERSPECTIVE, workbenchWindow);
				try {
					handlerService.executeCommand(pCommand, null);
				} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e1) {
				}
			} catch (WorkbenchException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Test open and close of view.
	 */
	@Test
	public void testOpenCloseView() throws WorkbenchException {
		IViewPart consoleView;
		IWorkbenchPage page = PlatformUI.getWorkbench().showPerspective(ORG_ECLIPSE_RESOURCE_PERSPECTIVE,
				workbenchWindow);
		for (int index = 0; index < numIterations; index++) {
			consoleView = page.showView(IPageLayout.ID_MINIMAP_VIEW);
			page.hideView(consoleView);
		}
	}

	/**
	 * Test open and close intro.
	 */
	@Test
	public void testOpenCloseIntro() {
		IIntroPart introPart;
		for (int index = 0; index < numIterations; index++) {
			introPart = PlatformUI.getWorkbench().getIntroManager().showIntro(workbenchWindow, false);
			PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
		}
	}
}
