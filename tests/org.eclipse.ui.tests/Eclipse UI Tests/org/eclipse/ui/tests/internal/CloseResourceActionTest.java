/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.tests.harness.util.UITestUtil.waitForJobs;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Closing a project whose location contains further open projects.
 */
public class CloseResourceActionTest extends ResourceActionTest {

	private IProject parent;
	private IProject nested;
	private IPreferenceStore store;
	private String oldPreference;

	@Before
	public void createProjects() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		parent = workspace.getRoot().getProject("CloseResourceActionTest_parent");
		parent.create(null);
		parent.open(null);
		nested = workspace.getRoot().getProject("CloseResourceActionTest_nested");
		IProjectDescription description = workspace.newProjectDescription(nested.getName());
		description.setLocation(parent.getLocation().append(nested.getName()));
		nested.create(description, null);
		nested.open(null);
		store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		oldPreference = store.getString(IDEInternalPreferences.CLOSE_NESTED_PROJECTS);
	}

	@After
	public void deleteProjects() throws CoreException {
		store.setValue(IDEInternalPreferences.CLOSE_NESTED_PROJECTS, oldPreference);
		nested.delete(true, null);
		parent.delete(true, null);
		waitForJobs(0, 30_000);
	}

	@Test
	public void testClosesOnlySelectedProjectByPreference() {
		store.setValue(IDEInternalPreferences.CLOSE_NESTED_PROJECTS, IDEInternalPreferences.PSPM_NEVER);
		close(parent);
		assertFalse(parent.isOpen());
		assertTrue(nested.isOpen());
	}

	@Test
	public void testClosesNestedProjectsByPreference() {
		store.setValue(IDEInternalPreferences.CLOSE_NESTED_PROJECTS, IDEInternalPreferences.PSPM_ALWAYS);
		close(parent);
		assertFalse(parent.isOpen());
		assertFalse(nested.isOpen());
	}

	@Test
	public void testDoesNotPromptWhenNestedProjectIsSelected() {
		store.setValue(IDEInternalPreferences.CLOSE_NESTED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		close(parent, nested);
		assertFalse(parent.isOpen());
		assertFalse(nested.isOpen());
	}

	@Test
	public void testDoesNotPromptWithoutNestedProjects() {
		store.setValue(IDEInternalPreferences.CLOSE_NESTED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		close(nested);
		assertTrue(parent.isOpen());
		assertFalse(nested.isOpen());
	}

	private static void close(IProject... projects) {
		Shell activeShell = Display.getCurrent().getActiveShell();
		CloseResourceAction action = new CloseResourceAction(() -> activeShell);
		action.selectionChanged(new StructuredSelection(projects));
		assertTrue(action.isEnabled());
		action.run();
		processUIEvents();
		waitForJobs(0, 30_000);
	}
}
