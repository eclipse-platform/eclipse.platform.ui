package org.eclipse.ui.internal;

/**
 * This interface should be implemented by all contribution items
 * defined by an action set.
 */
public interface IActionSetContributionItem {

/**
 * Returns the action set id.
 */
public String getActionSetId();
/**
 * Sets the action set id.
 */
public void setActionSetId(String newActionSetId);
}
