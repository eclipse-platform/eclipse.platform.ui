package org.eclipse.ui.views.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.help.*;

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
	setToolTipText(PropertiesMessages.getString("Filter.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IPropertiesHelpContextIds.FILTER_ACTION);
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
