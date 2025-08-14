/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Louis Detweiler <greg.detweiler42@gmail.com> - Issue #1780
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEventsUntil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.NavigationHistoryAction;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.EditorTestHelper;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.tests.harness.util.UITestUtil.Condition;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.TextSelectionNavigationLocation;
import org.junit.Assert;
import org.junit.Test;

/**
 * @since 3.3
 *
 */
public class GoBackForwardsTest extends UITestCase {

	private static final String TEST_NAME = "GoBackForwardsTest";

	public GoBackForwardsTest() {
		super(TEST_NAME);
	}

	private static final String PROJECT_NAME = "GoBackForwardsTestProject";
	private static final String FILE_NAME = "GoBackForwardsTestFile.java";
	private static final String FILE_CONTENTS = "public class GoBackForwardsTestFile {\n"
			+ "    public static void main(String[] args) {\n" + "        System.out.println(\"Hello world!\");\n"
			+ "    }\n" + "}";
	private static final String GENERIC_EDITOR_ID = "org.eclipse.ui.genericeditor.GenericEditor";
	private static final String TEXT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor";
	private static final String SELECTION_STRING = "Selection<offset: 10, length: 5>";

	private IProject project;
	private IFile file;

	@Override
	public void doSetUp() throws CoreException, IOException {
		project = FileUtil.createProject(PROJECT_NAME);
		file = FileUtil.createFile(FILE_NAME, project);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(FILE_CONTENTS);
		Files.writeString(Paths.get(file.getLocation().toOSString()), stringBuilder);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);

	}

	@Test
	public void testNavigationHistoryNavigation() throws PartInitException {
		IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
		PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);

		processEvents();

		Condition genericEditorNoSelection = currentNavigationHistoryLocationCondition(GENERIC_EDITOR_ID, false);
		Condition genericEditorSelection = currentNavigationHistoryLocationCondition(GENERIC_EDITOR_ID, true);
		Condition textEditorNoSelection = currentNavigationHistoryLocationCondition(TEXT_EDITOR_ID, false);
		Condition textEditorSelection = currentNavigationHistoryLocationCondition(TEXT_EDITOR_ID, true);

		FileEditorInput editorInput = new FileEditorInput(file);

		openGenericEditor(editorInput);

		assertTrue("Timeout during navigation." + getStateDetails(),
				processEventsUntil(genericEditorNoSelection, 1000));

		selectInGenericEditor(editorInput);

		assertTrue("Timeout during navigation." + getStateDetails(),
				processEventsUntil(genericEditorSelection, 1000));

		openTextEditor(editorInput);

		assertTrue("Timeout during navigation." + getStateDetails(),
				processEventsUntil(textEditorNoSelection, 1000));

		selectInTextEditor(editorInput);

		assertTrue("Timeout during navigation." + getStateDetails(),
				processEventsUntil(textEditorSelection, 1000));

		openGenericEditor(editorInput);

		assertTrue("Timeout during navigation." + getStateDetails(),
				processEventsUntil(genericEditorSelection, 1000));

		openTextEditor(editorInput);

		assertTrue("Timeout during navigation." + getStateDetails(),
				processEventsUntil(textEditorSelection, 1000));

		// Navigate backward from text editor to editor
		goBackward(EditorTestHelper.getActiveWorkbenchWindow(), genericEditorSelection);
		Assert.assertEquals(
				"Failed to correctly navigate backward from text editor to java editor." + getStateDetails(),
				GENERIC_EDITOR_ID, getActiveEditorId());

		// Navigate backward from java editor to text editor
		goBackward(EditorTestHelper.getActiveWorkbenchWindow(), textEditorSelection);
		Assert.assertEquals(
				"Failed to correctly navigate backward from java editor to test editor." + getStateDetails(),
				TEXT_EDITOR_ID, getActiveEditorId());

		// Navigate backward from text editor to text editor
		goBackward(EditorTestHelper.getActiveWorkbenchWindow(), textEditorNoSelection);
		Assert.assertEquals(
				"Failed to correctly navigate backward from text editor to text editor." + getStateDetails(),
				TEXT_EDITOR_ID, getActiveEditorId());

		// Navigate backward from java editor to java editor
		goBackward(EditorTestHelper.getActiveWorkbenchWindow(), genericEditorSelection);
		goBackward(EditorTestHelper.getActiveWorkbenchWindow(), genericEditorNoSelection);
		Assert.assertEquals(
				"Failed to correctly navigate backward from java editor to java editor." + getStateDetails(),
				GENERIC_EDITOR_ID, getActiveEditorId());

		// Navigate forward from java editor to java editor
		goForward(EditorTestHelper.getActiveWorkbenchWindow(), genericEditorSelection);
		Assert.assertEquals("Failed to correctly navigate forward from java editor to java editor." + getStateDetails(),
				GENERIC_EDITOR_ID, getActiveEditorId());

		// Navigate forward from text editor to text editor
		goForward(EditorTestHelper.getActiveWorkbenchWindow(), textEditorNoSelection);
		goForward(EditorTestHelper.getActiveWorkbenchWindow(), textEditorSelection);
		Assert.assertEquals("Failed to correctly navigate forward from java editor to java editor." + getStateDetails(),
				TEXT_EDITOR_ID, getActiveEditorId());

		// Navigate forward from text editor to java editor
		goForward(EditorTestHelper.getActiveWorkbenchWindow(), genericEditorSelection);
		Assert.assertEquals("Failed to correctly navigate forward from text editor to java editor." + getStateDetails(),
				GENERIC_EDITOR_ID, getActiveEditorId());

		// Navigate forward from java editor to text editor
		goForward(EditorTestHelper.getActiveWorkbenchWindow(), textEditorSelection);
		Assert.assertEquals("Failed to correctly navigate forward from java editor to text editor." + getStateDetails(),
				TEXT_EDITOR_ID, getActiveEditorId());
	}

	private Condition currentNavigationHistoryLocationCondition(String editorId, boolean selection) {
		return () -> {
			INavigationLocation location = EditorTestHelper.getActiveWorkbenchWindow().getActivePage()
					.getNavigationHistory().getCurrentLocation();
			if (location instanceof TextSelectionNavigationLocation) {
				return editorId.equals(location.getId())
						&& (!selection || SELECTION_STRING.equals(location.toString()));
			}
			return false;
		};
	}

	private void openGenericEditor(IEditorInput editorInput) throws PartInitException {
		EditorTestHelper.getActivePage().openEditor(editorInput, GENERIC_EDITOR_ID, true,
				IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
	}

	private void selectInGenericEditor(IEditorInput editorInput) throws PartInitException {
		AbstractTextEditor editor = (AbstractTextEditor) EditorTestHelper.getActivePage().openEditor(editorInput,
				GENERIC_EDITOR_ID, true, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
		editor.selectAndReveal(10, 5);
	}

	private void selectInTextEditor(IEditorInput editorInput) throws PartInitException {
		AbstractTextEditor editor = (AbstractTextEditor) EditorTestHelper.getActivePage().openEditor(editorInput,
				TEXT_EDITOR_ID, true, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
		editor.selectAndReveal(10, 5);
	}

	private void openTextEditor(IEditorInput editorInput) throws PartInitException {
		EditorTestHelper.getActivePage().openEditor(editorInput, TEXT_EDITOR_ID, true,
				IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
	}

	private void goForward(IWorkbenchWindow window, Condition condition) {
		NavigationHistoryAction action = new NavigationHistoryAction(window, true);
		action.run();
		assertTrue("Timeout during navigation.", processEventsUntil(condition, 1000));
	}

	private void goBackward(IWorkbenchWindow window, Condition condition) {
		NavigationHistoryAction action = new NavigationHistoryAction(window, false);
		action.run();
		assertTrue("Timeout during navigation.", processEventsUntil(condition, 1000));
	}

	private String getActiveEditorId() {
		return EditorTestHelper.getActivePage().getActiveEditor().getEditorSite().getId();
	}

	private String getStateDetails() {
		StringBuilder result = new StringBuilder();
		result.append("\nOpen Editors:\n");
		IWorkbenchPage activePage = EditorTestHelper.getActivePage();
		for (IEditorReference editorReference : activePage.getEditorReferences()) {
			IEditorPart editor = editorReference.getEditor(false);
			if (editor != null) {
				String id = editorReference.getId();
				IEditorInput editorInput = editor.getEditorInput();
				result.append("  id=").append(id).append(", input=").append(editorInput).append("\n");
			}
		}
		result.append("Navigation Locations:\n");
		INavigationLocation[] locations = activePage.getNavigationHistory().getLocations();
		for (INavigationLocation location : locations) {
			String id = location.getId();
			Object input = location.getInput();
			result.append("  id=").append(id).append(", input=").append(input).append(", location=").append(location)
					.append("\n");
		}
		return result.toString();
	}
}
