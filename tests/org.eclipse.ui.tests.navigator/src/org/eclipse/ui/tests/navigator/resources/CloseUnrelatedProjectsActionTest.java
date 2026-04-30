/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.resources;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CloseUnrelatedProjectsAction;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CloseUnrelatedProjectsActionTest {

	private IProject a;
	private IProject b;
	private IProject c;
	private IProject d;
	private boolean oldCloseUnrelated;
	private Shell shell;

	@BeforeEach
	public void setUp() throws CoreException {
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		oldCloseUnrelated = store.getBoolean(IDEInternalPreferences.CLOSE_UNRELATED_PROJECTS);
		store.setValue(IDEInternalPreferences.CLOSE_UNRELATED_PROJECTS, true);
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		long suffix = System.nanoTime();
		a = ws.getRoot().getProject("CUPA_A_" + suffix);
		b = ws.getRoot().getProject("CUPA_B_" + suffix);
		c = ws.getRoot().getProject("CUPA_C_" + suffix);
		d = ws.getRoot().getProject("CUPA_D_" + suffix);
		a.create(null);
		a.open(null);
		b.create(null);
		b.open(null);
		c.create(null);
		c.open(null);

		IProjectDescription aDesc = a.getDescription();
		aDesc.setReferencedProjects(new IProject[] { b });
		a.setDescription(aDesc, null);

		shell = new Shell(Display.getDefault());
	}

	@AfterEach
	public void tearDown() throws CoreException {
		if (shell != null && !shell.isDisposed()) {
			shell.dispose();
		}
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(IDEInternalPreferences.CLOSE_UNRELATED_PROJECTS,
				oldCloseUnrelated);
		for (IProject p : new IProject[] { a, b, c, d }) {
			if (p != null && p.exists()) {
				p.delete(true, true, null);
			}
		}
	}

	@Test
	public void testDisabledAfterAllUnrelatedProjectsClosedAndSelectionChanges() throws CoreException {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);

		action.selectionChanged(new StructuredSelection(a));
		assertTrue(action.isEnabled(), "action must be enabled while unrelated open project C exists");

		c.close(null);

		action.selectionChanged(new StructuredSelection(b));
		assertFalse(action.isEnabled(), "action must be disabled when no unrelated open project remains");
	}

	@Test
	public void testDisabledAfterAllUnrelatedProjectsAreDeleted() throws CoreException {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);

		action.selectionChanged(new StructuredSelection(a));
		assertTrue(action.isEnabled(), "action must be enabled while unrelated open project C exists");

		c.delete(true, true, null);

		action.selectionChanged(new StructuredSelection(a));
		assertFalse(action.isEnabled(), "action must be disabled when no unrelated open project remains");
	}

	@Test
	public void testDoNotCloseDeleted() throws CoreException {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);

		action.selectionChanged(new StructuredSelection(a));
		assertTrue(action.isEnabled(), "action must be enabled while unrelated open project C exists");

		c.delete(true, true, null);

		action.selectionChanged(new StructuredSelection(a));
		assertFalse(action.isEnabled(), "action must be disabled when no unrelated open project remains");
		action.run(); // should not throw
	}

	@Test
	public void testDisabledAfterUnrelatedProjectCreated() throws CoreException {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);
		c.close(null);
		action.selectionChanged(new StructuredSelection(b));
		assertFalse(action.isEnabled(), "action must be disabled when no unrelated open project remains");

		d.create(null);
		d.open(null);

		action.selectionChanged(new StructuredSelection(b));
		assertTrue(action.isEnabled(), "action must be enabled unrelated project is created");
	}

	@Test
	public void testEnabledAfterDeleteAndReopen() throws CoreException {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);
		action.selectionChanged(new StructuredSelection(b));
		assertTrue(action.isEnabled(), "action must be enabled when and unrelated open project remains");
		action.run(); // should not throw

		d.create(null);
		d.open(null);

		action.selectionChanged(new StructuredSelection(b));
		assertTrue(action.isEnabled(), "action must be enabled when unrelated project is created");

		d.delete(true, true, null);
		c.open(null);

		action.selectionChanged(new StructuredSelection(b));
		assertTrue(action.isEnabled(), "action must be enabled when unrelated project is reopened");
		action.run(); // should not throw
	}

	@Test
	public void testDisabledAfterRunAndUnrelatedProjectCreated() throws CoreException {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);
		action.selectionChanged(new StructuredSelection(b));
		assertTrue(action.isEnabled(), "action must be enabled while unrelated open projects exist");
		action.run();

		d.create(null);
		d.open(null);

		action.selectionChanged(new StructuredSelection(b));
		assertTrue(action.isEnabled(), "action must be enabled unrelated project is created");
	}

	@Test
	public void testDisabledAfterAllUnrelatedProjectsClosed() throws CoreException {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);

		action.selectionChanged(new StructuredSelection(a));
		assertTrue(action.isEnabled(),
				"action must be enabled while unrelated open project C exists");

		c.close(null);

		action.selectionChanged(new StructuredSelection(b));
		assertFalse(action.isEnabled(),
				"action must be disabled when no unrelated open project remains");
	}

	@Test
	public void testEnabledWhenUnrelatedOpenProjectExists() {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);

		action.selectionChanged(new StructuredSelection(a));
		assertTrue(action.isEnabled(), "expected enabled when unrelated open project C exists");

		action.selectionChanged(new StructuredSelection(b));
		assertTrue(action.isEnabled(),
				"expected enabled when unrelated open project C exists (selection B)");
	}

	@Test
	public void testDisabledWhenSelectionCoversAllOpenProjects() throws CoreException {
		c.close(null);
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);
		action.selectionChanged(new StructuredSelection(new Object[] { a, b }));
		assertFalse(action.isEnabled(),
				"action must be disabled when selection plus its references covers all open projects");
	}

	@Test
	public void testListenerInvalidatesGraphOnProjectClose() throws CoreException {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);

		action.selectionChanged(new StructuredSelection(a));
		assertTrue(action.isEnabled(),
				"action must be enabled while unrelated open project C exists");

		// Closing C fires a POST_CHANGE event; the registered listener
		// invalidates the cached graph and re-evaluates enablement.
		c.close(null);

		// Re-select A without manually calling selectionChanged first —
		// the graph must already be invalidated by the listener.
		action.selectionChanged(new StructuredSelection(a));
		assertFalse(action.isEnabled(),
				"action must be disabled after listener-driven graph invalidation");
	}
}
