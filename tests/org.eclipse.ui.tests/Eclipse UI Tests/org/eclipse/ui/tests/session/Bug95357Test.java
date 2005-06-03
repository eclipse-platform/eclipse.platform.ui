package org.eclipse.ui.tests.session;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.EditorSite;
import org.eclipse.ui.internal.EditorStack;
import org.eclipse.ui.tests.api.SessionEditorPart;
import org.eclipse.ui.tests.dnd.DragOperations;
import org.eclipse.ui.tests.dnd.EditorDropTarget;
import org.eclipse.ui.tests.dnd.ExistingWindowProvider;
import org.eclipse.ui.tests.util.FileUtil;

/**
 * Bug 95357 Need a test to ensure editor activation is not broken on startup.
 * When eclipse starts, there should be tabs for all of the open editor windows
 * but only the <b>active</b> editor window(s) should have been instantiated. A
 * bug that crops up occasionally is that all of the editors have been
 * instantiated, which impacts performance.
 * 
 * These tests more or less depend on being run in order. The workspace exists
 * from method to method.
 * 
 * @since 3.1
 * 
 */
public class Bug95357Test extends TestCase {

	private static final String BUG95357PROJECT = "Bug95357project";

	private static final int FILE_MAX = 8;

	private IWorkbenchWindow fWin;

	private IWorkbenchPage fActivePage;

	private IProject fProject;

	private IWorkbench fWorkbench;

	private String[] itsFilename;

	public Bug95357Test(String testName) {
		super(testName);
		fWorkbench = PlatformUI.getWorkbench();
		fProject = null;

	}

	/**
	 * @param ext
	 */
	private void setupFilenames(String ext) {
		itsFilename = new String[Bug95357Test.FILE_MAX];
		for (int i = 0; i < Bug95357Test.FILE_MAX; ++i) {
			itsFilename[i] = "test" + i + ext;
		}
	}

	protected void setUp() throws Exception {
		fWin = fWorkbench.getActiveWorkbenchWindow();

		fActivePage = fWin.getActivePage();
		fProject = FileUtil.createProject(Bug95357Test.BUG95357PROJECT);
	}

	/**
	 * Multiple editors open - part 1 of 2. This makes sure that there are
	 * FILE_MAX editors open, and the files have been created. Then the session
	 * stops.
	 * 
	 * @throws PartInitException
	 * @throws CoreException
	 */
	private void multipleEditors() throws PartInitException, CoreException {
		fActivePage.closeAllEditors(false);
		
		IEditorPart[] part = new IEditorPart[itsFilename.length];
		for (int i = 0; i < itsFilename.length; i++) {
			part[i] = IDE.openEditor(fActivePage, FileUtil.createFile(
					itsFilename[i], fProject), true);
		}

		assertTrue(fActivePage.isEditorAreaVisible());
		assertFalse(fActivePage.isPartVisible(part[0]));
		assertTrue(fActivePage.isPartVisible(part[part.length - 1]));
	}

	/**
	 * Multiple editors open - part 2 of 2. We can test the state of eclipse
	 * after the system has restarted. We expect that the last editor will be
	 * active and instantiated, but the other editors won't have been
	 * instantiated.
	 */
	private void multipleEditorsOpen() {
		IEditorReference[] editors = fActivePage.getEditorReferences();
		assertEquals(Bug95357Test.FILE_MAX, editors.length);

		for (int i = 0; i < editors.length - 1; i++) {
			assertNull("Editor " + i + " " + editors[i].getName()
					+ " should not be active", editors[i].getEditor(false));
		}
		assertNotNull(editors[editors.length - 1].getEditor(false));
		assertNotNull(editors[0].getEditor(true));
	}

	/**
	 * Multiple editors in 2 stacks - part 1 of 2. Set up eclipse with FILE_MAX
	 * editors open in 2 stacks.
	 * 
	 * @throws PartInitException
	 * @throws CoreException
	 */
	private void multipleStacks() throws PartInitException, CoreException {
		final String f1 = itsFilename[0];
		final String f2 = itsFilename[1];
		final int startAt = 2;

		fActivePage.closeAllEditors(false);

		IEditorPart last = IDE.openEditor(fActivePage, FileUtil.createFile(f1,
				fProject), true);
		IEditorPart current = IDE.openEditor(fActivePage, FileUtil.createFile(
				f2, fProject), true);

		// create the second editor stack using the second editor
		DragOperations.drag(current, new EditorDropTarget(
				new ExistingWindowProvider(fWin), 0, SWT.BOTTOM), false);

		EditorStack firstStack = (EditorStack) ((EditorSite) last
				.getEditorSite()).getPane().getContainer();
		EditorStack secondStack = (EditorStack) ((EditorSite) current
				.getEditorSite()).getPane().getContainer();

		for (int i = startAt; i < itsFilename.length; ++i) {
			fActivePage.activate(last);
			last = current;
			current = IDE.openEditor(fActivePage, FileUtil.createFile(
					itsFilename[i], fProject), true);
		}
		assertEquals(Bug95357Test.FILE_MAX / 2, firstStack.getItemCount());
		assertEquals(Bug95357Test.FILE_MAX / 2, secondStack.getItemCount());
	}

	/**
	 * Multiple editors in 2 stacks - part 2 of 2. 2 of the editors should have
	 * been instantiated. The rest should still be inactive.
	 * 
	 */
	private void multipleStacksOnStartup() {
		IEditorReference lastFile = null;
		IEditorReference secondLastFile = null;

		IEditorReference[] editors = fActivePage.getEditorReferences();
		assertEquals(Bug95357Test.FILE_MAX, editors.length);

		for (int i = 0; i < editors.length; i++) {
			if (itsFilename[itsFilename.length - 1]
					.equals(editors[i].getName())) {
				lastFile = editors[i];
			} else if (itsFilename[itsFilename.length - 2].equals(editors[i]
					.getName())) {
				secondLastFile = editors[i];
			}
		}

		assertNotNull(lastFile.getEditor(false));
		assertNotNull(secondLastFile.getEditor(false));
		for (int i = 0; i < editors.length; ++i) {
			if (editors[i] != lastFile && editors[i] != secondLastFile) {
				assertNull("For file " + i + " " + editors[i].getName(),
						editors[i].getEditor(false));
			}
		}
	}

	/**
	 * Test for .txt files and the basic editor. Part 1 of 2
	 * 
	 * @throws Throwable
	 */
	public void testMultipleEditors() throws Throwable {
		setupFilenames(".txt");
		
		multipleEditors();
	}

	/**
	 * Test for .txt files and the basic editor. Part 2 of 2
	 * 
	 * @throws Throwable
	 */
	public void testMultipleEditorsOpen() throws Throwable {
		setupFilenames(".txt");
	
		multipleEditorsOpen();
	}

	/**
	 * Test multiple stacks with .txt editor. Part 1 of 2
	 * 
	 * @throws Throwable
	 */
	public void testMultipleStacks() throws Throwable {
		setupFilenames(".txt");
		multipleStacks();
	}

	/**
	 * Test multiple stacks with .txt editor. Part 2 of 2
	 * 
	 * @throws Throwable
	 */
	public void testMultipleStacksOnStartup() throws Throwable {
		setupFilenames(".txt");
		multipleStacksOnStartup();
		
	}
	
	/**
	 * Test for .session files and the SessionEditorPart editor. Part 1 of 2
	 * 
	 * @throws Throwable
	 */
	public void testMultipleEditorsSession() throws Throwable {
		setupFilenames(".session");
		multipleEditors();
		assertEquals(Bug95357Test.FILE_MAX, SessionEditorPart.instantiatedEditors);
		
	}
	
	/**
	 * Test for .session files and the SessionEditorPart editor. Part 2 of 2
	 * 
	 * @throws Throwable
	 */
	public void testMultipleEditorsOpenSession() throws Throwable {
		setupFilenames(".session");
		multipleEditorsOpen();
		assertEquals(2, SessionEditorPart.instantiatedEditors);
	}

	/**
	 * Test multiple stacks with .session editor. Part 1 of 2
	 * 
	 * @throws Throwable
	 */
	public void testMultipleStacksSession() throws Throwable {
		setupFilenames(".session");
		SessionEditorPart.instantiatedEditors = 0;
		multipleStacks();
		assertEquals(Bug95357Test.FILE_MAX, SessionEditorPart.instantiatedEditors);
		
	}
	
	/**
	 * Test multiple stacks with .session editor. Part 2 of 2
	 * 
	 * @throws Throwable
	 */
	public void testMultipleStacksOnStartupSession() throws Throwable {
		setupFilenames(".session");
		multipleStacksOnStartup();
		assertEquals(2, SessionEditorPart.instantiatedEditors);
	}
}
