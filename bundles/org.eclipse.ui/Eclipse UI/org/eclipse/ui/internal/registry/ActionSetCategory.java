package org.eclipse.ui.internal.registry;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import java.util.ArrayList;

/**
 *
 */
public class ActionSetCategory {
	private String label;
	private ArrayList actionSets;
/**
 * ActionSetCategory constructor comment.
 */
public ActionSetCategory(String label) {
	super();
	this.label = label;
}
/**
 * Adds an action set to this category.
 */
public void addActionSet(IActionSetDescriptor desc) {
	if (actionSets == null)
		actionSets = new ArrayList(5);
	actionSets.add(desc);
}
/**
 * Returns the action sets for this category.
 * May be null.
 */
public ArrayList getActionSets() {
	return actionSets;
}
/**
 * Returns category name.
 */
public String getLabel() {
	return label;
}
}
