/*******************************************************************************
 * Copyright (c) 2019 IBM Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.tests.harness.util.UITestUtil.waitForJobs;
import static org.junit.Assert.assertNotEquals;

import java.io.ByteArrayInputStream;

import org.eclipse.core.internal.resources.mapping.ModelProviderManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ModelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CloseResourceAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * Bug 549139 - DeleteResourceAction should check with registered ModelProviders
 * before deleting resources
 */
public class Bug549139Test extends ResourceActionTest {

	private static final String TEST_MANAGER_ID = "org.eclipse.ui.tests.org.eclipse.ui.tests.internal.Bug549139Test.TestModelProvider";

	@Rule
	public TestName name = new TestName();

	public static class TestModelProvider extends ModelProvider {

		private String lastValidatedResourceName;

		@Override
		public IStatus validateChange(IResourceDelta delta, IProgressMonitor monitor) {
			IResource resource = delta.getResource();
			lastValidatedResourceName = resource.getName();
			return super.validateChange(delta, monitor);
		}

		public String getLastValidatedResourceName() {
			return lastValidatedResourceName;
		}
	}

	private IProject testProject;

	public Bug549139Test() {
		super();
	}

	@Before
	public void setUp2() throws Exception {
		testProject = createTestProject(getName());
	}

	@After
	public void tearDown2() throws Exception {
		if (testProject.exists()) {
			boolean force = true;
			testProject.delete(force, new NullProgressMonitor());
		}
		waitForJobs(0, 30_000);
	}

	/**
	 * Registers a model provider, creates a project with some files and closes it.
	 * Assert that the model provider was asked to confirm the close.
	 */
	@Test
	public void testCloseChecksWithModelProvider() throws Throwable {
		String testProjectName = getName();

		TestModelProvider testModelProvider = getTestModelManager();

		String lastValidatedResourceName = testModelProvider.getLastValidatedResourceName();
		assertNotEquals("expected test ModelProvider to not be called yet", testProjectName, lastValidatedResourceName);

		runCloseActionOnproject();

		lastValidatedResourceName = testModelProvider.getLastValidatedResourceName();
		assertNotEquals("expected test ModelProvider to be called after close action", testProjectName,
				lastValidatedResourceName);
	}

	private void runCloseActionOnproject() {
		IStructuredSelection selection = getTestProjectSelection();
		Shell activeShell = Display.getCurrent().getActiveShell();
		CloseResourceAction closeAction = new CloseResourceAction(() -> activeShell);
		closeAction.setEnabled(true);
		closeAction.selectionChanged(selection);
		closeAction.run();
		processUIEvents();
		waitForJobs(0, 30_000);
	}

	/**
	 * Registers a model provider, creates a project with some files and deletes it.
	 * Assert that the model provider was asked to confirm the delete.
	 */
	@Test
	public void testDeleteChecksWithModelProvider() throws Throwable {
		String testProjectName = getName();

		TestModelProvider testModelProvider = getTestModelManager();

		String lastValidatedResourceName = testModelProvider.getLastValidatedResourceName();
		assertNotEquals("expected test ModelProvider to not be called yet", testProjectName, lastValidatedResourceName);

		runDeleteActionOnproject();

		lastValidatedResourceName = testModelProvider.getLastValidatedResourceName();
		assertNotEquals("expected test ModelProvider to be called after delete action", testProjectName,
				lastValidatedResourceName);
	}

	private String getName() {
		return name.getMethodName();
	}

	private void runDeleteActionOnproject() {
		IStructuredSelection selection = getTestProjectSelection();
		Shell activeShell = Display.getCurrent().getActiveShell();
		TestDeleteResourceAction deleteAction = new TestDeleteResourceAction(() -> activeShell);
		deleteAction.setEnabled(true);
		deleteAction.selectionChanged(selection);
		deleteAction.run();
		processUIEvents();
		joinDeleteResourceActionJobs();
	}

	private IStructuredSelection getTestProjectSelection() {
		TreePath selectionPath = new TreePath(new Object[] { testProject });
		IStructuredSelection selection = new TreeSelection(selectionPath);
		return selection;
	}

	private static TestModelProvider getTestModelManager() throws CoreException {
		ModelProviderManager manager = ModelProviderManager.getDefault();
		TestModelProvider testModelProvider = (TestModelProvider) manager.getModelProvider(TEST_MANAGER_ID);
		return testModelProvider;
	}

	private static IProject createTestProject(String testProjectName) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject testProject = workspace.getRoot().getProject(testProjectName);
		testProject.create(null);
		testProject.open(null);

		IFile textFile = testProject.getFile("some_text_file.txt");
		ByteArrayInputStream inputStream = new ByteArrayInputStream("some text content".getBytes());
		boolean force = true;
		textFile.create(inputStream, force, new NullProgressMonitor());
		return testProject;
	}
}
