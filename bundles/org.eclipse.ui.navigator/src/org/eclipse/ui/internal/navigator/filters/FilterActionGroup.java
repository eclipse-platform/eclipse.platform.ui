/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.navigator.filters;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;

/**
 * Creates the last 5 filters that were touched as view menu options.
 * 
 * @since 3.2
 * 
 */
public class FilterActionGroup extends ActionGroup {	

	private static final String FILTER_ACTION_GROUP = "filterActionGroup"; //$NON-NLS-1$
	
	private static final String FILTER_ACTION_GROUP_FILTERS_START = FILTER_ACTION_GROUP+"Filters-start"; //$NON-NLS-1$

	private static final String FILTER_ACTION_GROUP_FILTERS_END = FILTER_ACTION_GROUP+"Filters-end"; //$NON-NLS-1$	
	
	private SelectFiltersAction selectFiltersAction;
	private CommonViewer commonViewer;
	private INavigatorViewerDescriptor viewerDescriptor;
	
	private final Set filterShortcutActions = new LinkedHashSet();
	

	/**
	 * @param aCommonViewer The viewer this action group is associated with
	 */
	public FilterActionGroup(CommonViewer aCommonViewer) {
		Assert.isNotNull(aCommonViewer);
		commonViewer = aCommonViewer;
		viewerDescriptor = commonViewer.getNavigatorContentService().getViewerDescriptor();
		makeActions();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		IMenuManager menu = actionBars.getMenuManager();
		menu.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS,
				new Separator(FILTER_ACTION_GROUP));
		if (selectFiltersAction != null) { 
			menu.appendToGroup(FILTER_ACTION_GROUP,
					selectFiltersAction);
			
			menu.appendToGroup(FILTER_ACTION_GROUP,
					new GroupMarker(FILTER_ACTION_GROUP_FILTERS_START));
			
			menu.appendToGroup(FILTER_ACTION_GROUP_FILTERS_START,
					new Separator(FILTER_ACTION_GROUP_FILTERS_END));
			
			
			for (Iterator iter = filterShortcutActions.iterator(); iter.hasNext();) {
				IAction action = (IAction) iter.next();
				menu.appendToGroup(FILTER_ACTION_GROUP_FILTERS_START, action);				
			}			
			
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) { 
		super.fillContextMenu(menu);
	}

	/**
	 * 
	 */
	private void makeActions() {
		boolean hideAvailableCustomizationsDialog = viewerDescriptor
				.getBooleanConfigProperty(INavigatorViewerDescriptor.PROP_HIDE_AVAILABLE_CUSTOMIZATIONS_DIALOG);
		if (!hideAvailableCustomizationsDialog) {
			selectFiltersAction = new SelectFiltersAction(commonViewer, this);
			ImageDescriptor selectFiltersIcon = NavigatorPlugin.getImageDescriptor("icons/full/elcl16/filter_ps.gif"); //$NON-NLS-1$ 
			selectFiltersAction.setImageDescriptor(selectFiltersIcon);
			selectFiltersAction.setHoverImageDescriptor(selectFiltersIcon);
		}
	}
	
	protected void updateFilterShortcuts() {
		
	}
}
