package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.NewWizardAction;
import org.eclipse.ui.actions.NewWizardMenu;

/**
 * Invoke the resource creation wizard selection Wizard.
 * This action will retarget to the active view.
 */
public class NewWizardDropDownAction extends Action implements IMenuCreator, IWorkbenchWindowPulldownDelegate2 {
	private IWorkbench workbench;
	private NewWizardAction newWizardAction;
	private MenuManager dropDownMenuMgr;
	private NewWizardMenu newWizardMenu;
/**
 *	Create a new instance of this class
 */
public NewWizardDropDownAction(IWorkbench aWorkbench, NewWizardAction newWizardAction) {
	super(WorkbenchMessages.getString("NewWizardDropDown.text")); //$NON-NLS-1$
	this.workbench = aWorkbench;
	this.newWizardAction = newWizardAction;
	setToolTipText(newWizardAction.getToolTipText());
	setImageDescriptor(newWizardAction.getImageDescriptor());
	setMenuCreator(this);
}
/**
 * create the menu manager for the drop down menu.
 */
protected void createDropDownMenuMgr() {
	if (dropDownMenuMgr == null) {
		dropDownMenuMgr = new MenuManager();
		newWizardMenu = new NewWizardMenu(dropDownMenuMgr, 
			workbench.getActiveWorkbenchWindow(),
			false);
	}
}
/**
 * dispose method comment.
 */
public void dispose() {
	if (dropDownMenuMgr != null) {
		dropDownMenuMgr.dispose();
		dropDownMenuMgr = null;
	}
}
/**
 * getMenu method comment.
 */
public Menu getMenu(Control parent) {
	createDropDownMenuMgr();
	newWizardMenu.updateMenu();
	return dropDownMenuMgr.createContextMenu(parent);
}
/**
 * Create the drop down menu as a submenu of parent.  Necessary
 * for CoolBar support.
 */
public Menu getMenu(Menu parent) {
	createDropDownMenuMgr();
	newWizardMenu.updateMenu();
	Menu menu= new Menu(parent);
	IContributionItem[] items = dropDownMenuMgr.getItems();
	for (int i=0; i<items.length; i++) {
		IContributionItem item = items[i];
		IContributionItem newItem = item;
		if (item instanceof ActionContributionItem) {
			newItem = new ActionContributionItem(((ActionContributionItem)item).getAction());
		}
		newItem.fill(menu, -1);
	}
	return menu;
}
/**
 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
 */
public void init(IWorkbenchWindow window){
}
public void run() {
   newWizardAction.run();
}
/**
 * @see runWithEvent(IAction, Event)
 * @see IActionDelegate#run(IAction)
 */
public void run(IAction action) {
}
/**
 * @see IActionDelegate#selectionChanged(IAction, ISelection)
 */
public void selectionChanged(IAction action, ISelection selection){
}
}
