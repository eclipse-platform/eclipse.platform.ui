package org.eclipse.ui.tests.internal;

import org.eclipse.core.resources.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.tests.util.*;
import org.eclipse.ui.tests.api.*;


/**
 * This class contains tests for the editor action bars
 * implementation.
 */
public class EditorActionBarsTest extends UITestCase {

	protected IWorkbenchWindow fWindow;
	protected IWorkbenchPage fPage;
	private String EDITOR_ID = "org.eclipse.ui.tests.internal.EditorActionBarsTest";
	
	/**
	 * Constructor for IEditorPartTest
	 */
	public EditorActionBarsTest(String testName) {
		super(testName);
	}

	public void setUp() {
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}
	
	/**
	 * Test action enablement / disablement when a
	 * part is active.
	 * <p>
	 * Created for PR 1GJNB52: ToolItems in EditorToolBarManager can get 
	 * out of synch with the state of the IAction
	 * </p>
	 */
	public void testActionEnablementWhenActive() throws Throwable {
		// Open an editor.
		MockEditorPart editor = openEditor(fPage, "1");
		MockEditorActionBarContributor contributor = 
			(MockEditorActionBarContributor)editor.getEditorSite().getActionBarContributor();
		
		// Enable all actions.
		contributor.enableActions(true);
		verifyToolItemState(contributor, true);
		
		// Disable all actions.
		contributor.enableActions(false);
		verifyToolItemState(contributor, false);
	}
	
	/**
	 * Test action enablement / disablement when a
	 * part is inactive.
	 * <p>
	 * Created for PR 1GJNB52: ToolItems in EditorToolBarManager can get 
	 * out of synch with the state of the IAction
	 * </p>
	 */
	public void testActionEnablementWhenInactive() throws Throwable {
		// Open an editor.
		MockEditorPart editor = openEditor(fPage, "2");
		MockEditorActionBarContributor contributor = 
			(MockEditorActionBarContributor)editor.getEditorSite().getActionBarContributor();
		
		// Enable all actions.
		contributor.enableActions(true);
		verifyToolItemState(contributor, true);
		
		// Activate some other part.  Disable the actions.
		// Then activate the editor and test tool item state.
		fPage.showView(MockViewPart.ID);
		contributor.enableActions(false);
		fPage.activate(editor);
		verifyToolItemState(contributor, false);
		
		// Activate some other part.  Enable the actions.
		// Then activate the editor and test tool item state.
		fPage.showView(MockViewPart.ID);
		contributor.enableActions(true);
		fPage.activate(editor);
		verifyToolItemState(contributor, true);
	}
	
	/**
	 * Open a test editor.
	 */
	protected MockEditorPart openEditor(IWorkbenchPage page, String suffix) 
		throws Throwable 
	{
		IProject proj = FileUtil.createProject("IEditorActionBarsTest");
		IFile file = FileUtil.createFile("test" + suffix + ".txt", proj);
		return (MockEditorPart)page.openEditor(file, EDITOR_ID);
	}
	
	/**
	 * Tests whether actions are enabled.
	 */
	protected void verifyToolItemState(MockEditorActionBarContributor ctr,
		boolean enabled) 
	{
		MockAction [] actions = ctr.getActions();
		for (int nX = 0; nX < actions.length; nX ++)
			verifyToolItemState(actions[nX], enabled);
	}
	
	/**
	 * Tests whether an action is enabled.
	 */
	protected void verifyToolItemState(IAction action, boolean enabled) {
		String actionText = action.getText();
		ToolBarManager tbm = ((WorkbenchWindow)fWindow).getToolBarManager();
		ToolBar tb = tbm.getControl();
		ToolItem [] items = tb.getItems();
		for (int nX = 0; nX < items.length; nX ++) {
			String itemText = items[nX].getToolTipText();
			if (actionText.equals(itemText)) {
				assertEquals(enabled, items[nX].getEnabled());
				return;
			}
		}
		fail("Action for " + actionText + " not found");
	}
}

