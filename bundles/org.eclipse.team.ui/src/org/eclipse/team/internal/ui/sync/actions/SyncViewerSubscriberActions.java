/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.sync.views.SynchronizeView;

/**
 * This class manages the actions contributed by the subscriber.
 */
public class SyncViewerSubscriberActions extends SyncViewerActionGroup {

	// cache of the subscriber actins
	private HashMap definitions; // Subscriber class name -> SubscriberAction[]
	private ContributedSubscriberAction[] actions;
	
	/**
	 * @param syncView
	 */
	protected SyncViewerSubscriberActions(SynchronizeView syncView) {
		super(syncView);
		loadDefinitions();
	}

	private void loadDefinitions() {
		IExtensionPoint point = Platform.getPluginRegistry().getExtensionPoint(TeamUIPlugin.ID, TeamUIPlugin.PT_SUBSCRIBER_MENUS);
		IExtension[] types = point.getExtensions();
		definitions = new HashMap(types.length);
		for (int i = 0; i < types.length; i++)
			loadDefinitions(types[i]);
	}

	private void loadDefinitions(IExtension type) {
		IConfigurationElement[] elements = type.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			String subscriberName = getSubscriberClassName(element);
			if (subscriberName != null) {
				definitions.put(getSubscriberClassName(element), createActions(element));
			}
		}
	}

	/**
	 * @param element
	 * @return
	 */
	private ContributedSubscriberAction[] createActions(IConfigurationElement element) {
		IConfigurationElement[] children = element.getChildren();
		List result = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			IConfigurationElement actionDefinition = children[i];
			ContributedSubscriberAction action = new ContributedSubscriberAction(getSyncView(), actionDefinition);
			result.add(action);
		}
		return (ContributedSubscriberAction[]) result.toArray(new ContributedSubscriberAction[result.size()]);
	}

	/**
	 * @param extension
	 * @return
	 */
	private String getSubscriberClassName(IConfigurationElement element) {
		return (String)element.getAttribute("subscriberClass");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		if (actions == null || actions.length == 0) return;
		menu.add(new Separator());
		ISelection selection = getSyncView().getSelection();
		for (int i = 0; i < actions.length; i++) {
			ContributedSubscriberAction action = actions[i];
			action.setActivePart(getSyncView().getSite().getPage().getActivePart());
			action.setContext(getSubscriberContext());
			action.selectionChanged(selection);
			menu.add(action);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.actions.SyncViewerActionGroup#initializeActions()
	 */
	protected void initializeActions() {
		super.initializeActions();
		if(getSubscriberContext() != null) {
			String className = getSubscriberContext().getSubscriber().getClass().getName();
			actions = (ContributedSubscriberAction[])definitions.get(className);
		}
	}

}
