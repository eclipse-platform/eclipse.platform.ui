package org.eclipse.ui.internal;

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

