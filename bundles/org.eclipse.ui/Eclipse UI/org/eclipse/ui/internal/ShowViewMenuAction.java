package org.eclipse.ui.internal;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public class ShowViewMenuAction extends ShowPartPaneMenuAction {

/**
 * Constructor for ShowViewMenuAction.
 * @param window
 */
public ShowViewMenuAction(WorkbenchWindow window, String id) {
	super(window, id);
}
/**
 * Show the pane title menu.
 */
protected void showMenu(PartPane pane) {
	pane.showViewMenu();
}

/**
 * Updates the enabled state.
 */
protected void updateState() {
	super.updateState();

	//All of the conditions in the super class passed
	//now check for the menu.
	if (isEnabled()) {
		PartPane pane = (((PartSite) getActivePart().getSite()).getPane());
		if (pane instanceof ViewPane)
			setEnabled(((ViewPane) pane).hasViewMenu());
	}
}
}