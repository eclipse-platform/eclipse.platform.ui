package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;

/**
 * This class marks a sub contribution item as belonging to
 * an action set.
 */
public class ActionSetContributionItem extends SubContributionItem 
	implements IActionSetContributionItem 
{
	private String actionSetId;
/**
 * Constructs a new item
 */
public ActionSetContributionItem(IContributionItem item, String actionSetId) {
	super(item);
	this.actionSetId = actionSetId;
}
/**
 * Returns the action set id.
 */
public String getActionSetId() {
	return actionSetId;
}
/**
 * Sets the action set id.
 */
public void setActionSetId(String newActionSetId) {
	actionSetId = newActionSetId;
}
}
