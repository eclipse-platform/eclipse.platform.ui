/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * FiltersContribution is the contribution for the filters menu.
 *
 * @since 3.4
 */
public class FiltersContribution extends MarkersContribution {

	@Override
	protected IContributionItem[] getContributionItems() {
		final ExtendedMarkersView view = getView();
		if (view == null) {
			return new IContributionItem[0];
		}

		Collection<MarkerFieldFilterGroup> groups = view.getAllFilters();

		if (groups.isEmpty()) {
			return new IContributionItem[0];
		}

		Iterator<MarkerFieldFilterGroup> groupsIterator = groups.iterator();
		IContributionItem[] items = new IContributionItem[groups.size() + 2];
		for (int i = 0; i < groups.size(); i++) {
			final MarkerFieldFilterGroup group = groupsIterator.next();
			items[i] = new ContributionItem() {

				@Override
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
				 * @return Listener
				 */
				private Listener getMenuItemListener(final MarkerFieldFilterGroup filter,
						final ExtendedMarkersView extendedView) {
					return event -> {
						if (extendedView != null)
							extendedView.toggleFilter(filter);
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

			@Override
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.CHECK);
				item.setText(MarkerMessages.MarkerFilter_showAllCommand_title);
				item.setSelection(noFiltersSelected());

				item.addListener(SWT.Selection, event -> {
					ExtendedMarkersView view = getView();
					if (view != null) {
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
				if (view == null) {
					return true;
				}

				Iterator<MarkerFieldFilterGroup> groupsIterator = view.getAllFilters().iterator();
				while (groupsIterator.hasNext()) {
					MarkerFieldFilterGroup group = groupsIterator.next();
					if (group.isEnabled()) {
						return false;
					}
				}
				return true;
			}

		};
	}

}
