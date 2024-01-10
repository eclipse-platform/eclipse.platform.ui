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

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * SortFieldContribution is the contribution that allows the user to choose
 * which field will become the primary sort field.
 *
 * @since 3.4
 */
public class SortFieldContribution extends MarkersContribution {

	/**
	 * Create a new instance of the receiver.
	 */
	public SortFieldContribution() {
		super();
	}

	public SortFieldContribution(String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		ExtendedMarkersView view = getView();
		if (view == null)
			return new IContributionItem[0];

		MarkerField[] fields = view.getVisibleFields();

		if (fields.length == 0)
			return new IContributionItem[0];

		IContributionItem[] items = new IContributionItem[fields.length + 2];

		for (int i = 0; i < fields.length; i++) {
			items[i] = getContributionItem(fields[i]);
		}

		items[fields.length] = new Separator();
		items[fields.length + 1] = getDirectionContribution();

		return items;

	}

	/**
	 * Return the ascending/descending contriution.
	 * @return IContributionItem
	 */
	private IContributionItem getDirectionContribution() {
		return new ContributionItem() {

			@Override
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.CHECK);
				item.setText(MarkerMessages.sortDirectionAscending_text);
				final ExtendedMarkersView view = getView();
				item.addListener(SWT.Selection, event -> {

					if (view != null)
						view.toggleSortDirection();
				});

				if (view != null)
					item.setSelection(view.getSortAscending());

			}

		};
	}

	/**
	 * Return the IContributionItem for field.
	 *
	 * @return IContributionItem
	 */
	private IContributionItem getContributionItem(final MarkerField field) {
		return new ContributionItem() {

			@Override
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.RADIO);
				String title = field.getName();
				item.setText(title);
				ExtendedMarkersView view = getView();
				item.addListener(SWT.Selection,
						getMenuItemListener(field, view));

				if (view != null)
					item.setSelection(view.isPrimarySortField(field));

			}

			/**
			 * Return the menu item listener for selection of a field.
			 *
			 * @return Listener
			 */
			private Listener getMenuItemListener(final MarkerField markerField,
					final ExtendedMarkersView view) {
				return event -> {

					MenuItem item = (MenuItem) event.widget;

					if (item.getSelection() && view != null)
						view.setPrimarySortField(markerField);
				};
			}
		};
	}

}
