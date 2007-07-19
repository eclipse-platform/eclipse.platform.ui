/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;

public class RepositoriesSortingActionGroup extends ActionGroup {
	// Sorting actions
	private Action labelSortingAction;
	private Action locationSortingAction;
	private Action hostSortingAction;

	/**
	 * Current comparator set.
	 */
	private RepositoryComparator comparator;
	private IPropertyChangeListener comparatorUpdater;

	/* package */static final RepositoryComparator orderByLabelComparator = new RepositoryComparator(/* default */);
	/* package */static final RepositoryComparator orderByLocationComparator = new RepositoryComparator(
			RepositoryComparator.ORDER_LOCATION);
	/* package */static final RepositoryComparator orderByHostComparator = new RepositoryComparator(
			RepositoryComparator.ORDER_HOST);

	/**
	 * Indicates if comparator was changed
	 */
	public static final String CHANGE_COMPARATOR = "changeComparator"; //$NON-NLS-1$

	private static final String REPOSITORIES_SORTING_ACTION_GROUP = "repositoriesSortingActionGroup"; //$NON-NLS-1$

	public RepositoriesSortingActionGroup(Shell shell,
			IPropertyChangeListener comparatorUpdater) {
		Assert.isNotNull(shell);

		this.comparatorUpdater = comparatorUpdater;
		labelSortingAction = new Action(
				CVSUIMessages.RepositoriesSortingActionGroup_label, Action.AS_RADIO_BUTTON) {
			public void run() {
				if (labelSortingAction.isChecked())
					setComparator(orderByLabelComparator);
			}
		};
		locationSortingAction = new Action(
				CVSUIMessages.RepositoriesSortingActionGroup_location,
				Action.AS_RADIO_BUTTON) {
			public void run() {
				if (locationSortingAction.isChecked())
					setComparator(orderByLocationComparator);
			}
		};
		hostSortingAction = new Action(CVSUIMessages.RepositoriesSortingActionGroup_host,
				Action.AS_RADIO_BUTTON) {
			public void run() {
				if (hostSortingAction.isChecked())
					setComparator(orderByHostComparator);
			}
		};
		// set sorting by label as default
		setComparator(orderByLabelComparator);
		labelSortingAction.setChecked(true);
	}

	/* package */void setComparator(RepositoryComparator newComparator) {
		RepositoryComparator oldComparator = this.comparator;
		this.comparator = newComparator;
		firePropertyChange(newComparator, oldComparator);
	}

	private void firePropertyChange(RepositoryComparator newComparator,
			RepositoryComparator oldComparator) {
		// Update viewer
		if (comparatorUpdater != null) {
			comparatorUpdater.propertyChange(new PropertyChangeEvent(this,
					CHANGE_COMPARATOR, oldComparator, newComparator));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		IMenuManager menuManager = actionBars.getMenuManager();

		if (menuManager.find(IWorkbenchActionConstants.MB_ADDITIONS) != null)
			menuManager.insertAfter(IWorkbenchActionConstants.MB_ADDITIONS,
					new Separator(REPOSITORIES_SORTING_ACTION_GROUP));
		else
			menuManager.add(new Separator(REPOSITORIES_SORTING_ACTION_GROUP));

		IMenuManager sortSubmenu = new MenuManager(
				CVSUIMessages.RepositoriesSortingActionGroup_sortBy);
		menuManager.appendToGroup(REPOSITORIES_SORTING_ACTION_GROUP,
				sortSubmenu);
		sortSubmenu.add(labelSortingAction);
		sortSubmenu.add(locationSortingAction);
		sortSubmenu.add(hostSortingAction);
	}

	public void setSelectedComparator(String selectedComparator) {
		
		//uncheck all
		labelSortingAction.setChecked(false);
		locationSortingAction.setChecked(false);
		hostSortingAction.setChecked(false);
		
		try {
			switch (Integer.parseInt(selectedComparator)) {
			case RepositoryComparator.ORDER_LOCATION:
				locationSortingAction.setChecked(true);
				firePropertyChange(orderByLocationComparator, null);
				return;
			case RepositoryComparator.ORDER_HOST:
				hostSortingAction.setChecked(true);
				firePropertyChange(orderByHostComparator, null);
				return;
			}
		} catch (NumberFormatException e) {
			// ignore
		}
		// default comparator
		labelSortingAction.setChecked(true);
		firePropertyChange(orderByLabelComparator, null);
	}
}
