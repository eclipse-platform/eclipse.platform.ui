/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize;

import org.eclipse.jface.action.*;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Used to add one or more actions to the context menu, toolbar or view menu 
 * of an {@link ISynchronizePage}. An action group is added to a synchronize
 * page by adding the group to the {@link ISynchronizePageConfiguration} after
 * configuration has been created by the page but before the page is created. 
 * <p>
 * The life cycle of an action group is:
 * <ul>
 * <li>the <code>initialize(ISynchronizePageConfiguration}</code> method is
 * invoked before the methods to populate menus. This is done to give clients 
 * a change to create and initialize the actions of the action group.
 * <li>The <code>fillActionBars(IActionBars)</code> method is invoked
 * to populate the page's action bars (view menu and toolbar). It is
 * possible for the action bars to be missing one or more components
 * so clients are expected to check for <code>null</code> when accessing
 * the menus from the action bars.
 * <li>The <code>fillContextMenu(IMenuManager)</code> method is invoked each time
 * the context menu is shown. Before this method is called, the 
 * action group will be provided with an <code>ActionContext</code>
 * containing the view selection. Clients can access the context using
 * <code>getContext()</code>.
 * <li>The <code>updateActionBars()</code> method is invoked whenever the
 * page's selection changes. Before this method is called, the 
 * action group will be provided with an <code>ActionContext</code>
 * containing the view selection. Clients can access the context using
 * <code>getContext()</code>.
 * <li>The <code>modelChanged(ISynchronizeModelElement)</code> method is
 * invoked whenever the model being displayed is changed. This gives clients
 * a chance to adjust the input to actions that operate on all visible elements.
 * <li>The <code>dispose()</code> method is called when the page is disposed.
 * </ul>
 * <p>
 * TODO: Describe menu configuration and adding actions to groups
 * @since 3.0
 */
public abstract class SynchronizePageActionGroup extends ActionGroup {

	private ISynchronizePageConfiguration configuration;

	/**
	 * Initialize the actions of this contribution.
	 * This method will be invoked once before any calls are
	 * made to <code>filleContextMenu</code> or <code>setActionBars</code>
	 * but after the control for the page has been created. As a result
	 * of this, the site of the configuration can be accessed.
	 * Sublcasses may override this method but must invoke
	 * the overriden method.
	 * @param configuration the configuration for the part to which
	 * the contribution is associated
	 */
	public void initialize(ISynchronizePageConfiguration configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * This method is invoked whenever the model being displayed in the
	 * view changes. This includes when the input to the view changes
	 * and when the children of the input change. The default implementation
	 * of this method does nothing. Subclasses may override.
	 * @param root the root of the model being viewed
	 */
	public void modelChanged(ISynchronizeModelElement root) {
		// Do nothing by default
	}
	
	/** 
	 * Dispose of the action group. Subclasses may override but must
	 * invoke the overriden method.
	 */
	public void dispose() {
		super.dispose();
		if (configuration != null) {
			configuration.removeActionContribution(this);
		}
	}
	
	/**
	 * Helper method to find the group of the given id for the page
	 * associated with the configuration of this action group.
	 * The id of the returned group will not match that of the
	 * provided id since the group must be modified to ensure that
	 * groups are unique accross pages.
	 * @param menu the menu
	 * @param groupId the id of the group being searched for
	 * @return the group for the given id or <code>null</code>
	 */
	protected IContributionItem findGroup(IContributionManager menu, String groupId) {
		if(menu == null) return null;
		IContributionItem item = menu.find(((SynchronizePageConfiguration)configuration).getGroupId(groupId));
		if (item == null) {
			// Context menus do not change the id
			item = menu.find(groupId);
		}
		return item;
	}
	
	/**
	 * Helper method to add an action to a group in a menu. The action is only
	 * added to the menu if the group exists in the menu. Calling this method 
	 * also has no effect if either the menu or action are <code>null</code>.
	 * @param manager the menu manager
	 * @param groupId the group to append the action to
	 * @param action the action to add
	 * @return <code>true</code> if the group exists and the action was added
	 * and <code>false</code> if the action was not added
	 */
	protected boolean appendToGroup(IContributionManager manager, String groupId, IAction action) {
		if (manager == null || action == null) return false;
		IContributionItem group = findGroup(manager, groupId);
		if (group != null) {
			manager.appendToGroup(group.getId(), action);
			return true;
		}
		return false;
	}
	
	/**
	 * Helper method to add a contribution item to a group in a menu. The item is only
	 * added to the menu if the group exists in the menu. Calling this method 
	 * also has no effect if either the menu or item are <code>null</code>.
	 * @param manager the menu manager
	 * @param groupId the group to append the action to
	 * @param item the item to add
	 * @return <code>true</code> if the group exists and the action was added
	 * and <code>false</code> if the action was not added
	 */
	protected boolean appendToGroup(IContributionManager manager, String groupId, IContributionItem item) {
		if (manager == null || item == null) return false;
		IContributionItem group = findGroup(manager, groupId);
		if (group != null) {
			manager.appendToGroup(group.getId(), item);
			return true;
		}
		return false;
	}
}
