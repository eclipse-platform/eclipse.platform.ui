package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.model.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import java.util.*;

/**
 * Edit the action sets.
 */
public class EditActionSetsAction  extends Action {
	private IWorkbenchWindow window;
/**
 * 
 */
public EditActionSetsAction(IWorkbenchWindow window) {
	super("Customize...");
	setToolTipText("Customize the current perspective...");
	this.window = window;
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.EDIT_ACTION_SETS_ACTION});
}
/**
 * Open the selected resource in the default page.
 */
public void run() {
	WorkbenchPage page = (WorkbenchPage)window.getActivePage();
	if (page == null)
		return;
	page.editActionSets();
}
}
