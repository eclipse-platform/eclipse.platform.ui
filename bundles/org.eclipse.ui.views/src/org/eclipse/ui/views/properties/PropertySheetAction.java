package org.eclipse.ui.views.properties;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.*;

/**
 * This is the base class of all the local actions used
 * in the PropertySheet.
 */
/*package*/ abstract class PropertySheetAction extends Action {
	protected PropertySheetViewer viewer;
	private String id;
/**
 * Create a PropertySheetViewer action.
 */
protected PropertySheetAction(PropertySheetViewer viewer, String name) {
	super (name);
	this.id = name;
	this.viewer = viewer;
}
/**
 * Return the unique action ID that will be
 * used in contribution managers.
 */
public String getId() {
	return id;
}
/**
 * Return the PropertySheetViewer
 */
public PropertySheetViewer getPropertySheet() {
	return viewer;
}
/**
 * Set the unique ID that should be used
 * in the contribution managers.
 */
public void setId(String newId) {
	id = newId;
}
}
