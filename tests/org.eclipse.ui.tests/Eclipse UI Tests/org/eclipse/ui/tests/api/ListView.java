package org.eclipse.ui.tests.api;

import java.util.ArrayList;

import junit.framework.TestCase;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * An ElementViewPart shows a bunch of elements in a list
 * viewer.
 */
public class ListView extends MockViewPart 
	implements IMenuListener
{

	ListViewer viewer;
	ArrayList input;
	MenuManager menuMgr;
	Menu menu;
	Action fakeAction;
	String FAKE_ACTION_ID = "fakeAction";
	
	/**
	 * Constructor for ElementViewPart
	 */
	public ListView() {
		super();
		input = new ArrayList();
		input.add(new ListElement("Fred"));
		input.add(new ListElement("Barney"));
		input.add(new ListElement("Wilma"));
		input.add(new ListElement("Betty"));
	}

	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		callTrace.add("createPartControl");
		
		// Create viewer.
		viewer = new ListViewer(parent);
		viewer.setLabelProvider(new LabelProvider());
		viewer.setContentProvider(new ListContentProvider());
		viewer.setInput(input);
		
		// Create popup menu.
		createPopupMenu();
	}

	/**
	 * Creates a popup menu.
	 */	
	public void createPopupMenu() {
		// Create actions.
		fakeAction = new Action("Fake") {
			public void run() {
			}
		};
		fakeAction.setId(FAKE_ACTION_ID);
		
		// Create popup menu.
		IConfigurationElement config = getConfig();
		String str = config.getAttributeAsIs("menuType");
		if (str.equals("static"))
			createStaticPopupMenu();
		else	
			createDynamicPopupMenu();
	}
	
	/**
	 * Creates a dynamic popup menu.
	 */	
	public void createDynamicPopupMenu() {
		menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}
	
	/**
	 * Creates a static popup menu.
	 */	
	public void createStaticPopupMenu() {
		menuMgr = new MenuManager();
		menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
		menuAboutToShow(menuMgr);
	}
	
	public void addElement(ListElement el) {
		input.add(el);
		viewer.refresh();
	}
	
	public void selectElement(ListElement el) {
		if (el == null)
			viewer.setSelection(new StructuredSelection());
		else
			viewer.setSelection(new StructuredSelection(el));
	}
	
	public MenuManager getMenuManager() {
		return menuMgr;
	}
	
	/**
	 * @see IMenuListener#menuAboutToShow(IMenuManager)
	 */
	public void menuAboutToShow(IMenuManager menuMgr) {
		menuMgr.add(fakeAction);
		menuMgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * Tests that the menu mgr contains the expected actions.
	 */
	public void verifyActions(TestCase test, IMenuManager menuMgr) {
		test.assertNotNull(menuMgr.find(FAKE_ACTION_ID));
	}

}

