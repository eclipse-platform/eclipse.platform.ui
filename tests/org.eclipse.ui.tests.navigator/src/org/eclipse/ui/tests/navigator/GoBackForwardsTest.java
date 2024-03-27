/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.NavigationHistoryAction;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.EditorTestHelper;
import org.eclipse.ui.tests.harness.util.FileTool;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
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
	private static final String JAVA_EDITOR_ID = "org.eclipse.jdt.ui.CompilationUnitEditor";
	private static final String TEXT_EDITOR_ID = "org.eclipse.ui.genericeditor.GenericEditor";
	private static final String SELECTION_STRING = "Selection<offset: 10, length: 5>";

	private IProject project;
	private IFile file;

	@Override
	public void doSetUp() {
		try {
			project = FileUtil.createProject(PROJECT_NAME);
			file = FileUtil.createFile(FILE_NAME, project);
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(FILE_CONTENTS);
			FileTool.writeFromBuilder(file.getLocation().toOSString(), stringBuilder);
		} catch (CoreException e) {
			fail("Should not throw an exception");
		} catch (IOException e) {
			fail("Should not throw an exception");
		}

	}

	@Test
	public void testNavigationHistoryNavigation() {
		IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
		PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);

		Condition javaEditorNoSelection = currentNavigationHistoryLocationCondition(JAVA_EDITOR_ID, false);
		Condition javaEditorSelection = currentNavigationHistoryLocationCondition(JAVA_EDITOR_ID, true);
		Condition textEditorNoSelection = currentNavigationHistoryLocationCondition(TEXT_EDITOR_ID, false);
		Condition textEditorSelection = currentNavigationHistoryLocationCondition(TEXT_EDITOR_ID, true);

		FileEditorInput editorInput = new FileEditorInput(file);

		openJavaEditor(editorInput);

		if (!processEventsUntil(javaEditorNoSelection, 30000)) {
			fail("Timeout during navigation.");
		}

		selectInJavaEditor(editorInput);

		if (!processEventsUntil(javaEditorSelection, 30000)) {
			fail("Timeout during navigation.");
		}

		openTextEditor(editorInput);

		if (!processEventsUntil(textEditorNoSelection, 30000)) {
			fail("Timeout during navigation.");
		}

		selectInTextEditor(editorInput);

		if (!processEventsUntil(textEditorSelection, 30000)) {
			fail("Timeout during navigation.");
		}

		openJavaEditor(editorInput);

		if (!processEventsUntil(javaEditorSelection, 30000)) {
			fail("Timeout during navigation.");
		}

		openTextEditor(editorInput);

		if (!processEventsUntil(textEditorSelection, 30000)) {
			fail("Timeout during navigation.");
		}

		// Navigate backward from text editor to java editor
		goBackward(EditorTestHelper.getActiveWorkbenchWindow(), javaEditorSelection);
		Assert.assertEquals("Failed to correctly navigate backward from text editor to java editor.", JAVA_EDITOR_ID,
				getActiveEditorId());

		// Navigate backward from java editor to text editor
		goBackward(EditorTestHelper.getActiveWorkbenchWindow(), textEditorSelection);
		Assert.assertEquals("Failed to correctly navigate backward from java editor to test editor.", TEXT_EDITOR_ID,
				getActiveEditorId());

		// Navigate backward from text editor to text editor
		goBackward(EditorTestHelper.getActiveWorkbenchWindow(), textEditorNoSelection);
		Assert.assertEquals("Failed to correctly navigate backward from text editor to text editor.", TEXT_EDITOR_ID,
				getActiveEditorId());

		// Navigate backward from java editor to java editor
		goBackward(EditorTestHelper.getActiveWorkbenchWindow(), javaEditorSelection);
		goBackward(EditorTestHelper.getActiveWorkbenchWindow(), javaEditorNoSelection);
		Assert.assertEquals("Failed to correctly navigate backward from java editor to java editor.", JAVA_EDITOR_ID,
				getActiveEditorId());

		// Navigate forward from java editor to java editor
		goForward(EditorTestHelper.getActiveWorkbenchWindow(), javaEditorSelection);
		Assert.assertEquals("Failed to correctly navigate forward from java editor to java editor.", JAVA_EDITOR_ID,
				getActiveEditorId());

		// Navigate forward from text editor to text editor
		goForward(EditorTestHelper.getActiveWorkbenchWindow(), textEditorNoSelection);
		goForward(EditorTestHelper.getActiveWorkbenchWindow(), textEditorSelection);
		Assert.assertEquals("Failed to correctly navigate forward from java editor to java editor.", TEXT_EDITOR_ID,
				getActiveEditorId());

		// Navigate forward from text editor to java editor
		goForward(EditorTestHelper.getActiveWorkbenchWindow(), javaEditorSelection);
		Assert.assertEquals("Failed to correctly navigate forward from text editor to java editor.", JAVA_EDITOR_ID,
				getActiveEditorId());

		// Navigate forward from java editor to text editor
		goForward(EditorTestHelper.getActiveWorkbenchWindow(), textEditorSelection);
		Assert.assertEquals("Failed to correctly navigate forward from java editor to text editor.", TEXT_EDITOR_ID,
				getActiveEditorId());
	}

	private Condition currentNavigationHistoryLocationCondition(String editorId, boolean selection) {
		return new Condition() {

			@Override
			public boolean compute() {
				INavigationLocation location = EditorTestHelper.getActiveWorkbenchWindow().getActivePage()
						.getNavigationHistory().getCurrentLocation();
				if (location instanceof TextSelectionNavigationLocation) {
					return editorId.equals(location.getId())
							&& (!selection || SELECTION_STRING.equals(location.toString()));
				}
				return false;
			}

		};
	}

	private void openJavaEditor(IEditorInput editorInput) {
		try {
			EditorTestHelper.getActivePage().openEditor(editorInput, JAVA_EDITOR_ID, true,
					IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
		} catch (PartInitException e) {
			fail("Should not throw an exception");
		}
	}

	private void selectInJavaEditor(IEditorInput editorInput) {
		try {
			AbstractTextEditor editor = (AbstractTextEditor) EditorTestHelper.getActivePage().openEditor(editorInput,
					JAVA_EDITOR_ID, true, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
			editor.selectAndReveal(10, 5);
		} catch (PartInitException e) {
			fail("Should not throw an exception");
		}
	}

	private void selectInTextEditor(IEditorInput editorInput) {
		try {
			AbstractTextEditor editor = (AbstractTextEditor) EditorTestHelper.getActivePage().openEditor(editorInput,
					TEXT_EDITOR_ID, true, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
			editor.selectAndReveal(10, 5);
		} catch (PartInitException e) {
			fail("Should not throw an exception");
		}
	}

	private void openTextEditor(IEditorInput editorInput) {
		try {
			EditorTestHelper.getActivePage().openEditor(editorInput, TEXT_EDITOR_ID, true,
					IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
		} catch (PartInitException e) {
			fail("Should not throw an exception");
		}
	}

	private void goForward(IWorkbenchWindow window, Condition condition) {
		NavigationHistoryAction action = new NavigationHistoryAction(window, true);
		action.run();
		if (!processEventsUntil(condition, 30000)) {
			fail("Timeout during navigation.");
		}
	}

	private void goBackward(IWorkbenchWindow window, Condition condition) {
		NavigationHistoryAction action = new NavigationHistoryAction(window, false);
		action.run();
		if (!processEventsUntil(condition, 30000)) {
			fail("Timeout during navigation.");
		}
	}

	private String getActiveEditorId() {
		return EditorTestHelper.getActivePage().getActiveEditor().getEditorSite().getId();
	}
}
