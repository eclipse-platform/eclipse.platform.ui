/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

package org.eclipse.ui.tests.keys;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.internal.keys.WorkbenchKeyboard;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test for Bug 44460.
 *
 * @since 3.0
 */
@Ignore("disabled since it refers to the Java builder and nature, which are not available in an RCP build")
public class Bug44460Test {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	/**
	 * Test that pressing "Ctrl+Shift+T" in the Team Synchronizing perspective
	 * does not match anything.
	 *
	 * @throws CommandException
	 *             If execution of the handler fails.
	 * @throws CoreException
	 *             If the project cannot be created or opened.
	 */
	@Test
	public void testCtrlShiftT() throws CommandException, CoreException {
		// Open a new test window.
		IWorkbenchWindow window = openTestWindow();

		// Open a new Java project, with a new class.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject testProject = workspace.getRoot().getProject(
				"Bug 44460 Project"); //$NON-NLS-1$
		testProject.create(null);
		testProject.open(null);
		IProjectDescription projectDescription = testProject.getDescription();
		String[] natureIds = { "org.eclipse.jdt.core.javanature" }; //$NON-NLS-1$
		projectDescription.setNatureIds(natureIds);
		ICommand buildCommand = new BuildCommand();
		buildCommand.setBuilderName("org.eclipse.jdt.core.javabuilder"); //$NON-NLS-1$
		projectDescription.setBuildSpec(new ICommand[] { buildCommand });
		testProject.setDescription(projectDescription, null);
		IFile javaFile = testProject.getFile("A.java"); //$NON-NLS-1$
		String classContents = "public class Main { public static main(String[] args) { ; } }"; //$NON-NLS-1$
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				classContents.getBytes());
		javaFile.create(inputStream, true, null);
		IDE.openEditor(window.getActivePage(), javaFile, true);

		// Switch to the team synchronizing perspective.
		IPerspectiveRegistry registry = PlatformUI.getWorkbench()
				.getPerspectiveRegistry();
		IPerspectiveDescriptor perspectiveDescriptor = registry
				.findPerspectiveWithId("org.eclipse.team.ui.TeamSynchronizingPerspective"); //$NON-NLS-1$
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();
		page.setPerspective(perspectiveDescriptor);

		// Press "Ctrl+Shift+T".
		Event ctrlShiftT = new Event();
		ctrlShiftT.stateMask = SWT.SHIFT | SWT.CTRL;
		ctrlShiftT.character = 'T';
		ctrlShiftT.keyCode = 't';
		List<KeyStroke> keyStrokes = WorkbenchKeyboard
				.generatePossibleKeyStrokes(ctrlShiftT);
		Workbench workbench = (Workbench) window.getWorkbench();
		BindingService support = (BindingService) workbench
				.getAdapter(IBindingService.class);
		support.getKeyboard().press(keyStrokes, null);

		// Test that only two child shells are open (default).
		Shell windowShell = window.getShell();
		Shell[] childShells = windowShell.getShells();
		assertTrue(
				"Type hierarchy dialog opened inappropriately on 'Ctrl+Shift+T'", (childShells.length == 2)); //$NON-NLS-1$
	}
}
