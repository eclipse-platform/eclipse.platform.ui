package org.eclipse.ui.views.properties;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * This action hides or shows expert properties in the <code>PropertySheetViewer</code>.
 */
/*package*/ class FilterAction extends PropertySheetAction {
/**
 * Create the Filter action. This action is used to show
 * or hide expert properties.
 */
public FilterAction(PropertySheetViewer viewer, String name) {
	super(viewer, name);
	setToolTipText("Show/hide advanced properties");
}
/**
 * Toggle the display of expert properties.
 */

public void run() {
	PropertySheetViewer ps = getPropertySheet();
	ps.deactivateCellEditor();
	if (isChecked()) {
		ps.showExpert();
	} else {
		ps.hideExpert();
	}
}
}
