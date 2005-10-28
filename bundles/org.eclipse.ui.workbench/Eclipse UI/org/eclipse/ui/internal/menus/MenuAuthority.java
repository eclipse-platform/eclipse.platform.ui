/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.menus.MenuElement;
import org.eclipse.ui.internal.sources.ExpressionAuthority;
import org.eclipse.ui.menus.IMenuContribution;

/**
 * <p>
 * A central authority for deciding visibility of menu elements. This authority
 * listens to a variety of incoming sources, and updates the underlying menu
 * manager if changes occur. It only updates those menu elements that are
 * showing, and listens to the menu manager to determine which menu elements are
 * currently visible.
 * </p>
 * 
 * @since 3.2
 */
final class MenuAuthority extends ExpressionAuthority {

	/**
	 * This is a map of menu element contributions (<code>Collection</code>
	 * of <code>IMenuContribution</code>) sorted by identifier (<code>MenuElement</code>).
	 * If there is only one contribution for a menu element, then the
	 * <code>Collection</code> is replaced by a <code>IMenuContribution</code>.
	 * If there is no contribution, the entry should be removed entirely.
	 */
	private final Map menuContributionsByElement = new HashMap();

	/**
	 * A bucket sort of the contributed menu elements based on source priority.
	 * This only includes the items that are currently showing within the
	 * workbench. Each contribution will appear only once per set, but may
	 * appear in multiple sets. If no contributions are defined for a particular
	 * priority level, then the array at that index will only contain
	 * <code>null</code>.
	 */
	private final Set[] showingContributionsBySourcePriority = new Set[33];

	/**
	 * Constructs a new instance of <code>MenuAuthority</code>.
	 */
	MenuAuthority() {
	}

	/**
	 * Contributes a menu element to the workbench. This will add it to a master
	 * list.
	 * 
	 * @param contribution
	 *            The contribution; must not be <code>null</code>.
	 */
	final void contributeMenu(final IMenuContribution contribution) {
		// First we update the menuContributionsByElement map.
		final MenuElement element = contribution.getMenuElement();
		final Object value = menuContributionsByElement.get(element);
		if (value instanceof Collection) {
			final Collection menuContributions = (Collection) value;
			if (!menuContributions.contains(contribution)) {
				menuContributions.add(contribution);
			}
		} else if (value instanceof IMenuContribution) {
			if (value != contribution) {
				final Collection menuContributions = new ArrayList(2);
				menuContributions.add(value);
				menuContributions.add(contribution);
				menuContributionsByElement.put(element, menuContributions);
			}
		} else {
			menuContributionsByElement.put(element, contribution);
		}

		// Next we update the source priority bucket sort of activations.
		if (contribution.getMenuElement().isShowing()) {
			final int sourcePriority = contribution.getSourcePriority();
			for (int i = 1; i <= 32; i++) {
				if ((sourcePriority & (1 << i)) != 0) {
					Set contributions = showingContributionsBySourcePriority[i];
					if (contributions == null) {
						contributions = new HashSet(1);
						showingContributionsBySourcePriority[i] = contributions;
					}
					contributions.add(contribution);
				}
			}
		}
	}

	/**
	 * Removes a contribution from the workbench. This will remove it from the
	 * master list, and update as appropriate.
	 * 
	 * @param contribution
	 *            The contribution; must not be <code>null</code>.
	 */
	final void removeContribution(final IMenuContribution contribution) {
		// First we update the menuContributionByElement map.
		final MenuElement element = contribution.getMenuElement();
		final Object value = menuContributionsByElement.get(element);
		if (value instanceof Collection) {
			final Collection menuContributions = (Collection) value;
			if (menuContributions.contains(contribution)) {
				menuContributions.remove(contribution);
				if (menuContributions.isEmpty()) {
					menuContributionsByElement.remove(element);

				} else if (menuContributions.size() == 1) {
					final IMenuContribution remainingContribution = (IMenuContribution) menuContributions
							.iterator().next();
					menuContributionsByElement.put(element,
							remainingContribution);

				}
			}
		} else if (value instanceof IMenuContribution) {
			if (value == contribution) {
				menuContributionsByElement.remove(element);
			}
		}

		// Next we update the source priority bucket sort of activations.
		if (element.isShowing()) {
			final int sourcePriority = contribution.getSourcePriority();
			for (int i = 1; i <= 32; i++) {
				if ((sourcePriority & (1 << i)) != 0) {
					final Set contributions = showingContributionsBySourcePriority[i];
					if (contributions == null) {
						continue;
					}
					contributions.remove(contribution);
					if (contributions.isEmpty()) {
						showingContributionsBySourcePriority[i] = null;
					}
				}
			}
		}
	}

	/**
	 * Carries out the actual source change notification. It assumed that by the
	 * time this method is called, <code>getEvaluationContext()</code> is
	 * up-to-date with the current state of the application.
	 * 
	 * @param sourcePriority
	 *            A bit mask of all the source priorities that have changed.
	 */
	protected final void sourceChanged(final int sourcePriority) {
		/*
		 * In this first phase, we cycle through all of the contributions that
		 * could have potentially changed. Each such contribution is added to a
		 * set for future processing. We add it to a set so that we avoid
		 * handling any individual contribution more than once.
		 */
		final Set contributionsToRecompute = new HashSet();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				final Collection contributions = showingContributionsBySourcePriority[i];
				if (contributions != null) {
					final Iterator contributionItr = contributions.iterator();
					while (contributionItr.hasNext()) {
						contributionsToRecompute.add(contributionItr.next());
					}
				}
			}
		}

		/*
		 * For every contribution, we recompute its state, and check whether it
		 * has changed. If it has changed, then we take note of the menu element
		 * so we can update the menu element later.
		 */
		final Iterator contributionItr = contributionsToRecompute.iterator();
		while (contributionItr.hasNext()) {
			final IMenuContribution contribution = (IMenuContribution) contributionItr
					.next();
			final boolean currentlyVisible = contribution
					.evaluate(getEvaluationContext());
			contribution.clearResult();
			final boolean newVisible = contribution
					.evaluate(getEvaluationContext());
			if (newVisible != currentlyVisible) {
				contribution.getMenuElement().setVisible(newVisible);
			}
		}
	}
}
