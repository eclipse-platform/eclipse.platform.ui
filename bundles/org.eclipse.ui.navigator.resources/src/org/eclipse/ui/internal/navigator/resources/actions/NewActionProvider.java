/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.navigator.resources.actions;

import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.NewExampleAction;
import org.eclipse.ui.actions.NewProjectAction;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.navigator.WizardActionGroup;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;

/**
 * Provides the new (artifact creation) menu options for a context menu.
 *
 * <p>
 * The added submenu has the following structure
 * </p>
 *
 * <ul>
 * <li>a new generic project wizard shortcut action, </li>
 * <li>a separator, </li>
 * <li>a set of context sensitive wizard shortcuts (as defined by
 * <b>org.eclipse.ui.navigator.commonWizard</b>), </li>
 * <li>another separator, </li>
 * <li>a generic examples wizard shortcut action, and finally </li>
 * <li>a generic "Other" new wizard shortcut action</li>
 * </ul>
 *
 * @since 3.2
 */
public class NewActionProvider extends CommonActionProvider {

	private static final String FULL_EXAMPLES_WIZARD_CATEGORY = "org.eclipse.ui.Examples"; //$NON-NLS-1$

	private static final String NEW_MENU_NAME = "common.new.menu";//$NON-NLS-1$

	private static final Predicate<IWizardDescriptor> PROJECT_WIZARD_FILTER = wizardDescriptor -> Arrays
			.stream(wizardDescriptor.getTags()).anyMatch(WorkbenchWizardElement.TAG_PROJECT::equals);

	private ActionFactory.IWorkbenchAction showDlgAction;

	private IAction newProjectAction;

	private IAction newExampleAction;

	private WizardActionGroup newWizardActionGroup;

	private boolean contribute = false;

	private WizardActionGroup newProjectWizardActionGroup;

	@Override
	public void init(ICommonActionExtensionSite anExtensionSite) {

		if (anExtensionSite.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			IWorkbenchWindow window = ((ICommonViewerWorkbenchSite) anExtensionSite.getViewSite()).getWorkbenchWindow();
			showDlgAction = ActionFactory.NEW.create(window);
			newProjectAction = new NewProjectAction(window);
			newExampleAction = new NewExampleAction(window);
			newProjectWizardActionGroup = new WizardActionGroup(window,
					PlatformUI.getWorkbench().getNewWizardRegistry(), WizardActionGroup.TYPE_NEW,
					anExtensionSite.getContentService(), PROJECT_WIZARD_FILTER, false);
			newWizardActionGroup = new WizardActionGroup(window, PlatformUI.getWorkbench().getNewWizardRegistry(),
					WizardActionGroup.TYPE_NEW, anExtensionSite.getContentService(), PROJECT_WIZARD_FILTER.negate(),
					true);
			contribute = true;
		}
	}

	/**
	 * Adds a submenu to the given menu with the name "group.new" see
	 * {@link ICommonMenuConstants#GROUP_NEW}). The submenu contains the following structure:
	 *
	 * <ul>
	 * <li>a new generic project wizard shortcut action, </li>
	 * <li>a set of context sensitive wizard shortcuts (as defined by
	 * <b>org.eclipse.ui.navigator.commonWizard</b>) that are marked as project wizards, </li>
	 * <li>a separator, </li>
	 * <li>a set of context sensitive wizard shortcuts (as defined by
	 * <b>org.eclipse.ui.navigator.commonWizard</b>) that are not marked as project wizards, </li>
	 * <li>another separator, </li>
	 * <li>a generic examples wizard shortcut action, and finally </li>
	 * <li>a generic "Other" new wizard shortcut action</li>
	 * </ul>
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		IMenuManager submenu = new MenuManager(
				WorkbenchNavigatorMessages.NewActionProvider_NewMenu_label,
				NEW_MENU_NAME);
		if(!contribute) {
			return;
		}
		// Add new project wizard shortcut
		submenu.add(newProjectAction);
		// and all commonWizard contributions that are project wizards
		newProjectWizardActionGroup.setContext(getContext());
		newProjectWizardActionGroup.fillContextMenu(submenu);
		submenu.add(new Separator());

		// fill the menu from the commonWizard contributions
		newWizardActionGroup.setContext(getContext());
		newWizardActionGroup.fillContextMenu(submenu);

		submenu.add(new Separator(ICommonMenuConstants.GROUP_ADDITIONS));

		// if there are examples, then add them to the end of the menu
		if (hasExamples()) {
			submenu.add(new Separator());
			submenu.add(newExampleAction);
		}

		// Add other ..
		submenu.add(new Separator());
		submenu.add(showDlgAction);

		// append the submenu after the GROUP_NEW group.
		menu.insertAfter(ICommonMenuConstants.GROUP_NEW, submenu);
	}

	/**
	 * Return whether or not any examples are in the current install.
	 *
	 * @return True if there exists a full examples wizard category.
	 */
	private boolean hasExamples() {
		IWizardRegistry newRegistry = PlatformUI.getWorkbench().getNewWizardRegistry();
		IWizardCategory category = newRegistry.findCategory(FULL_EXAMPLES_WIZARD_CATEGORY);
		return category != null;

	}

	@Override
	public void dispose() {
		if (showDlgAction!=null) {
			showDlgAction.dispose();
			showDlgAction = null;
		}
		super.dispose();
	}
}
