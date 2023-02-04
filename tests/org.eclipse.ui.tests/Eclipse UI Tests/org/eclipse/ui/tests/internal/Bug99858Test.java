/*******************************************************************************
 * Copyright (c) 2000, 2017, 2019 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 *     Tim Neumann <tim.neumann@advantest.com> - Bug 485167
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.views.navigator.ResourceNavigator;
import org.junit.Test;

/**
 * bug 99858 [IDE] Error upon deleting a project. Tests that our delete code no
 * longer throws a CoreException when deleting a closed project.
 *
 * @since 3.2
 */
public class Bug99858Test extends ResourceActionTest {

	public Bug99858Test() {
		super();
	}

	/**
	 * Create a project with some files, close it, and delete it. With the
	 * changes in runtime to throw a CoreException from IContainer#members(),
	 * the project won't get deleted if ReadOnlyStateChecker is not fixed.
	 *
	 * @throws Throwable
	 *             if it goes wrong
	 */
	@Test
	public void testDeleteClosedProject() throws Throwable {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject testProject = workspace.getRoot().getProject(
				"TestClosedDelete");
		testProject.create(null);
		testProject.open(null);

		String contents = "File ready for execution, sir!";
		createProjectFile(testProject, "a.txt", contents);
		createProjectFile(testProject, "b.txt", contents);

		ResourceNavigator view = (ResourceNavigator) page.showView(IPageLayout.ID_RES_NAV);
		view.setFocus();

		// get a testing version of the delete action, and set it up.
		TestDeleteResourceAction newDel = new TestDeleteResourceAction(
				view.getViewSite());
		newDel.setEnabled(true);
		TextActionHandler tmpHandler = new TextActionHandler(view.getViewSite()
				.getActionBars());
		tmpHandler.setDeleteAction(newDel);

		view.getViewSite().getActionBars().updateActionBars();

		processUIEvents();

		StructuredSelection s = new StructuredSelection(testProject);

		// close the project and update the selection events.
		testProject.close(null);
		assertFalse(testProject.isAccessible());
		view.getViewSite().getSelectionProvider().setSelection(s);
		newDel.selectionChanged(s);
		processUIEvents();

		IAction del = view.getViewSite().getActionBars()
				.getGlobalActionHandler(ActionFactory.DELETE.getId());

		assertTrue(del.isEnabled());

		// run the delete event.
		del.runWithEvent(null);

		processUIEvents();

		// the delete even ran
		assertTrue(newDel.fRan);

		joinDeleteResourceActionJobs();

		// if our project still exists, the delete failed.
		assertFalse(testProject.exists());
	}

	/**
	 * Create a quick project file, so the project has some children to delete.
	 *
	 * @param testProject
	 *            the project
	 * @param name
	 *            the filename
	 * @param contents
	 *            A small string for contents
	 * @throws CoreException
	 *             if IFile#create(...) throws an exception
	 */
	private void createProjectFile(IProject testProject, String name,
			String contents) throws CoreException {
		IFile textFile = testProject.getFile(name);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				contents.getBytes());
		textFile.create(inputStream, true, null);
	}
}
