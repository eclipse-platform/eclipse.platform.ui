package org.eclipse.ui.tests.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.action.*;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.tests.api.*;
import org.eclipse.ui.tests.util.FileUtil;
import org.eclipse.ui.tests.util.UITestCase;


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
		IToolBarManager tbm = ((WorkbenchWindow)fWindow).getToolsManager();
		if (tbm instanceof ToolBarManager) {
			ToolBar tb = ((ToolBarManager) tbm).getControl();
			ToolItem [] items = tb.getItems();
			for (int i = 0; i < items.length; i ++) {
				String itemText = items[i].getToolTipText();
				if (actionText.equals(itemText)) {
					assertEquals(enabled, items[i].getEnabled());
					return;
				}
			}
		}
		else if (tbm instanceof CoolBarManager) {
			IContributionItem[] coolItems = tbm.getItems();
			for (int i = 0; i < coolItems.length; ++i) {
				if (coolItems[i] instanceof CoolBarContributionItem) {
					CoolBarContributionItem coolItem = (CoolBarContributionItem) coolItems[i];
					ToolBarManager citbm = coolItem.getToolBarManager();
					ToolBar tb = ((ToolBarManager) citbm).getControl();
					ToolItem [] items = tb.getItems();
					for (int j = 0; j < items.length; j ++) {
						String itemText = items[j].getToolTipText();
						if (actionText.equals(itemText)) {
							assertEquals(enabled, items[j].getEnabled());
							return;
						}
					}
				}
			}
		}
		fail("Action for " + actionText + " not found");
	}
}

