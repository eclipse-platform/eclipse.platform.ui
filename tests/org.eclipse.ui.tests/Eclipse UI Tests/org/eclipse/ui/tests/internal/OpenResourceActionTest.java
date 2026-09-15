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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Opening a project whose location contains further closed projects.
 */
public class OpenResourceActionTest extends ResourceActionTest {

	private IProject parent;
	private IProject nested;
	private IProject referenced;
	private IPreferenceStore store;
	private String oldPreference;
	private String oldReferencedPreference;

	@Before
	public void createProjects() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		parent = workspace.getRoot().getProject("OpenResourceActionTest_parent");
		parent.create(null);
		parent.open(null);
		nested = workspace.getRoot().getProject("OpenResourceActionTest_nested");
		IProjectDescription description = workspace.newProjectDescription(nested.getName());
		description.setLocation(parent.getLocation().append(nested.getName()));
		nested.create(description, null);
		nested.open(null);
		nested.close(null);
		referenced = workspace.getRoot().getProject("OpenResourceActionTest_referenced");
		referenced.create(null);
		referenced.open(null);
		referenced.close(null);
		IProjectDescription parentDescription = parent.getDescription();
		parentDescription.setReferencedProjects(new IProject[] { referenced });
		parent.setDescription(parentDescription, null);
		parent.close(null);
		store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		oldPreference = store.getString(IDEInternalPreferences.OPEN_NESTED_PROJECTS);
		oldReferencedPreference = store.getString(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS);
		// the nested tests ask only about nested projects
		store.setValue(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS, IDEInternalPreferences.PSPM_NEVER);
	}

	@After
	public void deleteProjects() throws CoreException {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, oldPreference);
		store.setValue(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS, oldReferencedPreference);
		nested.delete(true, null);
		referenced.delete(true, null);
		parent.delete(true, null);
		waitForJobs(0, 30_000);
	}

	@Test
	public void testOpensOnlySelectedProjectByPreference() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_NEVER);
		open(parent);
		assertTrue(parent.isOpen());
		assertFalse(nested.isOpen());
	}

	@Test
	public void testOpensNestedProjectsByPreference() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_ALWAYS);
		open(parent);
		assertTrue(parent.isOpen());
		assertTrue(nested.isOpen());
	}

	@Test
	public void testDoesNotPromptWhenNestedProjectIsSelected() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		boolean[] answered = answerDialog(IDEWorkbenchMessages.OpenResourceAction_promptTitle,
				IDialogConstants.CANCEL_LABEL);
		open(parent, nested);
		assertFalse(answered[0]);
		assertTrue(parent.isOpen());
		assertTrue(nested.isOpen());
	}

	@Test
	public void testDoesNotPromptWithoutNestedProjects() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		boolean[] answered = answerDialog(IDEWorkbenchMessages.OpenResourceAction_promptTitle,
				IDialogConstants.CANCEL_LABEL);
		open(nested);
		assertFalse(answered[0]);
		assertFalse(parent.isOpen());
		assertTrue(nested.isOpen());
	}

	@Test
	public void testOpensOnlySelectedProjectWhenNotIncluded() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		boolean[] answered = answerDialog(IDEWorkbenchMessages.OpenResourceAction_promptTitle,
				IDEWorkbenchMessages.OpenResourceAction_open);
		open(parent);
		assertTrue(answered[0]);
		assertTrue(parent.isOpen());
		assertFalse(nested.isOpen());
		assertEquals(IDEInternalPreferences.PSPM_PROMPT, store.getString(IDEInternalPreferences.OPEN_NESTED_PROJECTS));
	}

	@Test
	public void testOpensNestedProjectsWhenIncluded() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		boolean[] answered = answerDialog(IDEWorkbenchMessages.OpenResourceAction_promptTitle,
				IDEWorkbenchMessages.OpenResourceAction_open, IDEWorkbenchMessages.RelatedProjectsDialog_includeNested);
		open(parent);
		assertTrue(answered[0]);
		assertTrue(parent.isOpen());
		assertTrue(nested.isOpen());
		assertEquals(IDEInternalPreferences.PSPM_PROMPT, store.getString(IDEInternalPreferences.OPEN_NESTED_PROJECTS));
	}

	@Test
	public void testRemembersToIncludeNestedProjects() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		boolean[] answered = answerDialog(IDEWorkbenchMessages.OpenResourceAction_promptTitle,
				IDEWorkbenchMessages.OpenResourceAction_open, IDEWorkbenchMessages.RelatedProjectsDialog_includeNested,
				JFaceResources.getString("MessageDialogWithToggle.defaultToggleMessage"));
		open(parent);
		assertTrue(answered[0]);
		assertTrue(parent.isOpen());
		assertTrue(nested.isOpen());
		assertEquals(IDEInternalPreferences.PSPM_ALWAYS, store.getString(IDEInternalPreferences.OPEN_NESTED_PROJECTS));
	}

	@Test
	public void testRemembersToSkipNestedProjects() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		boolean[] answered = answerDialog(IDEWorkbenchMessages.OpenResourceAction_promptTitle,
				IDEWorkbenchMessages.OpenResourceAction_open,
				JFaceResources.getString("MessageDialogWithToggle.defaultToggleMessage"));
		open(parent);
		assertTrue(answered[0]);
		assertTrue(parent.isOpen());
		assertFalse(nested.isOpen());
		assertEquals(IDEInternalPreferences.PSPM_NEVER, store.getString(IDEInternalPreferences.OPEN_NESTED_PROJECTS));
	}

	@Test
	public void testCancelChangesNothing() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		boolean[] answered = answerDialog(IDEWorkbenchMessages.OpenResourceAction_promptTitle,
				IDialogConstants.CANCEL_LABEL, IDEWorkbenchMessages.RelatedProjectsDialog_includeNested,
				JFaceResources.getString("MessageDialogWithToggle.defaultToggleMessage"));
		open(parent);
		assertTrue(answered[0]);
		assertFalse(parent.isOpen());
		assertFalse(nested.isOpen());
		assertEquals(IDEInternalPreferences.PSPM_PROMPT, store.getString(IDEInternalPreferences.OPEN_NESTED_PROJECTS));
	}

	@Test
	public void testOpensReferencedProjectsByPreference() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_NEVER);
		store.setValue(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS, IDEInternalPreferences.PSPM_ALWAYS);
		open(parent);
		assertTrue(parent.isOpen());
		assertTrue(referenced.isOpen());
		assertFalse(nested.isOpen());
	}

	@Test
	public void testOpensReferencedProjectsWhenIncluded() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_NEVER);
		store.setValue(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		boolean[] answered = answerDialog(IDEWorkbenchMessages.OpenResourceAction_promptTitle,
				IDEWorkbenchMessages.OpenResourceAction_open, IDEWorkbenchMessages.RelatedProjectsDialog_includeReferenced);
		open(parent);
		assertTrue(answered[0]);
		assertTrue(parent.isOpen());
		assertTrue(referenced.isOpen());
		assertFalse(nested.isOpen());
	}

	@Test
	public void testSkipsReferencedProjectsWhenNotIncluded() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_NEVER);
		store.setValue(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		boolean[] answered = answerDialog(IDEWorkbenchMessages.OpenResourceAction_promptTitle,
				IDEWorkbenchMessages.OpenResourceAction_open);
		open(parent);
		assertTrue(answered[0]);
		assertTrue(parent.isOpen());
		assertFalse(referenced.isOpen());
	}

	@Test
	public void testAsksBothQuestionsAtOnce() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		store.setValue(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		boolean[] answered = answerDialog(IDEWorkbenchMessages.OpenResourceAction_promptTitle,
				IDEWorkbenchMessages.OpenResourceAction_open, IDEWorkbenchMessages.RelatedProjectsDialog_includeNested,
				IDEWorkbenchMessages.RelatedProjectsDialog_includeReferenced,
				JFaceResources.getString("MessageDialogWithToggle.defaultToggleMessage"));
		open(parent);
		assertTrue(answered[0]);
		assertTrue(parent.isOpen());
		assertTrue(nested.isOpen());
		assertTrue(referenced.isOpen());
		assertEquals(IDEInternalPreferences.PSPM_ALWAYS, store.getString(IDEInternalPreferences.OPEN_NESTED_PROJECTS));
		assertEquals(IDEInternalPreferences.PSPM_ALWAYS, store.getString(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS));
	}

	@Test
	public void testCancelWithBothQuestionsChangesNothing() {
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		store.setValue(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		boolean[] answered = answerDialog(IDEWorkbenchMessages.OpenResourceAction_promptTitle,
				IDialogConstants.CANCEL_LABEL, IDEWorkbenchMessages.RelatedProjectsDialog_includeNested,
				IDEWorkbenchMessages.RelatedProjectsDialog_includeReferenced,
				JFaceResources.getString("MessageDialogWithToggle.defaultToggleMessage"));
		open(parent);
		assertTrue(answered[0]);
		assertFalse(parent.isOpen());
		assertFalse(nested.isOpen());
		assertFalse(referenced.isOpen());
		assertEquals(IDEInternalPreferences.PSPM_PROMPT, store.getString(IDEInternalPreferences.OPEN_NESTED_PROJECTS));
		assertEquals(IDEInternalPreferences.PSPM_PROMPT, store.getString(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS));
	}

	@Test
	public void testDoesNotAskWhenReferencedProjectIsOpen() throws CoreException {
		referenced.open(null);
		store.setValue(IDEInternalPreferences.OPEN_NESTED_PROJECTS, IDEInternalPreferences.PSPM_NEVER);
		store.setValue(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS, IDEInternalPreferences.PSPM_PROMPT);
		boolean[] answered = answerDialog(IDEWorkbenchMessages.OpenResourceAction_promptTitle,
				IDialogConstants.CANCEL_LABEL);
		open(parent);
		assertFalse(answered[0]);
		assertTrue(parent.isOpen());
	}

	private static void open(IProject... projects) {
		Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		OpenResourceAction action = new OpenResourceAction(() -> activeShell);
		action.selectionChanged(new StructuredSelection(projects));
		assertTrue(action.isEnabled());
		action.run();
		processUIEvents();
		waitForJobs(0, 30_000);
	}
}
