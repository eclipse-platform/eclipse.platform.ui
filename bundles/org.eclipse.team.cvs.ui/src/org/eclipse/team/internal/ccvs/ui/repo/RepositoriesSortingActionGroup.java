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
	// action for switching between asc/desc sorting order
	private Action reverseSortingOrderAction;

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
				CVSUIMessages.RepositoriesSortingActionGroup_label,
				Action.AS_RADIO_BUTTON) {
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
		hostSortingAction = new Action(
				CVSUIMessages.RepositoriesSortingActionGroup_host,
				Action.AS_RADIO_BUTTON) {
			public void run() {
				if (hostSortingAction.isChecked())
					setComparator(orderByHostComparator);
			}
		};
		reverseSortingOrderAction = new Action(
				CVSUIMessages.RepositoriesSortingActionGroup_descending,
				Action.AS_CHECK_BOX) {
			public void run() {
				switchOrder(comparator);
			}
		};
		// set sorting by label as default
		setSelectedComparator(orderByLabelComparator);
		labelSortingAction.setChecked(true);
		reverseSortingOrderAction.setChecked(!orderByLabelComparator.isAscending());
	}

	/* package */void setComparator(RepositoryComparator newComparator) {
		RepositoryComparator oldComparator = this.comparator;
		// preserve sorting order
		if (oldComparator != null)
			newComparator.setAscending(oldComparator.isAscending());
		this.comparator = newComparator;
		firePropertyChange(newComparator, oldComparator);
	}

	private void switchOrder(RepositoryComparator currentComparator) {
		RepositoryComparator oldComparator = this.comparator;
		RepositoryComparator switchedComparator = currentComparator
				.getReversedComparator();
		this.comparator = switchedComparator;
		firePropertyChange(switchedComparator, oldComparator);
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
		sortSubmenu.add(new Separator());
		sortSubmenu.add(reverseSortingOrderAction);
	}

	public void setSelectedComparator(RepositoryComparator selectedComparator) {
		this.comparator = selectedComparator;

		labelSortingAction
				.setChecked(selectedComparator.getOrderBy() == RepositoryComparator.ORDER_DEFAULT);
		locationSortingAction
				.setChecked(selectedComparator.getOrderBy() == RepositoryComparator.ORDER_LOCATION);
		hostSortingAction
				.setChecked(selectedComparator.getOrderBy() == RepositoryComparator.ORDER_HOST);

		reverseSortingOrderAction.setChecked(!selectedComparator.isAscending());

		firePropertyChange(comparator, null);
	}
}
