package org.eclipse.ui.internal;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public class ShowViewMenuAction extends ShowPartPaneMenuAction {

/**
 * Constructor for ShowViewMenuAction.
 * @param window
 */
public ShowViewMenuAction(WorkbenchWindow window) {
	super(window);
}

/**
 * Initialize the menu text and tooltip.
 */
protected void initText() {
	setText(WorkbenchMessages.getString("ShowViewMenuAction.text"));
	setToolTipText(WorkbenchMessages.getString("ShowViewMenuAction.toolTip"));
}
/**
 * Show the pane title menu.
 */
protected void showMenu(PartPane pane) {
	pane.showViewMenu();
}
}

	/**
	 * See IPartListener
	 */
	public void partActivated(IWorkbenchPart part) {
		super.partActivated(part);

		//All of the conditions in the super class passed
		//now check for the menu.
		if (isEnabled()) {
			PartPane pane = (((PartSite) part.getSite()).getPane());
			if (pane instanceof ViewPane)
				setEnabled(((ViewPane) pane).hasViewMenu());
		}
	}
}