/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * FiltersContribution is the contribution for the filters menu.
 * 
 * @since 3.4
 * 
 */
public class FiltersContribution extends MarkersContribution {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
	 */
	protected IContributionItem[] getContributionItems() {
		final ExtendedMarkersView view = getView();
		if (view == null)
			return new IContributionItem[0];

		Collection groups = view.getAllFilters();

		if (groups.size() == 0)
			return new IContributionItem[0];

		Iterator groupsIterator = groups.iterator();
		IContributionItem[] items = new IContributionItem[groups.size() + 2];
		for (int i = 0; i < groups.size(); i++) {
			final MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) groupsIterator
					.next();
			items[i] = new ContributionItem() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Menu,
				 *      int)
				 */
				public void fill(Menu menu, int index) {
					MenuItem item = new MenuItem(menu, SWT.CHECK);
					item.setText(group.getName());
					item.addListener(SWT.Selection, getMenuItemListener(group,
							view));

					if (view != null && view.isEnabled(group)) {
						item.setSelection(true);
					}
				}

				/**
				 * Return the menu item listener for selection of a filter.
				 * 
				 * @param group
				 * @param view
				 * @return Listener
				 */
				private Listener getMenuItemListener(
						final MarkerFieldFilterGroup group,
						final ExtendedMarkersView view) {
					return new Listener() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
						 */
						public void handleEvent(Event event) {
							if (view != null)
								view.toggleFilter(group);
						}
					};
				}
			};
		}

		items[groups.size()] = new Separator();
		items[groups.size() + 1] = getShowAllContribution();

		return items;

	}

	/**
	 * Return the show all contribution.
	 * 
	 * @return IContributionItem
	 */
	private IContributionItem getShowAllContribution() {
		return new ContributionItem() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Menu,
			 *      int)
			 */
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.CHECK);
				item.setText(MarkerMessages.MarkerFilter_showAllCommand_title);
				item.setSelection(noFiltersSelected());

				item.addListener(SWT.Selection, new Listener() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
					 */
					public void handleEvent(Event event) {
						ExtendedMarkersView view = getView();
						if (view != null)
							view.disableAllFilters();
					}
				});
			}

			/**
			 * Return whether or not any filters are selected.
			 * 
			 * @return boolean <code>true</code> if none of the current
			 *         filters are selected.
			 */
			private boolean noFiltersSelected() {
				ExtendedMarkersView view = getView();
				if (view == null)
					return true;

				Iterator groupsIterator= view.getAllFilters().iterator();
				while (groupsIterator.hasNext()) {
					MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) groupsIterator
							.next();
					if (group.isEnabled())
						return false;
				}
				return true;
			}

		};
	}

}
