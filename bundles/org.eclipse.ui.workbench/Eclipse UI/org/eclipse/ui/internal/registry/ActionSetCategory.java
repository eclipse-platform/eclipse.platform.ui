package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import java.util.ArrayList;

/**
 *
 */
public class ActionSetCategory {
	private String id;
	private String label;
	private ArrayList actionSets;
/**
 * ActionSetCategory constructor comment.
 */
public ActionSetCategory(String id, String label) {
	super();
	this.id = id;
	this.label = label;
}
/**
 * Adds an action set to this category.
 */
public void addActionSet(IActionSetDescriptor desc) {
	if (actionSets == null)
		actionSets = new ArrayList(5);
	actionSets.add(desc);
	desc.setCategory(id);
}
/**
 * Returns the action sets for this category.
 * May be null.
 */
public ArrayList getActionSets() {
	return actionSets;
}
/**
 * Returns category id.
 */
public String getId() {
	return id;
}
/**
 * Returns category name.
 */
public String getLabel() {
	return label;
}
}
