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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * GroupsContribution is the contribution for the marker groupings.
 * 
 * @since 3.4
 * 
 */
public class GroupsContribution extends MarkersContribution {

	/**
	 * Create a new instance of the receiver.
	 */
	public GroupsContribution() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
	 */
	protected IContributionItem[] getContributionItems() {
		ExtendedMarkersView view = getView();
		if (view == null)
			return new IContributionItem[0];

		Collection groups = view.getBuilder().getGenerator().getMarkerGroups();

		if (groups.isEmpty())
			return new IContributionItem[0];

		Iterator groupsIterator = groups.iterator();
		IContributionItem[] items = new IContributionItem[groups.size() + 1];

		for (int i = 0; i < items.length - 1; i++) {
			final MarkerGroup group = (MarkerGroup) groupsIterator.next();
			items[i] = getContributionItem(group);
		}

		items[items.length - 1] = getContributionItem(null);
		return items;

	}

	/**
	 * Return the IContributionItem for group.
	 * 
	 * @param group
	 * @return IContributionItem
	 */
	private IContributionItem getContributionItem(final MarkerGroup group) {
		return new ContributionItem() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Menu,
			 *      int)
			 */
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.RADIO);
				item.setText(group == null ? MarkerMessages.ProblemView_None
						: group.getMarkerField().getName());
				ExtendedMarkersView view = getView();
				item.addListener(SWT.Selection,
						getMenuItemListener(group, view));

				if (view != null) {
					if (group == null)
						item.setSelection(view.getCategoryGroup() == null);
					else
						item
								.setSelection(group.equals(view
										.getCategoryGroup()));
				}

			}

			/**
			 * Return the menu item listener for selection of a filter.
			 * 
			 * @param group
			 * @param view
			 * @return Listener
			 */
			private Listener getMenuItemListener(final MarkerGroup group,
					final ExtendedMarkersView view) {
				return new Listener() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
					 */
					public void handleEvent(Event event) {

						MenuItem item = (MenuItem) event.widget;

						if (item.getSelection() && view != null)
							view.setCategoryGroup(group);
					}
				};
			}
		};
	}
}
