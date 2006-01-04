package org.eclipse.ui.navigator.resources.internal.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.NewExampleAction;
import org.eclipse.ui.actions.NewProjectAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonActionProviderConfig;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardRegistry;

/**
 * Provides the new menu options for a context menu.
 * @since 3.2
 *
 */
public class NewActionProvider extends CommonActionProvider  {

	private static final CommonWizardRegistry COMMON_WIZARD_REGISTRY = CommonWizardRegistry
			.getInstance();

	private static final String FULL_EXAMPLES_WIZARD_CATEGORY = "org.eclipse.ui.Examples"; //$NON-NLS-1$

	private IAction showDlgAction;

	private IAction newProjectAction;

	private IAction newExampleAction;
 

	private WizardActionGroup newWizardActionGroup;

	public void init(CommonActionProviderConfig aConfig) {

		IWorkbenchWindow window = aConfig.getViewSite().getWorkbenchWindow();
		showDlgAction = ActionFactory.NEW.create(window);
		newProjectAction = new NewProjectAction(window);
		newExampleAction = new NewExampleAction(window);

		newWizardActionGroup = new WizardActionGroup(window,
				WizardActionGroup.NEW_WIZARD);

	} 

	public void fillContextMenu(IMenuManager aMenu) {
		addNewMenu(aMenu); 
	}

	/**
	 * @param menuManager
	 * @param selection
	 */
	private void addNewMenu(IMenuManager menuManager) {
		IMenuManager submenu = new MenuManager(
				WorkbenchNavigatorMessages.Workbench_new,
				ICommonMenuConstants.GROUP_NEW);

		// Add new project ..
		submenu.add(newProjectAction);
		submenu.add(new Separator());

		// fill the menu from the commonWizard contribution
		fillNewMenu(submenu);
		submenu.add(new Separator(ICommonMenuConstants.GROUP_ADDITIONS));

		if (hasExamples()) {
			// Add examples ..
			submenu.add(new Separator());
			submenu.add(newExampleAction);
		}

		// Add other ..
		submenu.add(new Separator());
		submenu.add(showDlgAction);

		menuManager.insertAfter(ICommonMenuConstants.GROUP_NEW, submenu);
	}

	/**
	 * Return whether or not any examples are in the current install.
	 * 
	 * @return boolean
	 */
	private boolean hasExamples() {
		IWizardRegistry newRegistry = PlatformUI.getWorkbench()
				.getNewWizardRegistry();
		IWizardCategory category = newRegistry
				.findCategory(FULL_EXAMPLES_WIZARD_CATEGORY);
		return category != null;

	}

	private void fillNewMenu(IMenuManager aSubmenu) {
		if (getContext() != null && !getContext().getSelection().isEmpty()
				&& getContext().getSelection() instanceof IStructuredSelection) {

			IStructuredSelection structuredSelection = (IStructuredSelection) getContext()
					.getSelection();
			if (structuredSelection.size() == 1)
				/* structuredSelection.size() = 1 */
				addCommomWizardNewMenus(aSubmenu, structuredSelection
						.getFirstElement());
		}
	}

	private void addCommomWizardNewMenus(IMenuManager aSubmenu, Object anElement) {
		String[] wizardDescriptorIds = COMMON_WIZARD_REGISTRY
				.getEnabledCommonWizardDescriptorIds(anElement,
						CommonWizardRegistry.WIZARD_TYPE_NEW);
		if (wizardDescriptorIds.length == 0)
			return;

		newWizardActionGroup.setWizardActionIds(wizardDescriptorIds);
		newWizardActionGroup.setContext(getContext());
		newWizardActionGroup.fillContextMenu(aSubmenu);

	}

}
