/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548428
 *     Dinesh Palanisamy (ETAS GmbH) - Issue 1530
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.internal.dialogs.DynamicMenuSelection;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * A <code>NewWizardMenu</code> augments <code>BaseNewWizardMenu</code> with IDE-specific
 * actions: New Project... (always shown) and New Example... (shown only if there are example wizards installed).
 * <p>
 * <strong>Note:</strong> Clients must dispose this menu when it is no longer required.
 * </p>
 */
public class NewWizardMenu extends BaseNewWizardMenu {

	private IAction newExampleAction;
	private IAction newProjectAction;

	private boolean enabled = true;

	/**
	 * Creates a new wizard shortcut menu for the IDE.
	 * <p>
	 * <strong>Note:</strong> Clients must dispose this menu when it is no longer required.
	 * </p>
	 *
	 * @param window
	 *            the window containing the menu
	 */
	public NewWizardMenu(IWorkbenchWindow window) {
		this(window, null);

	}

	/**
	 * Creates a new wizard shortcut menu for the IDE.
	 * <p>
	 * <strong>Note:</strong> Clients must dispose this menu when it is no longer required.
	 * </p>
	 *
	 * @param window
	 *            the window containing the menu
	 * @param id
	 *            the identifier for this contribution item
	 */
	public NewWizardMenu(IWorkbenchWindow window, String id) {
		super(window, id);
		newExampleAction = new NewExampleAction(window);
		newProjectAction = new NewProjectAction(window);
	}

	/**
	 * Create a new wizard shortcut menu.
	 * <p>
	 * If the menu will appear on a semi-permanent basis, for instance within
	 * a toolbar or menubar, the value passed for <code>register</code> should be true.
	 * If set, the menu will listen to perspective activation and update itself
	 * to suit.  In this case clients are expected to call <code>deregister</code>
	 * when the menu is no longer needed.  This will unhook any perspective
	 * listeners.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> Clients must dispose this menu when it is no longer required.
	 * </p>
	 *
	 * @param innerMgr the location for the shortcut menu contents
	 * @param window the window containing the menu
	 * @param register if <code>true</code> the menu listens to perspective changes in
	 *      the window
	 * @deprecated use NewWizardMenu(IWorkbenchWindow) instead
	 */
	@Deprecated
	public NewWizardMenu(IMenuManager innerMgr, IWorkbenchWindow window,
			boolean register) {
		this(window, null);
		fillMenu(innerMgr);
		// Must be done after constructor to ensure field initialization.
	}

	private void fillMenu(IContributionManager innerMgr) {
		// Remove all.
		innerMgr.removeAll();

		for (IContributionItem item : getContributionItems()) {
			innerMgr.add(item);
		}
	}

	/**
	 * Removes all listeners from the containing workbench window.
	 * <p>
	 * This method should only be called if the shortcut menu is created with
	 * <code>register = true</code>.
	 * </p>
	 *
	 * @deprecated has no effect
	 */
	@Deprecated
	public void deregisterListeners() {
		// do nothing
	}

	/**
	 * Return whether or not any examples are in the current install.
	 *
	 * @return boolean
	 */
	private boolean hasExamples() {
		boolean hasCategory = registryHasCategory(WizardsRegistryReader.FULL_EXAMPLES_WIZARD_CATEGORY);
		if (hasCategory) {
			IWizardCategory exampleCategory = WorkbenchPlugin
					.getDefault()
					.getNewWizardRegistry()
					.findCategory(
							WizardsRegistryReader.FULL_EXAMPLES_WIZARD_CATEGORY);
			return hasWizards(exampleCategory);
		}
		return false;
	}

	private boolean hasWizards(IWizardCategory category) {
		IWizardDescriptor[] wizards = category.getWizards();
		if (wizards.length>0) {
			for (IWizardDescriptor wizard : wizards) {
				if (!WorkbenchActivityHelper.filterItem(wizard)) {
					return true;
				}
			}
		}
		for (IWizardCategory wizardCategory : category.getCategories()) {
			if (hasWizards(wizardCategory)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void addItems(List<IContributionItem> list) {
		List<IContributionItem> shortCuts = new ArrayList<>();
		addShortcuts(shortCuts);

		for (Iterator<IContributionItem> iterator = shortCuts.iterator(); iterator.hasNext();) {
			IContributionItem curr = iterator.next();
			if (curr instanceof ActionContributionItem && isNewProjectWizardAction(((ActionContributionItem) curr).getAction())) {
				iterator.remove();
				list.add(curr);
			}
		}
		list.add(new ActionContributionItem(newProjectAction));
		list.add(new Separator());
		if (!shortCuts.isEmpty()) {
			list.addAll(shortCuts);
			list.add(new Separator());
		}
		if (hasExamples()) {
			list.add(new ActionContributionItem(newExampleAction));
			list.add(new Separator());
		}

		// To add shortcuts from OTHER... wizard regardless of perspective
		Collection<IContributionItem> otherItems = new LinkedList<>();
		if (!DynamicMenuSelection.getInstance().getSelectedFromOther().isEmpty()) {
			for (String selectedItemsFormOthers : DynamicMenuSelection.getInstance().getSelectedFromOther()) {
				IAction action = getAction(selectedItemsFormOthers);
				otherItems.add(new ActionContributionItem(action));
			}
			dynamicMenuCheck(list, otherItems);
		}
		list.add(new ActionContributionItem(getShowDialogAction()));
	}

	private void dynamicMenuCheck(List<IContributionItem> list, Collection<IContributionItem> otherItems) {
		Set<IContributionItem> existingShortcutsInPerspective = new HashSet<>(list);
		for (IContributionItem item : otherItems) {
			if (!existingShortcutsInPerspective.contains(item)) {
				list.add(item);
				existingShortcutsInPerspective.add(item);
			}
		}
	}

	private boolean isNewProjectWizardAction(IAction action) {
		if (action instanceof NewWizardShortcutAction) {
			IWizardDescriptor wizardDescriptor= ((NewWizardShortcutAction) action).getWizardDescriptor();
			String [] tags = wizardDescriptor.getTags();
			for (String tag : tags) {
				if (WorkbenchWizardElement.TAG_PROJECT.equals(tag)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled state of the receiver.
	 *
	 * @param enabledValue if <code>true</code> the menu is enabled; else
	 * 		it is disabled
	 */
	public void setEnabled(boolean enabledValue) {
		this.enabled = enabledValue;
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		if (isEnabled()) {
			return super.getContributionItems();
		}
		return new IContributionItem[0];
	}

	@Override
	public void dispose() {
		newExampleAction = null;
		newProjectAction = null;
		super.dispose();
	}
}
