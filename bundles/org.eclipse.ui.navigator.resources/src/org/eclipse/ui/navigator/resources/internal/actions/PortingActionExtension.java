/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation 
 *  	Sebastian Davids <sdavids@gmx.de> - Collapse all action
 *      Sebastian Davids <sdavids@gmx.de> - Images for menu items
 *******************************************************************************/
package org.eclipse.ui.navigator.resources.internal.actions;

import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonActionProviderConfig;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorPlugin;

/**
 * The main action group for the navigator. This contains a few actions and
 * several subgroups.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class PortingActionExtension extends CommonActionProvider {
 
	private static final CommonWizardRegistry COMMON_WIZARD_REGISTRY = CommonWizardRegistry
			.getInstance();

	private static final String TYPE_IMPORT = "import"; //$NON-NLS-1$

	private static final String TYPE_EXPORT = "export"; //$NON-NLS-1$
 
	private static final String COMMON_NAVIGATOR_IMPORT_MENU = "common.import.menu"; //$NON-NLS-1$

	private static final String COMMON_NAVIGATOR_EXPORT_MENU = "common.export.menu"; //$NON-NLS-1$	
 

	private ImportResourcesAction importAction;

	private ExportResourcesAction exportAction;
  
	private INavigatorContentService contentService;

	private WizardActionGroup importWizardActionGroup;

	private WizardActionGroup exportWizardActionGroup;

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/full/"; //$NON-NLS-1$ 
		URL url = WorkbenchNavigatorPlugin.getDefault().find(
				new Path(iconPath + relativePath));
		if(url == null)
			return ImageDescriptor.getMissingImageDescriptor();
		return ImageDescriptor.createFromURL(url);
	}

	public void init(CommonActionProviderConfig aConfig) {
		contentService = aConfig.getContentService();
		IWorkbenchWindow window = (aConfig.getViewSite() != null) ? aConfig
				.getViewSite().getWorkbenchWindow() : null;
		importAction = new ImportResourcesAction(window);
		exportAction = new ExportResourcesAction(window);
		importWizardActionGroup = new WizardActionGroup(window,
				WizardActionGroup.IMPORT_WIZARD);
		exportWizardActionGroup = new WizardActionGroup(window,
				WizardActionGroup.EXPORT_WIZARD);

	}

	/**
	 * Extends the superclass implementation to dispose the subgroups.
	 */
	public void dispose() {
		// dispose
	}
 

	public void fillActionBars(IActionBars theActionBars) {
		theActionBars.setGlobalActionHandler(ActionFactory.IMPORT.getId(),
				importAction);
		theActionBars.setGlobalActionHandler(ActionFactory.EXPORT.getId(),
				exportAction);

	}

	public void fillContextMenu(IMenuManager aMenu) { 

		if (getContext() == null
				|| getContext().getSelection().isEmpty()
				|| !(getContext().getSelection() instanceof IStructuredSelection)) {
			addSimplePortingMenus(aMenu);
		} else {
			IStructuredSelection structuredSelection = (IStructuredSelection) getContext()
					.getSelection();
			if (structuredSelection.size() > 1)
				addSimplePortingMenus(aMenu);
			else
				/* structuredSelection.size() = 1 */
				addFocusedPortingMenus(aMenu, structuredSelection
						.getFirstElement());
		}
	}

	private void addSimplePortingMenus(IMenuManager aMenu) {
		aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, importAction);
		aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, exportAction);
	}

	private void addFocusedPortingMenus(IMenuManager aMenu, Object anElement) { 
		if (contentService != null) {
			addImportMenu(aMenu, anElement);
			addExportMenu(aMenu, anElement);
		} else
			addSimplePortingMenus(aMenu);
	}

	/**
	 * @param aMenu
	 * @param selection
	 */
	private void addImportMenu(IMenuManager aMenu, Object anElement) {

		String[] wizardDescriptorIds = COMMON_WIZARD_REGISTRY
				.getEnabledCommonWizardDescriptorIds(anElement, TYPE_IMPORT);

		if (wizardDescriptorIds.length == 0) {
			aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, importAction);
			return;
		}

		IMenuManager submenu = new MenuManager(
				WorkbenchNavigatorMessages.ImportResourcesAction_text,
				COMMON_NAVIGATOR_IMPORT_MENU);
		importWizardActionGroup.setWizardActionIds(wizardDescriptorIds);
		importWizardActionGroup.setContext(getContext());
		importWizardActionGroup.fillContextMenu(submenu);

		submenu.add(new Separator(ICommonMenuConstants.GROUP_ADDITIONS));
		submenu.add(new Separator());
		submenu.add(importAction);
		aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, submenu);
	}

	/**
	 * @param aMenu
	 * @param selection
	 */
	private void addExportMenu(IMenuManager aMenu, Object anElement) {
		String[] wizardDescriptorIds = COMMON_WIZARD_REGISTRY
				.getEnabledCommonWizardDescriptorIds(anElement, TYPE_EXPORT);
		if (wizardDescriptorIds.length == 0) {
			aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, exportAction);
			return;
		}
		IMenuManager submenu = new MenuManager(
				WorkbenchNavigatorMessages.ExportResourcesAction_text,
				COMMON_NAVIGATOR_EXPORT_MENU);
		exportWizardActionGroup.setWizardActionIds(wizardDescriptorIds);
		exportWizardActionGroup.setContext(getContext());
		exportWizardActionGroup.fillContextMenu(submenu);
		submenu.add(new Separator(ICommonMenuConstants.GROUP_ADDITIONS));
		submenu.add(new Separator());
		submenu.add(exportAction);
		aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, submenu);
	}
 
}
