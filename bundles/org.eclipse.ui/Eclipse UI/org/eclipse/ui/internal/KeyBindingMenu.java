package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Menu;

/**
 * A submenu with an item for each accelerator in the AcceleratorScope table.
 */

public class KeyBindingMenu extends ContributionItem {

	private WorkbenchWindow workbenchWindow;
	private Menu acceleratorsMenu;
	private MenuItem cascade;

	public KeyBindingMenu(WorkbenchWindow window) {
		super("Key binding menu"); //$NON-NLS-1$
		this.workbenchWindow = window;
	}

	public void fill(final Menu parent, int index) {
		cascade = new MenuItem(parent, SWT.CASCADE,index);
		cascade.setText("key binding menu");
		cascade.setMenu (acceleratorsMenu = new Menu (cascade));
		workbenchWindow.getKeyBindingService().setAcceleratorsMenu(acceleratorsMenu);
//		parent.addListener (SWT.Show, new Listener () {
//			public void handleEvent (Event event) {
//				cascade.setMenu (null);
//				cascade.dispose ();
//			}
//		});
//		parent.addListener (SWT.Hide, new Listener () {
//			public void handleEvent (Event event) {
//				cascade = new MenuItem (parent, SWT.CASCADE, 0);
//				cascade.setMenu(acceleratorsMenu);
//			}
//		});
	}
	
	public Menu getAcceleratorsMenu() {
		return acceleratorsMenu;
	}
}
