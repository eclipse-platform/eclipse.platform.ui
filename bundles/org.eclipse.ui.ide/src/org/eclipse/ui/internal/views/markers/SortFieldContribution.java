/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.views.markers.MarkerField;

/**
 * SortFieldContribution is the contribution that allows the user to choose
 * which field will become the primary sort field.
 * 
 * @since 3.4
 * 
 */
public class SortFieldContribution extends MarkersContribution {

	/**
	 * Create a new instance of the receiver.
	 */
	public SortFieldContribution() {
		super();
	}

	/**
	 * @param id
	 */
	public SortFieldContribution(String id) {
		super(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
	 */
	protected IContributionItem[] getContributionItems() {

		MarkerField[] fields = getView().getBuilder().getVisibleFields();

		if (fields.length == 0)
			return new IContributionItem[0];

		IContributionItem[] items = new IContributionItem[fields.length];

		for (int i = 0; i < items.length; i++) {
			items[i] = getContributionItem(fields[i]);
		}

		return items;

	}

	/**
	 * Return the IContributionItem for field.
	 * 
	 * @param field
	 * @return IContributionItem
	 */
	private IContributionItem getContributionItem(final MarkerField field) {
		return new ContributionItem() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Menu,
			 *      int)
			 */
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.RADIO);
				String title = field.getColumnHeaderText();
				if (title.length() == 0)
					title = field.getColumnTooltipText();
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
			 * @param field
			 * @param view
			 * @return Listener
			 */
			private Listener getMenuItemListener(final MarkerField field,
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
							view.setPrimarySortField(field);
					}
				};
			}
		};
	}

}
