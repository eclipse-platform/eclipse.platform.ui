package org.eclipse.ui.views.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.help.*;

/**
 * This action hides or shows categories in the <code>PropertySheetViewer</code>.
 */
/*package*/ class CategoriesAction extends PropertySheetAction {
/**
 * Creates the Categories action. This action is used to show
 * or hide categories properties.
 */
public CategoriesAction(PropertySheetViewer viewer, String name) {
	super(viewer, name);
	WorkbenchHelp.setHelp(this, IPropertiesHelpContextIds.CATEGORIES_ACTION);
}
/**
 * Toggles the display of categories for the properties.
 */
public void run() {
	PropertySheetViewer ps = getPropertySheet();
	ps.deactivateCellEditor();
	if (isChecked()) {
		ps.showCategories();
	} else {
		ps.hideCategories();
	}
}
}
