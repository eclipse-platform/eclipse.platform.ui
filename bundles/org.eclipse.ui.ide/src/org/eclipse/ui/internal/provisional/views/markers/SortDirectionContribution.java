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

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * SortDirectionContribution is the dynamic contribution for sort direction.
 * 
 * @since 3.4
 * 
 */
public class SortDirectionContribution extends MarkersContribution {

	/**
	 * Create a new instance of the receiver.
	 */
	public SortDirectionContribution() {
		super();
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param id
	 */
	public SortDirectionContribution(String id) {
		super(id);

	}

	protected IContributionItem[] getContributionItems() {
		IContributionItem[] items = new IContributionItem[1];

		items[0] = new ContributionItem() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Menu,
			 *      int)
			 */
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.RADIO);
				item.setText(MarkerMessages.sortDirectionAscending_text);
				final ExtendedMarkersView view = getView();
				item.addListener(SWT.Selection, new Listener() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
					 */
					public void handleEvent(Event event) {

						if (view != null)
							view.toggleSortDirection();
					}
				});

				if (view != null)
					item.setSelection(view.getSortAscending());

			}

		};

		return items;
	}
}
