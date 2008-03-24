/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;

/**
 * ContentsContribution is the class that defines the content selection
 * contribution for the {@link ExtendedMarkersView}.
 * 
 * @since 3.4
 * 
 */
public class ContentsContribution extends MarkersContribution {

	/**
	 * Create a new instance of the receiver.
	 */
	public ContentsContribution() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
	 */
	protected IContributionItem[] getContributionItems() {

		ExtendedMarkersView view = getView();
		String[] generatorIds = view.getGeneratorIds();

		Collection items = new ArrayList();
		for (int i = 0; i < generatorIds.length; i++) {
			final MarkerContentGenerator generator = MarkerSupportRegistry
					.getInstance().getGenerator(generatorIds[i]);
			
			if(generator == null){
				view.logInvalidGenerator(generatorIds[i]);
				continue;
			}
				
			
			items.add(new ContributionItem() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Menu,
				 *      int)
				 */
				public void fill(Menu menu, int index) {
					MenuItem item = new MenuItem(menu, SWT.RADIO);
					item.setText(generator.getName());
					ExtendedMarkersView view = getView();
					item.addListener(SWT.Selection, getMenuItemListener(
							generator, view));
					if (view != null && view.isShowing(generator))
						item.setSelection(true);
				}

				/**
				 * Create a menu listener for the generator and the view.
				 * 
				 * @param generator
				 * @param view
				 * @return Listener
				 */
				private Listener getMenuItemListener(
						final MarkerContentGenerator generator,
						final ExtendedMarkersView view) {
					return new Listener() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
						 */
						public void handleEvent(Event event) {
							if (view != null)
								view.setContentGenerator(generator);
						}
					};
				}
			});
		}

		items.add( new Separator());
		items.add(getFiltersDialogContribution());
		IContributionItem[] contributionItems = new IContributionItem[items.size()];
		items.toArray(contributionItems);
		return contributionItems;
	}

	/**
	 * Get the filter item for the contribution dialog.
	 * 
	 * @return ContributionItem
	 */
	private ContributionItem getFiltersDialogContribution() {
		return new ContributionItem() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Menu,
			 *      int)
			 */
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setText(MarkerMessages.configureFiltersCommand_title);
				item.addListener(SWT.Selection, new Listener() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
					 */
					public void handleEvent(Event event) {
						getView().openFiltersDialog();
					}
				});
			}

		};
	}
}
