package org.eclipse.ui.tests.internal;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbookEditorsHandler;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class WorkbookEditorsHandlerTest extends UITestCase {


	public WorkbookEditorsHandlerTest(String testName) {
		super(testName);
	}

	private static final String PROJECT_NAME = "WorkbookEditorsHandlerTest";
	private static final String PROJECT_NAME_1 = PROJECT_NAME + 1;
	private static final String PROJECT_NAME_2 = PROJECT_NAME + 2;
	private IWorkbenchWindow activeWindow;
	private IWorkbenchPage activePage;
	private IProject project1;
	private IProject project2;

	@Override
	public void doSetUp() throws CoreException {
		activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		activePage = activeWindow.getActivePage();
		project1 = FileUtil.createProject(PROJECT_NAME_1);
		project2 = FileUtil.createProject(PROJECT_NAME_2);
	}

	@Override
	public void doTearDown() throws Exception {
		if (project1 != null) {
			project1.delete(true, true, null);
			project1 = null;
		}
		if (project2 != null) {
			project2.delete(true, true, null);
			project2 = null;
		}
		activePage.closeAllEditors(false);
	}

	@Test
	public void testSingleFile() throws Exception {
		String fileName = "example.txt";
		IDE.openEditor(activePage, FileUtil.createFile(fileName, project1), true);
		ICommandService cmdService = PlatformUI.getWorkbench().getService(ICommandService.class);
		final Command cmd = cmdService.getCommand("org.eclipse.ui.window.openEditorDropDown");
		WorkbookEditorsHandlerTestable handler = new WorkbookEditorsHandlerTestable();
		cmd.setHandler(handler);
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		final ExecutionEvent event = handlerService.createExecutionEvent(cmd, null);

		handler.execute(event);

		assertEquals("Display text should match file name", fileName, handler.tableItemTexts.get(0));
		assertEquals("Selection should be on current editor if only one editor is open", fileName,
				handler.tableItemTexts.get(0));
	}

	@Test
	public void testMultipleFilesWithNoNameConflict() throws Exception {
		String fileName = "example.txt";
		IDE.openEditor(activePage, FileUtil.createFile(fileName, project1), true);
		String fileName2 = "example2.txt";
		IDE.openEditor(activePage, FileUtil.createFile(fileName2, project1), true);
		ICommandService cmdService = PlatformUI.getWorkbench().getService(ICommandService.class);
		final Command cmd = cmdService.getCommand("org.eclipse.ui.window.openEditorDropDown");
		WorkbookEditorsHandlerTestable handler = new WorkbookEditorsHandlerTestable();
		cmd.setHandler(handler);
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		final ExecutionEvent event = handlerService.createExecutionEvent(cmd, null);

		handler.execute(event);

		assertEquals("Display text should match file name", fileName2, handler.tableItemTexts.get(0));
		assertEquals("Display text should match file name", fileName, handler.tableItemTexts.get(1));
		assertEquals("Selection should be the editor that was active before the currently active editor", fileName,
				handler.tableItemTexts.get(1));
	}

	@Test
	public void testTwoFilesWithNameClash() throws Exception {
		String fileName = "example.txt";
		IDE.openEditor(activePage, FileUtil.createFile(fileName, project1), true);
		IDE.openEditor(activePage, FileUtil.createFile(fileName, project2), true);
		ICommandService cmdService = PlatformUI.getWorkbench().getService(ICommandService.class);
		final Command cmd = cmdService.getCommand("org.eclipse.ui.window.openEditorDropDown");
		WorkbookEditorsHandlerTestable handler = new WorkbookEditorsHandlerTestable();
		cmd.setHandler(handler);
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		final ExecutionEvent event = handlerService.createExecutionEvent(cmd, null);

		handler.execute(event);

		assertEquals("Text should have folder prepended because of name clash",
				PROJECT_NAME_2 + File.separator + fileName, handler.tableItemTexts.get(0));
		assertEquals("Text should have folder prepended because of name clash",
				PROJECT_NAME_1 + File.separator + fileName, handler.tableItemTexts.get(1));
		assertEquals("Selection should be the editor that was active before the currently active editor",
				PROJECT_NAME_1 + File.separator + fileName, handler.tableItemTexts.get(1));
	}

	@Test
	public void testMultipleFilesWithOneNameClash() throws Exception {
		String fileName = "example.txt";
		String otherFileName = "other.txt";
		IDE.openEditor(activePage, FileUtil.createFile(otherFileName, project1), true);
		IDE.openEditor(activePage, FileUtil.createFile(fileName, project1), true);
		IDE.openEditor(activePage, FileUtil.createFile(fileName, project2), true);
		ICommandService cmdService = PlatformUI.getWorkbench().getService(ICommandService.class);
		final Command cmd = cmdService.getCommand("org.eclipse.ui.window.openEditorDropDown");
		WorkbookEditorsHandlerTestable handler = new WorkbookEditorsHandlerTestable();
		cmd.setHandler(handler);
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		final ExecutionEvent event = handlerService.createExecutionEvent(cmd, null);

		handler.execute(event);

		assertEquals("Text should have folder prepended because of name clash",
				PROJECT_NAME_2 + File.separator + fileName, handler.tableItemTexts.get(0));
		assertEquals("Text should have folder prepended because of name clash",
				PROJECT_NAME_1 + File.separator + fileName, handler.tableItemTexts.get(1));
		assertEquals("Display name should equal to file name", otherFileName, handler.tableItemTexts.get(2));
		assertEquals("Selection should be the editor that was active before the currently active editor",
				PROJECT_NAME_1 + File.separator + fileName, handler.tableItemTexts.get(1));
	}

	@Test
	public void testMultipleFilesWithSharedParentFolders() throws Exception {
		String fileName = "example.txt";
		String path1 = "test1/foo/bar/baz/";
		String path2 = "test2/foo/bar/baz/";
		FileUtil.createFolder(project1.getFolder(path1), true, false, null);
		FileUtil.createFolder(project1.getFolder(path2), true, false, null);
		IDE.openEditor(activePage, FileUtil.createFile(path1 + fileName, project1), true);
		IDE.openEditor(activePage, FileUtil.createFile(path2 + fileName, project1), true);
		ICommandService cmdService = PlatformUI.getWorkbench().getService(ICommandService.class);
		final Command cmd = cmdService.getCommand("org.eclipse.ui.window.openEditorDropDown");
		WorkbookEditorsHandlerTestable handler = new WorkbookEditorsHandlerTestable();
		cmd.setHandler(handler);
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		final ExecutionEvent event = handlerService.createExecutionEvent(cmd, null);

		handler.execute(event);

		assertEquals("Text should have first differing folder prepended", "test2/" + fileName,
				handler.tableItemTexts.get(0));
		assertEquals("Text should have first differing folder prepended", "test1/" + fileName,
				handler.tableItemTexts.get(1));
		assertEquals("Selection should be the editor that was active before the currently active editor",
				"test1/" + fileName, handler.tableItemTexts.get(1));
	}

	@Test
	public void testMultipleFilesWithSharedParentFoldersButNotAllShareTheSameParentFolders() throws Exception {
		String fileName = "example.txt";
		String path1 = "test1/foo/bar/baz/";
		String path2 = "test2/foo/bar/baz/";
		String path3 = "test2/foo/bar/";
		FileUtil.createFolder(project1.getFolder(path1), true, false, null);
		FileUtil.createFolder(project1.getFolder(path2), true, false, null);
		IDE.openEditor(activePage, FileUtil.createFile(path1 + fileName, project1),
				true);
		IDE.openEditor(activePage, FileUtil.createFile(path2 + fileName, project1),
				true);
		IDE.openEditor(activePage, FileUtil.createFile(path3 + fileName, project1), true);
		ICommandService cmdService = PlatformUI.getWorkbench().getService(ICommandService.class);
		final Command cmd = cmdService.getCommand("org.eclipse.ui.window.openEditorDropDown");
		WorkbookEditorsHandlerTestable handler = new WorkbookEditorsHandlerTestable();
		cmd.setHandler(handler);
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		final ExecutionEvent event = handlerService.createExecutionEvent(cmd, null);

		handler.execute(event);

		assertEquals("Text should have parent folder prepended", "bar/" + fileName,
				handler.tableItemTexts.get(0));
		assertEquals("Text should have full folder chain until differing folder prepended", path2 + fileName,
				handler.tableItemTexts.get(1));
		assertEquals("Text should have full folder chain until differing folder prepended", path1 + fileName,
				handler.tableItemTexts.get(2));
		assertEquals("There should only ever be one selected editor", 1, handler.selectionTexts.size());
		assertEquals("Selection should be the editor that was active before the currently active editor",
				path2 + fileName, handler.tableItemTexts.get(1));
	}

	class WorkbookEditorsHandlerTestable extends WorkbookEditorsHandler {
		List<String> selectionTexts;
		List<String> tableItemTexts;

		/**
		 * This method was solely chosen as an overwrite target because it gets passed
		 * the Table, which we need to get the relevant data for our assertions.
		 */
		@Override
		protected void addKeyListener(Table table, Shell dialog) {
			super.addKeyListener(table, dialog);
			tableItemTexts = Arrays.stream(table.getItems()).map(TableItem::getText).collect(toList());
			selectionTexts = Arrays.stream(table.getSelection()).map(TableItem::getText).collect(toList());
		}

		/**
		 * Don't block the UI thread. Otherwise our tests would hang indefinitely.
		 */
		@Override
		protected void keepOpen(Display display, Shell dialog) {
			dialog.close();
		}
	}

}
