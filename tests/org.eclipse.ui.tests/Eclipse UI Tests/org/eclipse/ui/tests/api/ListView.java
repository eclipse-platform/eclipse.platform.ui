package org.eclipse.ui.tests.api;

import java.util.ArrayList;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.junit.util.CallHistory;

/**
 * An ElementViewPart shows a bunch of elements in a list
 * viewer.
 */
public class ListView extends MockViewPart {

	ListViewer viewer;
	ArrayList input;
	MenuManager menuMgr;
	Menu menu;
	
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
		menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}
	
	public ListElement addElement(String name) {
		ListElement result = new ListElement(name);
		input.add(result);
		viewer.refresh();
		return result;
	}
	
	public void selectElement(ListElement el) {
		StructuredSelection sel = new StructuredSelection(el);
		viewer.setSelection(sel);
	}
	
	public MenuManager getMenuManager() {
		return menuMgr;
	}
}

