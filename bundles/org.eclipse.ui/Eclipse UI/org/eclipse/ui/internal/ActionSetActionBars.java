package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;

/**
 * This class represents the action bars for an action set.
 */
public class ActionSetActionBars extends SubActionBars {
	private String actionSetId;
/**
 * Constructs a new action bars object
 */
public ActionSetActionBars(IActionBars parent, String actionSetId) {
	super(parent);
	this.actionSetId = actionSetId;
}
/* (non-Javadoc)
 * Inherited from SubActionBars.
 */
protected SubMenuManager createSubMenuManager(IMenuManager parent) {
	return new ActionSetMenuManager(parent, actionSetId);
}
/* (non-Javadoc)
 * Inherited from SubActionBars.
 */
protected SubToolBarManager createSubToolBarManager(IToolBarManager parent) {
	return new ActionSetToolBarManager(parent, actionSetId);
}
}
