/*******************************************************************************
 * Copyright (c) 2022 Simeon Andreev and others.
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

package org.eclipse.ui.tests.largefile;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.internal.LargeFileLimitsPreferenceHandler;
import org.eclipse.ui.internal.LargeFileLimitsPreferenceHandler.FileLimit;
import org.eclipse.ui.internal.LargeFileLimitsPreferenceHandler.LargeFileEditorSelectionDialog;
import org.eclipse.ui.internal.LargeFileLimitsPreferenceHandler.PromptForEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.UITestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the large file associations preference added for bug 577289.
 *
 * @since 3.5
 */
public class LargeFileLimitsPreferenceHandlerTest {

	public static final String TEST_EDITOR_ID1 = "org.eclipse.ui.tests.largefile.testeditor1";
	public static final String TEST_EDITOR_ID2 = "org.eclipse.ui.tests.largefile.testeditor2";

	private static final int DEFALT_LIMIT_VALUE = 8 * 1024 * 1024;

	private static final String TXT_EXTENSION = "txt";
	private static final String XML_EXTENSION = "xml";

	private final IProgressMonitor monitor;
	private IProject testProject;
	private IFile temporaryFile;
	private TestPromptForEditor testPromptForEditor;
	private LargeFileLimitsPreferenceHandler preferenceHandler;
	private IEditorInput testEditorInput;
	private TestLogListener logListener;

	public LargeFileLimitsPreferenceHandlerTest() {
		monitor = new NullProgressMonitor();
	}

	@Before
	public void doSetUp() throws Exception {
		createTestFile();
		testPromptForEditor = new TestPromptForEditor();
		preferenceHandler = new LargeFileLimitsPreferenceHandler(testPromptForEditor);
		testEditorInput = new FileEditorInput(temporaryFile);
		logListener = new TestLogListener();
		Platform.addLogListener(logListener);
	}

	private void createTestFile() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		testProject = workspaceRoot.getProject("SomeProject");
		testProject.create(monitor);
		testProject.open(monitor);
		IPath path = IPath.fromOSString("/" + testProject.getName() + "/test_file" + "." + TXT_EXTENSION);
		temporaryFile = workspaceRoot.getFile(path);
		String content = String.join(System.lineSeparator(), "some line 1", "some line 2");
		boolean force = true;
		temporaryFile.create(new ByteArrayInputStream(content.getBytes()), force, monitor);
	}

	@After
	public void doTearDown() throws Exception {
		Platform.removeLogListener(logListener);
		setDefaultPreferences();
		preferenceHandler.dispose();
		deleteTestFile();
		boolean save = false;
		closeAllEditors(save);
	}

	private void deleteTestFile() throws CoreException {
		boolean force = true;
		temporaryFile.delete(force, monitor);
		testProject.delete(force, monitor);
	}

	@Test
	public void testPreferencePageSmokeTest() throws Throwable {
		String pageId = LargeFileLimitsPreferenceHandler.LARGE_FILE_ASSOCIATIONS_PREFERENCE_PAGE_ID;
		Shell shell = getWorkbench().getActiveWorkbenchWindow().getShell();
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell, pageId, null, null);
		try {
			waitForJobs();
			dialog.setBlockOnOpen(false);
			dialog.open();
			waitForJobs();
			PreferencePage page = (PreferencePage) dialog.getSelectedPage();
			// close
			page.performOk();
			waitForJobs();
		} finally {
			dialog.close();
			logListener.assertNoLoggedErrors();
		}
	}

	@Test
	public void testEditorPromptDialogSmokeTest() throws Throwable {
		boolean closedDialog = false;
		long fileSize = 4L;
		Shell shell = getWorkbench().getActiveWorkbenchWindow().getShell();
		LargeFileEditorSelectionDialog dialog = new LargeFileEditorSelectionDialog(shell, TXT_EXTENSION, fileSize);
		try {
			waitForJobs();
			dialog.setBlockOnOpen(false);
			dialog.open();
			waitForJobs();
			dialog.close();
			closedDialog = true;
			IEditorDescriptor selectedEditor = dialog.getSelectedEditor();
			boolean rememberSelection = dialog.shouldRememberSelectedEditor();
			assertNull("Expected no default selection in dialog", selectedEditor);
			assertFalse("Expected default to not remember editor selection", rememberSelection);
			waitForJobs();
		} finally {
			if (!closedDialog) {
				dialog.close();
			}
			logListener.assertNoLoggedErrors();
		}
	}

	@Test
	public void testOpenEditorWithIgnoreSize() throws Exception {
		String testEditorId = TEST_EDITOR_ID2;
		long fileSize = 4L;
		List<FileLimit> fileLimits = Arrays.asList(new FileLimit(testEditorId, fileSize));
		configureFileLimits(fileLimits);

		Class<?> expectedEditorClass = org.eclipse.ui.tests.api.MockEditorPart.class;
		IEditorInput editorInput = testEditorInput;
		String editorIdForOpen = "org.eclipse.ui.tests.api.MockEditorPart1";

		IWorkbenchPage page = getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor = null;
		try {
			boolean activate = true;
			int flags = IWorkbenchPage.MATCH_NONE | IWorkbenchPage.MATCH_IGNORE_SIZE;
			editor = page.openEditor(editorInput, editorIdForOpen, activate, flags);
			assertEquals("Wrong editor opened", expectedEditorClass, editor.getClass());
		} finally {
			if (editor != null) {
				editor.dispose();
			}
		}
	}

	@Test
	public void testDisabledDefaultLimit() throws Exception {
		String testEditorId = "org.eclipse.ui.tests.api.MockEditorPart1";
		long fileSize = 1L;
		LargeFileLimitsPreferenceHandler.setDefaultLimit(fileSize);
		LargeFileLimitsPreferenceHandler.disableDefaultLimit();

		IEditorRegistry editorRegistry = getWorkbench().getEditorRegistry();
		IEditorDescriptor testEditor = editorRegistry.findEditor(testEditorId);
		assertNotNull("Expected to find editor with ID: " + testEditorId, testEditor);

		testPromptForEditor.selectedEditor = testEditor;
		testPromptForEditor.rememberSelection = false;
		// bug 579119: dialog to chose editor should not come up, we disabled the
		// default limit preference
		assertNoEditorIsChosen();
	}

	@Test
	public void testOpenEditor() throws Exception {
		String testEditorId = "org.eclipse.ui.tests.api.MockEditorPart1";
		long fileSize = 4L;
		List<FileLimit> fileLimits = Arrays.asList(new FileLimit(testEditorId, fileSize));
		configureFileLimits(fileLimits);

		Class<?> expectedEditorClass = org.eclipse.ui.tests.api.MockEditorPart.class;
		IEditorInput editorInput = testEditorInput;
		String editorIdForOpen = TEST_EDITOR_ID2;

		IWorkbenchPage page = getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor = null;
		try {
			editor = page.openEditor(editorInput, editorIdForOpen);
			assertEquals("Wrong editor opened", expectedEditorClass, editor.getClass());
		} finally {
			if (editor != null) {
				editor.dispose();
			}
		}
	}

	@Test
	public void testOpenFileFromDeletedProject() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		String projectName = LargeFileLimitsPreferenceHandlerTest.class.getSimpleName() + "TestProject";
		IProject project = root.getProject(projectName);
		assertFalse("Expected project to not exist yet: " + projectName, project.exists());
		NullProgressMonitor monitor = new NullProgressMonitor();
		project.create(monitor);
		project.open(monitor);
		assertTrue("Expected project to be accessible: " + projectName, project.isAccessible());
		assertTrue("Expected project to exist: " + projectName, project.exists());

		IFile testFile = project.getFile("test_file.txt");
		ByteArrayInputStream input = new ByteArrayInputStream("test contents".getBytes());
		boolean force = true;
		testFile.create(input, force, monitor);
		IPathEditorInput editorInput = new FileEditorInput(testFile);
		project.delete(force, monitor);
		waitForJobs();

		String testEditorId = "org.eclipse.ui.tests.api.MockEditorPart1";
		long fileSize = 4L;
		List<FileLimit> fileLimits = Arrays.asList(new FileLimit(testEditorId, fileSize));
		configureFileLimits(fileLimits);

		// we expect the "error editor" to be opened here, as the file we want to open was deleted
		Class<?> expectedEditorClass = org.eclipse.ui.internal.ErrorEditorPart.class;
		String editorIdForOpen = TEST_EDITOR_ID2;
		IWorkbenchPage page = getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor = null;
		try {
			editor = page.openEditor(editorInput, editorIdForOpen);
			assertEquals("Wrong editor opened", expectedEditorClass, editor.getClass());
		} finally {
			if (editor != null) {
				editor.dispose();
			}
		}
	}

	@Test
	public void testRestoreDefaults() throws Exception {
		String[] configuredExtensions = { TXT_EXTENSION, XML_EXTENSION };
		String[] disabledExtensions = { XML_EXTENSION };
		LargeFileLimitsPreferenceHandler.setConfiguredExtensionTypes(configuredExtensions);
		LargeFileLimitsPreferenceHandler.setDisabledExtensionTypes(disabledExtensions);
		long fileSize = 4L;
		LargeFileLimitsPreferenceHandler.setDefaultLimit(fileSize);
		String testEditorId1 = TEST_EDITOR_ID1;
		long fileSize1 = 4_000L;
		List<FileLimit> fileLimits = Arrays.asList(new FileLimit(testEditorId1, fileSize1));
		configureFileLimits(fileLimits);

		LargeFileLimitsPreferenceHandler.restoreDefaults();

		doTestDefaults();
	}

	@Test
	public void testDefaults() throws Exception {
		doTestDefaults();
	}

	@Test
	public void testSetConfiguredExtensions() throws Exception {
		String[] extensions = { TXT_EXTENSION, XML_EXTENSION };
		LargeFileLimitsPreferenceHandler.setConfiguredExtensionTypes(extensions);
		String[] configuredExtensions = LargeFileLimitsPreferenceHandler.getConfiguredExtensionTypes();
		assertArrayEquals("Wrong configured extensions", extensions, configuredExtensions);
	}

	@Test
	public void testSetDisabledExtensions() throws Exception {
		String[] extensions = { TXT_EXTENSION, XML_EXTENSION };
		LargeFileLimitsPreferenceHandler.setDisabledExtensionTypes(extensions);
		String[] configuredExtensions = LargeFileLimitsPreferenceHandler.getDisabledExtensionTypes();
		assertArrayEquals("Wrong disabled extensions", extensions, configuredExtensions);
	}

	@Test
	public void testGetEditorForInput() throws Exception {
		long fileSize = 4L;
		LargeFileLimitsPreferenceHandler.setDefaultLimit(fileSize);
		String testEditorId = TEST_EDITOR_ID1;

		IEditorRegistry editorRegistry = getWorkbench().getEditorRegistry();
		IEditorDescriptor testEditor = editorRegistry.findEditor(testEditorId);
		assertNotNull("Expected to find editor with ID: " + testEditorId, testEditor);

		testPromptForEditor.selectedEditor = testEditor;
		testPromptForEditor.rememberSelection = false;
		assertEditorIsChosen(testEditorId);

		List<FileLimit> limits = LargeFileLimitsPreferenceHandler.getLargeFilePreferenceValues(TXT_EXTENSION);
		assertEquals("Expected exactly 1 file limit to be set, but got: " + limits, 1, limits.size());
		FileLimit limit = limits.get(0);
		assertEquals("Wrong limit editor ID",
				LargeFileLimitsPreferenceHandler.PROMPT_EDITOR_PREFERENCE_VALUE, limit.editorId);
		assertEquals("Wrong limit file size", fileSize, limit.fileSize);
	}

	@Test
	public void testRememberSelection() throws Exception {
		long fileSize = 4L;
		LargeFileLimitsPreferenceHandler.setDefaultLimit(fileSize);
		String testEditorId = TEST_EDITOR_ID1;

		IEditorRegistry editorRegistry = getWorkbench().getEditorRegistry();
		IEditorDescriptor testEditor = editorRegistry.findEditor(testEditorId);
		assertNotNull("Expected to find editor with ID: " + testEditorId, testEditor);

		testPromptForEditor.selectedEditor = testEditor;
		testPromptForEditor.rememberSelection = true;
		assertEditorIsChosen(testEditorId);

		List<FileLimit> limits = LargeFileLimitsPreferenceHandler.getLargeFilePreferenceValues(TXT_EXTENSION);
		assertEquals("Expected exactly 1 file limit to be set: " + limits, 1, limits.size());
		FileLimit limit = limits.get(0);
		assertEquals("Wrong limit editor ID remembered", testEditorId, limit.editorId);
		assertEquals("Wrong limit file size remembered", fileSize, limit.fileSize);
	}

	@Test
	public void testNoConfiguration() throws Exception {
		assertNoEditorIsChosen();
	}

	@Test
	public void testDefaultLimit() throws Exception {
		long fileSize = 4L;
		LargeFileLimitsPreferenceHandler.setDefaultLimit(fileSize);

		List<FileLimit> limits = LargeFileLimitsPreferenceHandler.getLargeFilePreferenceValues(TXT_EXTENSION);
		assertEquals("Expected only default file limit, but got: " + limits, 1, limits.size());

		FileLimit defaultLimit = limits.get(0);
		assertEquals("Wrong editor ID",
				LargeFileLimitsPreferenceHandler.PROMPT_EDITOR_PREFERENCE_VALUE, defaultLimit.editorId);
		assertEquals("Wrong file size limit", fileSize, defaultLimit.fileSize);
	}

	@Test
	public void testSingleLimitForExtension() throws Exception {
		String testEditorId = TEST_EDITOR_ID1;
		long fileSize = 4L;
		List<FileLimit> fileLimits = Arrays.asList(new FileLimit(testEditorId, fileSize));
		configureFileLimits(fileLimits);

		assertEditorIsChosen(testEditorId);
	}

	@Test
	public void testMultipleLimitsForExtension() throws Exception {
		String testEditorId1 = TEST_EDITOR_ID1;
		long fileSize1 = 4L;
		String testEditorId2 = TEST_EDITOR_ID2;
		long fileSize2 = 8L;
		List<FileLimit> fileLimits = Arrays.asList(new FileLimit(testEditorId1, fileSize1), new FileLimit(testEditorId2, fileSize2));
		configureFileLimits(fileLimits);

		// editor for largest matching file limit should be chosen
		assertEditorIsChosen(testEditorId2);
	}

	@Test
	public void testNoMatchingLimit() throws Exception {
		String testEditorId1 = TEST_EDITOR_ID1;
		long fileSize1 = 4_000L;
		List<FileLimit> fileLimits = Arrays.asList(new FileLimit(testEditorId1, fileSize1));
		configureFileLimits(fileLimits);

		assertNoEditorIsChosen();
	}

	@Test
	public void testSingleFileLimitAndDefault() throws Exception {
		long defaultLimit = 4L;
		LargeFileLimitsPreferenceHandler.setDefaultLimit(defaultLimit);

		String testEditorId = TEST_EDITOR_ID1;
		long fileSize = 1L;
		List<FileLimit> fileLimits = Arrays.asList(new FileLimit(testEditorId, fileSize));
		configureFileLimits(fileLimits);

		assertEditorIsChosen(testEditorId);
	}

	private void doTestDefaults() {
		String[] configuredExtensionTypes = LargeFileLimitsPreferenceHandler.getConfiguredExtensionTypes();
		assertEmptyArray("Expected no large file associations to be configured by default", configuredExtensionTypes);

		String[] disabledExtensionTypes = LargeFileLimitsPreferenceHandler.getDisabledExtensionTypes();
		assertEmptyArray("Expected no large file associations to be disabled by default", disabledExtensionTypes);

		String extension = TXT_EXTENSION;
		List<FileLimit> limits = LargeFileLimitsPreferenceHandler.getFileLimitsForExtension(extension);
		assertEquals("Expected no limit to be configured per default for file of type: " + extension,
				Collections.EMPTY_LIST, limits);

		boolean defaultLimitEnabled = LargeFileLimitsPreferenceHandler.isDefaultLimitEnabled();
		assertFalse("Expected default large file limit to be disabled", defaultLimitEnabled);
		long defaultLimit = LargeFileLimitsPreferenceHandler.getDefaultLimit();
		assertEquals("Wrong default large file limit value", DEFALT_LIMIT_VALUE, defaultLimit);
	}

	private void assertNoEditorIsChosen() {
		Optional<String> editorForInput = preferenceHandler.getEditorForInput(testEditorInput);
		assertNotNull("Expected non-null result for large file of type: " + TXT_EXTENSION, editorForInput);
		if (editorForInput.isPresent()) {
			fail("Expected no editor for large file of type: " + TXT_EXTENSION + ", but got: " + editorForInput.get());
		}
	}

	private void assertEditorIsChosen(String testEditorId) {
		Optional<String> editorForInput = preferenceHandler.getEditorForInput(testEditorInput);
		assertTrue("Expected editor for large file of type: " + TXT_EXTENSION, editorForInput.isPresent());
		assertEquals("Wrong editor for large file", testEditorId, editorForInput.get());
	}

	private static void waitForJobs() {
		UITestUtil.waitForJobs(250, 2_000);
	}

	private static void setDefaultPreferences() {
		LargeFileLimitsPreferenceHandler.setConfiguredExtensionTypes(new String[0]);
		LargeFileLimitsPreferenceHandler.setDisabledExtensionTypes(new String[0]);
		configureFileLimits(Collections.EMPTY_LIST);
		LargeFileLimitsPreferenceHandler.setDefaultLimit(DEFALT_LIMIT_VALUE);
		LargeFileLimitsPreferenceHandler.disableDefaultLimit();
	}

	private static void configureFileLimits(List<FileLimit> fileLimits) {
		LargeFileLimitsPreferenceHandler.setFileLimitsForExtension(TXT_EXTENSION, fileLimits);
		LargeFileLimitsPreferenceHandler.setFileLimitsForExtension(XML_EXTENSION, fileLimits);
	}

	private static void assertEmptyArray(String failMessage, String[] configuredExtensionTypes) {
		assertEquals(failMessage, Collections.EMPTY_LIST, Arrays.asList(configuredExtensionTypes));
	}

	private static void closeAllEditors(boolean save) {
		getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(save);
	}

	private static class TestLogListener implements ILogListener {

		private final List<IStatus> errors = new ArrayList<>();

		@Override
		public void logging(IStatus status, String plugin) {
			if (status.getSeverity() == IStatus.ERROR) {
				errors.add(status);
			}
		}

		void assertNoLoggedErrors() {
			if (!errors.isEmpty()) {
				StringBuilder failMessage = new StringBuilder();
				failMessage.append("Unexpected logged errors:");
				failMessage.append(System.lineSeparator());
				for (IStatus error : errors) {
					failMessage.append("Status message:");
					failMessage.append(error.getMessage());
					failMessage.append(System.lineSeparator());
					failMessage.append("From plug-in:");
					failMessage.append(error.getPlugin());
					failMessage.append(System.lineSeparator());
					Throwable exception = error.getException();
					if (exception != null) {
						failMessage.append("Exception:");
						failMessage.append(System.lineSeparator());
						failMessage.append(exception.getMessage());
						failMessage.append(System.lineSeparator());
						StackTraceElement[] stackTrace = exception.getStackTrace();
						for (StackTraceElement element : stackTrace) {
							failMessage.append('\t');
							failMessage.append(element);
							failMessage.append(System.lineSeparator());
						}
					}

				}
				fail(failMessage.toString());
			}
		}
	}

	private static class TestPromptForEditor implements PromptForEditor {

		IEditorDescriptor selectedEditor = null;
		boolean rememberSelection = false;

		@Override
		public void prompt(IPath inputPath, FileLimit fileLimit) {
			// "selected" editor is set by tests
		}

		@Override
		public IEditorDescriptor getSelectedEditor() {
			return selectedEditor;
		}

		@Override
		public boolean shouldRememberSelectedEditor() {
			return rememberSelection;
		}
	}
}
