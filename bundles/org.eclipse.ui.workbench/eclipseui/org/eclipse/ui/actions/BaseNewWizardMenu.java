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
 *     Andreas Buchen <andreas.buchen@sap.com> - Bug 206584
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548428
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * A <code>BaseNewWizardMenu</code> is used to populate a menu manager with New
 * Wizard actions for the current perspective's new wizard shortcuts, including
 * an Other... action to open the new wizard dialog.
 * <p>
 * <strong>Note:</strong> Clients must dispose this menu when it is no longer
 * required.
 * </p>
 *
 * @since 3.1
 */
public class BaseNewWizardMenu extends CompoundContributionItem {
	/*
	 * @issue Should be possible to implement this class entirely using public API.
	 * Cases to be fixed: WorkbenchWindow, NewWizardShortcutAction Suggestions: -
	 * add the ability to update the submenus of a window
	 */

	private final Map<String, IAction> actions = new HashMap<>(21);

	private final IExtensionChangeHandler configListener = new IExtensionChangeHandler() {

		@Override
		public void removeExtension(IExtension source, Object[] objects) {
			for (Object object : objects) {
				if (object instanceof NewWizardShortcutAction) {
					actions.values().remove(object);
				}
			}
		}

		@Override
		public void addExtension(IExtensionTracker tracker, IExtension extension) {
			// Do nothing
		}
	};

	/**
	 * TODO: should this be done with an addition listener?
	 */
	private final IRegistryChangeListener registryListener = event -> {
		// reset the reader.
		// TODO This is expensive. Can we be more selective?
		if (getParent() != null) {
			getParent().markDirty();
		}
	};

	private ActionFactory.IWorkbenchAction showDlgAction;

	private IWorkbenchWindow workbenchWindow;

	/**
	 * Creates a new wizard shortcut menu for the IDE.
	 * <p>
	 * <strong>Note:</strong> Clients must dispose this menu when it is no longer
	 * required.
	 * </p>
	 *
	 * @param window the window containing the menu
	 * @param id     the contribution item identifier, or <code>null</code>
	 */
	public BaseNewWizardMenu(IWorkbenchWindow window, String id) {
		super(id);
		Assert.isNotNull(window);
		this.workbenchWindow = window;
		showDlgAction = ActionFactory.NEW.create(window);
		registerListeners();
		// indicate that a new wizards submenu has been created
		if (window instanceof WorkbenchWindow) {
			((WorkbenchWindow) window).addSubmenu(WorkbenchWindow.NEW_WIZARD_SUBMENU);
		}
	}

	/**
	 * Adds the contribution items to show to the given list.
	 *
	 * @param list the list to add contribution items to
	 */
	protected void addItems(List<IContributionItem> list) {
		if (addShortcuts(list)) {
			list.add(new Separator());
		}
		list.add(new ActionContributionItem(getShowDialogAction()));
	}

	/**
	 * Adds the new wizard shortcuts for the current perspective to the given list.
	 *
	 * @param list the list to add contribution items to
	 * @return <code>true</code> if any items were added, <code>false</code> if none
	 *         were added
	 */
	protected boolean addShortcuts(List<IContributionItem> list) {
		boolean added = false;
		IWorkbenchPage page = workbenchWindow.getActivePage();
		if (page != null) {
			String[] wizardIds = page.getNewWizardShortcuts();
			for (String wizardId : wizardIds) {
				IAction action = getAction(wizardId);
				if (action != null) {
					if (!WorkbenchActivityHelper.filterItem(action)) {
						list.add(new ActionContributionItem(action));
						added = true;
					}
				}
			}
		}
		return added;
	}

	@Override
	public void dispose() {
		if (workbenchWindow != null) {
			unregisterListeners();
			showDlgAction.dispose();
			showDlgAction = null;
			workbenchWindow = null;
			super.dispose();
		}
	}

	/*
	 * Returns the action for the given wizard id, or null if not found.
	 */
	private IAction getAction(String id) {
		// Keep a cache, rather than creating a new action each time,
		// so that image caching in ActionContributionItem works.
		IAction action = actions.get(id);
		if (action == null) {
			IWizardDescriptor wizardDesc = WorkbenchPlugin.getDefault().getNewWizardRegistry().findWizard(id);
			if (wizardDesc != null) {
				action = new NewWizardShortcutAction(workbenchWindow, wizardDesc);
				actions.put(id, action);
				IConfigurationElement element = Adapters.adapt(wizardDesc, IConfigurationElement.class);
				if (element != null) {
					workbenchWindow.getExtensionTracker().registerObject(element.getDeclaringExtension(), action,
							IExtensionTracker.REF_WEAK);
				}
			}
		}
		return action;
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		List<IContributionItem> list = new ArrayList<>();
		if (workbenchWindow != null && workbenchWindow.getActivePage() != null
				&& workbenchWindow.getActivePage().getPerspective() != null) {
			addItems(list);
		} else {
			String text = WorkbenchMessages.Workbench_noApplicableItems;
			Action dummyAction = new Action(text) {
				// dummy inner class; no methods
			};
			dummyAction.setEnabled(false);
			list.add(new ActionContributionItem(dummyAction));
		}
		return list.toArray(new IContributionItem[list.size()]);
	}

	/**
	 * Returns the "Other..." action, used to show the new wizards dialog.
	 *
	 * @return the action used to show the new wizards dialog
	 */
	protected IAction getShowDialogAction() {
		return showDlgAction;
	}

	/**
	 * Returns the window in which this menu appears.
	 *
	 * @return the window in which this menu appears
	 */
	protected IWorkbenchWindow getWindow() {
		return workbenchWindow;
	}

	/**
	 * Registers listeners.
	 *
	 * @since 3.1
	 */
	private void registerListeners() {
		Platform.getExtensionRegistry().addRegistryChangeListener(registryListener);
		workbenchWindow.getExtensionTracker().registerHandler(configListener, null);
	}

	/**
	 * Returns whether the new wizards registry has a non-empty category with the
	 * given identifier.
	 *
	 * @param categoryId the identifier for the category
	 * @return <code>true</code> if there is a non-empty category with the given
	 *         identifier, <code>false</code> otherwise
	 */
	protected boolean registryHasCategory(String categoryId) {
		return WorkbenchPlugin.getDefault().getNewWizardRegistry().findCategory(categoryId) != null;
	}

	/**
	 * Unregisters listeners.
	 *
	 * @since 3.1
	 */
	private void unregisterListeners() {
		Platform.getExtensionRegistry().removeRegistryChangeListener(registryListener);
		IExtensionTracker extensionTracker = workbenchWindow.getExtensionTracker();
		if (extensionTracker != null) {
			extensionTracker.unregisterHandler(configListener);
		}
	}
}
