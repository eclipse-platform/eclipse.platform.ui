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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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
import org.eclipse.ui.internal.ide.misc.ProjectReferenceGraph;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
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
		BlockingReferenceProvider.release();
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

	/**
	 * Selects the given projects and waits until enablement reflects an up-to-date
	 * project reference graph. The graph is computed in a background job, so the
	 * first selection change may still answer from the previous graph.
	 */
	private static void select(CloseUnrelatedProjectsAction action, Object... selected) throws InterruptedException {
		refreshGraph();
		action.selectionChanged(new StructuredSelection(selected));
	}

	/**
	 * Rebuilds the shared graph and fails if it did not end up current, so that a
	 * later enablement assertion cannot fail for that reason instead.
	 */
	private static void refreshGraph() throws InterruptedException {
		assertTrue(ProjectReferenceGraph.getInstance().refresh(null),
				"the project reference graph did not become current");
	}

	/**
	 * Waits for the background job that {@link CloseUnrelatedProjectsAction#run()}
	 * starts. While it runs, the workspace defers its change notifications, and the
	 * graph would not see projects the test creates in the meantime.
	 */
	private static void waitForClosed(IProject... projects) {
		assertTrue(DisplayHelper.waitForCondition(Display.getDefault(), 10000,
				() -> Stream.of(projects).noneMatch(IProject::isOpen)), "projects were not closed");
	}

	private static void processUIEvents() {
		Display display = Display.getDefault();
		while (display.readAndDispatch()) {
			// drain the queue so asynchronous enablement updates are applied
		}
	}

	@Test
	public void testDisabledAfterAllUnrelatedProjectsClosedAndSelectionChanges() throws Exception {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);

		select(action, a);
		assertTrue(action.isEnabled(), "action must be enabled while unrelated open project C exists");

		c.close(null);

		select(action, b);
		assertFalse(action.isEnabled(), "action must be disabled when no unrelated open project remains");
	}

	@Test
	public void testDisabledAfterAllUnrelatedProjectsAreDeleted() throws Exception {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);

		select(action, a);
		assertTrue(action.isEnabled(), "action must be enabled while unrelated open project C exists");

		c.delete(true, true, null);

		select(action, a);
		assertFalse(action.isEnabled(), "action must be disabled when no unrelated open project remains");
	}

	@Test
	public void testDoNotCloseDeleted() throws Exception {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);

		select(action, a);
		assertTrue(action.isEnabled(), "action must be enabled while unrelated open project C exists");

		c.delete(true, true, null);

		select(action, a);
		assertFalse(action.isEnabled(), "action must be disabled when no unrelated open project remains");
		action.run(); // should not throw
	}

	@Test
	public void testDisabledAfterUnrelatedProjectCreated() throws Exception {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);
		c.close(null);
		select(action, b);
		assertFalse(action.isEnabled(), "action must be disabled when no unrelated open project remains");

		d.create(null);
		d.open(null);

		select(action, b);
		assertTrue(action.isEnabled(), "action must be enabled unrelated project is created");
	}

	@Test
	public void testEnabledAfterDeleteAndReopen() throws Exception {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);
		select(action, b);
		assertTrue(action.isEnabled(), "action must be enabled when and unrelated open project remains");
		action.run(); // should not throw
		waitForClosed(c);

		d.create(null);
		d.open(null);

		select(action, b);
		assertTrue(action.isEnabled(), "action must be enabled when unrelated project is created");

		d.delete(true, true, null);
		c.open(null);

		select(action, b);
		assertTrue(action.isEnabled(), "action must be enabled when unrelated project is reopened");
		action.run(); // should not throw
	}

	@Test
	public void testDisabledAfterRunAndUnrelatedProjectCreated() throws Exception {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);
		select(action, b);
		assertTrue(action.isEnabled(), "action must be enabled while unrelated open projects exist");
		action.run();
		waitForClosed(c);

		d.create(null);
		d.open(null);

		select(action, b);
		assertTrue(action.isEnabled(), "action must be enabled unrelated project is created");
	}

	@Test
	public void testDisabledAfterAllUnrelatedProjectsClosed() throws Exception {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);

		select(action, a);
		assertTrue(action.isEnabled(), "action must be enabled while unrelated open project C exists");

		c.close(null);

		select(action, b);
		assertFalse(action.isEnabled(), "action must be disabled when no unrelated open project remains");
	}

	@Test
	public void testEnabledWhenUnrelatedOpenProjectExists() throws Exception {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);

		select(action, a);
		assertTrue(action.isEnabled(), "expected enabled when unrelated open project C exists");

		select(action, b);
		assertTrue(action.isEnabled(), "expected enabled when unrelated open project C exists (selection B)");
	}

	@Test
	public void testDisabledWhenSelectionCoversAllOpenProjects() throws Exception {
		c.close(null);
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);
		select(action, a, b);
		assertFalse(action.isEnabled(),
				"action must be disabled when selection plus its references covers all open projects");
	}

	/**
	 * A selection change must never resolve project references itself: it answers
	 * from the shared graph and corrects enablement once the background rebuild
	 * finished.
	 */
	@Test
	public void testEnablementCorrectedAfterBackgroundRebuild() throws Exception {
		c.close(null);
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);
		select(action, b);
		assertFalse(action.isEnabled(), "action must be disabled when no unrelated open project remains");

		d.create(null);
		d.open(null);

		// A single selection change, without waiting for the graph first: it sees the
		// previous graph, which still has no open unrelated project.
		action.selectionChanged(new StructuredSelection(b));
		assertFalse(action.isEnabled(), "enablement must answer from the cached graph without blocking");

		refreshGraph();
		processUIEvents();
		assertTrue(action.isEnabled(), "enablement must be corrected once the graph rebuild finished");
	}

	/**
	 * Mirrors the RCPTT test CloseUnrelatedProjectsInQ7Explorer, which broke when
	 * enablement was made optimistic in the past: add a project reference, close the
	 * unrelated projects, then check that the action is disabled for both related
	 * projects. Nothing here refreshes the shared graph, so the action has to reach
	 * the right answer on its own.
	 */
	@Test
	public void testCloseUnrelatedAfterProjectReferenceAdded() throws Exception {
		d.create(null);
		d.open(null);
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);
		select(action, c);

		// C now references D, so closing what is unrelated to C must spare D. A graph
		// that still predates this change would close D as well.
		IProjectDescription cDesc = c.getDescription();
		cDesc.setReferencedProjects(new IProject[] { d });
		c.setDescription(cDesc, null);

		action.selectionChanged(new StructuredSelection(c));
		assertTrue(action.isEnabled(), "action must be enabled while unrelated open projects A and B exist");
		action.run();

		assertTrue(DisplayHelper.waitForCondition(Display.getDefault(), 10000, () -> !a.isOpen() && !b.isOpen()),
				"the unrelated projects A and B must be closed");
		assertTrue(c.isOpen(), "selected project C must stay open");
		assertTrue(d.isOpen(), "project D referenced by C must stay open");

		action.selectionChanged(new StructuredSelection(c));
		assertFalse(action.isEnabled(), "action must be disabled once no unrelated open project remains");

		action.selectionChanged(new StructuredSelection(d));
		assertFalse(action.isEnabled(), "action must be disabled for the referenced project as well");
	}

	/**
	 * A workspace change while the rebuild runs leaves it with components that are
	 * stale on arrival. The graph has to start the rebuild that converges, because
	 * invalidating it only marks the components stale, and the caller is waiting
	 * for the callback rather than asking again.
	 */
	@Test
	public void testCallbackRunsWhenGraphInvalidatedDuringRebuild() throws Exception {
		BlockingReferenceProvider.addTo(a);
		refreshGraph();

		AtomicInteger callbackRuns = new AtomicInteger();
		ProjectReferenceGraph graph = ProjectReferenceGraph.getInstance();

		BlockingReferenceProvider.arm();
		a.clearCachedDynamicReferences();
		d.create(null);
		d.open(null);
		graph.getComponents(callbackRuns::incrementAndGet);

		// the rebuild now sits in the reference lookup of A, so this change reaches
		// the graph before the rebuild publishes what it computed without it
		BlockingReferenceProvider.awaitBlocked();
		c.close(null);
		BlockingReferenceProvider.release();

		assertTrue(DisplayHelper.waitForCondition(Display.getDefault(), 10000, () -> callbackRuns.get() > 0),
				"the callback must run even when the graph is invalidated while the rebuild is running");
	}

	/**
	 * Closing a project invalidates the shared graph, so the next selection change
	 * schedules a rebuild rather than reusing the stale components.
	 */
	@Test
	public void testGraphInvalidatedOnProjectClose() throws Exception {
		CloseUnrelatedProjectsAction action = new CloseUnrelatedProjectsAction(() -> shell);

		select(action, a);
		assertTrue(action.isEnabled(), "action must be enabled while unrelated open project C exists");

		c.close(null);

		select(action, a);
		assertFalse(action.isEnabled(), "action must be disabled after the graph was rebuilt without open C");
	}
}
