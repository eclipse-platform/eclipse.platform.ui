package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.actions.NewWizardAction;
import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import java.util.*;

/**
 * Invoke the resource creation wizard selection Wizard.
 * This action will retarget to the active view.
 */
public class NewWizardDropDownAction extends Action implements IMenuCreator {
	private IWorkbench workbench;
	private NewWizardAction newWizardAction;
	private MenuManager dropDownMenuMgr;
	private NewWizardMenu newWizardMenu;
/**
 *	Create a new instance of this class
 */
public NewWizardDropDownAction(IWorkbench aWorkbench, NewWizardAction newWizardAction) {
	super("&New Wizards");
	this.workbench = aWorkbench;
	this.newWizardAction = newWizardAction;
	setToolTipText(newWizardAction.getToolTipText());
	setImageDescriptor(newWizardAction.getImageDescriptor());
	setMenuCreator(this);
}
private void createMenuForAction(Menu parent, Action action) {
	ActionContributionItem item = new ActionContributionItem(action);
	item.fill(parent, -1);
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
	if (dropDownMenuMgr == null) {
		dropDownMenuMgr = new MenuManager();
		newWizardMenu = new NewWizardMenu(dropDownMenuMgr, 
			workbench.getActiveWorkbenchWindow(),
			false);
	}
	return dropDownMenuMgr.createContextMenu(parent);
}
public Menu getMenu(Menu parent) {
	return null;
}
public void run() {
   newWizardAction.run();
}
}
