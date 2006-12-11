/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.services.ExpressionAuthority;

/**
 * <p>
 * A central authority for deciding visibility of menu elements. This authority
 * listens to a variety of incoming sources, and updates the underlying menu
 * manager if changes occur. It only updates those menu elements that are
 * showing, and listens to the menu manager to determine which menu elements are
 * currently visible.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
final public class MenuAuthority extends ExpressionAuthority {

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
	 * The window for which this authority applies. This is used for determining
	 * visibility of particular menu elements.
	 */
	private final Window window;

	/**
	 * Constructs a new instance of <code>MenuAuthority</code>.
	 * 
	 * @param window
	 *            The window to use when calling
	 *            {@link MenuElement#setVisible(Window, boolean)}; may be
	 *            <code>null</code>.
	 */
	public MenuAuthority(final Window window) {
		this.window = window;
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
		if (contribution.getMenuElement().isShowing(window)) {
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
		if (element.isShowing(window)) {
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
		oldSourceChanged(sourcePriority);
		newSourceChanged(sourcePriority);
	}

	/**
	 * @param sourcePriority
	 */
	private void oldSourceChanged(final int sourcePriority) {
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
			final boolean currentlyVisible = evaluate(contribution);
			contribution.clearResult();
			final boolean newVisible = evaluate(contribution);
			if (newVisible != currentlyVisible) {
				contribution.getMenuElement().setVisible(window, newVisible);
			}
		}
	}

	//
	// version 3.3 methods
	//

	private Map activationsByItem = new HashMap();

	private final Set[] activationsBySourcePriority = new Set[33];

	/**
	 * @param menuItem
	 *            the activation
	 */
	public void addContribution(final IMenuActivation menuItem) {
		IContributionItem item = menuItem.getContribution();
		Object o = activationsByItem.get(item);
		if (o != null) {
			if (o != menuItem) {
				// TODO log this error case
			}
			return;
		}
		activationsByItem.put(item, menuItem);
		item.setVisible(evaluate(menuItem));

		// Next we update the source priority bucket sort of activations.
		final int sourcePriority = menuItem.getSourcePriority();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				Set contributions = activationsBySourcePriority[i];
				if (contributions == null) {
					contributions = new HashSet(1);
					activationsBySourcePriority[i] = contributions;
				}
				contributions.add(menuItem);
			}
		}
	}

	/**
	 * @param menuItem
	 *            the activation
	 */
	public void removeContribution(final IMenuActivation menuItem) {
		Object o = activationsByItem.get(menuItem.getContribution());
		if (o != menuItem) {
			// TODO log this error case
			return;
		}
		activationsByItem.remove(menuItem.getContribution());

		final int sourcePriority = menuItem.getSourcePriority();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				final Set contributions = activationsBySourcePriority[i];
				if (contributions == null) {
					continue;
				}
				contributions.remove(menuItem);
				if (contributions.isEmpty()) {
					activationsBySourcePriority[i] = null;
				}
			}
		}
	}

	/**
	 * @param sourcePriority
	 */
	private void newSourceChanged(final int sourcePriority) {
		/*
		 * In this first phase, we cycle through all of the contributions that
		 * could have potentially changed. Each such contribution is added to a
		 * set for future processing. We add it to a set so that we avoid
		 * handling any individual contribution more than once.
		 */
		final Set contributionsToRecompute = new HashSet();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				final Collection contributions = activationsBySourcePriority[i];
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
			final IMenuActivation contribution = (IMenuActivation) contributionItr
					.next();
			final boolean currentlyVisible = evaluate(contribution);
			contribution.clearResult();
			final boolean newVisible = evaluate(contribution);
			if (newVisible != currentlyVisible) {
				IContributionItem menuItem = contribution.getContribution();
				menuItem.setVisible(newVisible);
				if (menuItem instanceof ContributionItem) {
					IContributionManager parent = ((ContributionItem) menuItem)
							.getParent();
					if (parent != null) {
						parent.markDirty();
					}
				}
			}
		}
	}

	/**
	 * This item will have its visibleWhen clause managed by this menu
	 * authority. The item lifecycle must be managed by the IMenuService that
	 * calls this method.
	 * 
	 * @param item
	 *            the item to manage. Must not be <code>null</code>. The item
	 *            must return the <code>setVisible(boolean)</code> value from
	 *            its <code>isVisible()</code> method.
	 * @param visibleWhen
	 *            The visibleWhen expression. Must not be <code>null</code>.
	 */
	public void addContribution(final IContributionItem item,
			final Expression visibleWhen) {
		if (item == null) {
			throw new IllegalArgumentException("item cannot be null"); //$NON-NLS-1$
		}
		if (visibleWhen == null) {
			throw new IllegalArgumentException(
					"visibleWhen expression cannot be null"); //$NON-NLS-1$
		}
		Object obj = activationsByItem.get(item);
		if (obj != null) {
			String id = item.getId();
			WorkbenchPlugin.log("item is already registered: " //$NON-NLS-1$
					+ (id == null ? "no id" : id)); //$NON-NLS-1$
			return;
		}
		MenuActivation activation = new MenuActivation(item, visibleWhen);
		activationsByItem.put(item, activation);
		item.setVisible(evaluate(activation));
		// Next we update the source priority bucket sort of activations.
		final int sourcePriority = activation.getSourcePriority();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				Set contributions = activationsBySourcePriority[i];
				if (contributions == null) {
					contributions = new HashSet(1);
					activationsBySourcePriority[i] = contributions;
				}
				contributions.add(activation);
			}
		}
	}

	/**
	 * Remove this item from having its visibleWhen clause managed by this menu
	 * authority. This method does nothing if the item is not managed by this
	 * menu authority.
	 * 
	 * @param item
	 *            the item to remove. Must not be <code>null</code>.
	 */
	public void removeContribition(final IContributionItem item) {
		if (item == null) {
			throw new IllegalArgumentException("item cannot be null"); //$NON-NLS-1$
		}

		Object obj = activationsByItem.remove(item);
		if (obj == null) {
			// it's a no-op to remove an unmanaged item
			return;
		}
		MenuActivation activation = (MenuActivation) obj;
		final int sourcePriority = activation.getSourcePriority();
		for (int i = 1; i <= 32; i++) {
			if ((sourcePriority & (1 << i)) != 0) {
				final Set contributions = activationsBySourcePriority[i];
				if (contributions == null) {
					continue;
				}
				contributions.remove(activation);
				if (contributions.isEmpty()) {
					activationsBySourcePriority[i] = null;
				}
			}
		}
	}
}
