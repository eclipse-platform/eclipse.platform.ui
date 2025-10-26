/*******************************************************************************
 * Copyright (c) 2025 Vegard IT GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Sebastian Thomschke (Vegard IT GmbH) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenFileWithReuseAction;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.EditorTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Verifies Project Explorer open behavior when the "reuse like Search" option
 * is enabled/disabled.
 */
public class OpenFileWithReuseActionTest extends NavigatorTestBase {

	public OpenFileWithReuseActionTest() {
		_navigatorInstanceId = org.eclipse.ui.navigator.resources.ProjectExplorer.VIEW_ID;
	}

	@Before
	public void before() {
		EditorTestHelper.closeAllEditors();
	}

	@After
	public void after() {
		EditorTestHelper.closeAllEditors();
	}

	private IFile createFile(IProject project, String name, String content) throws CoreException {
		IFile f = project.getFile(name);
		byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
		if (f.exists()) {
			f.setContents(bytes, true, false, null);
		} else {
			f.create(bytes, true, false, null);
		}
		return f;
	}

	@Test
	public void testReuseEnabled_singleSelectionsReuseSameEditor() throws Exception {
		// Enable preference
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");
		prefs.putBoolean(IPreferenceConstants.REUSE_LAST_OPENED_EDITOR, true);
		try {
			prefs.flush();
		} catch (Exception e) { /* ignore */ }

		// Create two simple files
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("p1");
		IFile a = createFile(project, "a.txt", "a");
		IFile b = createFile(project, "b.txt", "b");

		showNavigator();

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		var action = new OpenFileWithReuseAction(page, IPageLayout.ID_PROJECT_EXPLORER);

		// Open first file
		action.selectionChanged(new StructuredSelection(a));
		action.run();
		EditorTestHelper.runEventQueue(200);

		IEditorReference[] refs1 = page.getEditorReferences();
		assertEquals(1, refs1.length);
		IEditorReference ref1 = refs1[0];
		IEditorPart part1 = ref1.getEditor(false);
		assertNotNull(part1);
		// Sanity: editor shows first file
		assertNotNull(page.findEditor(new FileEditorInput(a)));

		// Open second file
		action.selectionChanged(new StructuredSelection(b));
		action.run();
		EditorTestHelper.runEventQueue(200);

		IEditorReference[] refs2 = page.getEditorReferences();
		assertEquals("Reused editor expected", 1, refs2.length);
		assertSame("Editor reference should be the same instance", ref1, refs2[0]);
		// Input replaced with second file
		assertNotNull(page.findEditor(new FileEditorInput(b)));
	}

	@Test
	public void testReuseDisabled_opensTwoEditors() throws Exception {
		// Disable preference
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");
		prefs.putBoolean(IPreferenceConstants.REUSE_LAST_OPENED_EDITOR, false);
		try {
			prefs.flush();
		} catch (Exception e) { /* ignore */ }

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject("p1");
		IFile a = createFile(project, "c.txt", "c");
		IFile b = createFile(project, "d.txt", "d");

		showNavigator();

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		var action = new OpenFileWithReuseAction(page, IPageLayout.ID_PROJECT_EXPLORER);

		action.selectionChanged(new StructuredSelection(a));
		action.run();
		EditorTestHelper.runEventQueue(200);

		action.selectionChanged(new StructuredSelection(b));
		action.run();
		EditorTestHelper.runEventQueue(200);

		IEditorReference[] refs = page.getEditorReferences();
		assertEquals("Two editors expected when reuse is disabled", 2, refs.length);
	}
}
