package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.jdt.junit.util.*;


/**
 * Tests the lifecycle for an editor action delegate.
 */
public class IEditorActionDelegateTest extends IActionDelegateTest {
	
	public static String EDITOR_ID = "org.eclipse.ui.tests.api.IEditorActionDelegateTest";
	private MockEditorPart editor;

	/**
	 * Constructor for IWorkbenchWindowActionDelegateTest
	 */
	public IEditorActionDelegateTest(String testName) {
		super(testName);
	}

	public void testSetActiveEditor() throws Throwable {
		// When an action delegate is run the
		// setActiveEditor, selectionChanged, and run methods should
		// be called, in that order.
		
		// Run the action.
		testRun();
		
		// Verify lifecycle.
		MockActionDelegate delegate = getDelegate();
		assertNotNull(delegate);
		assertTrue(delegate.callHistory.verifyOrder(
			new String [] {"setActiveEditor", "selectionChanged", "run"}));
	}
	
	/**
	 * @see IActionDelegateTest#runAction()
	 */
	protected void addAndRunAction() throws Throwable {
		editor = openEditor(fPage, "X");
		MockEditorActionBarContributor contributor = 	
			(MockEditorActionBarContributor)
				editor.getEditorSite().getActionBarContributor();
		IMenuManager mgr = contributor.getActionBars().getMenuManager();
		ActionUtil.runActionWithLabel(this, mgr, "Mock Action");
	}

	protected MockEditorPart openEditor(IWorkbenchPage page, String suffix) 
		throws Throwable 
	{
		IProject proj = FileUtil.createProject("IEditorActionDelegateTest");
		IFile file = FileUtil.createFile("test" + suffix + ".txt", proj);
		return (MockEditorPart)page.openEditor(file, EDITOR_ID);
	}

	/**
	 * @see IActionDelegateTest#fireSelection()
	 */
	protected void fireSelection() throws Throwable {
		editor.fireSelection();
	}
}

