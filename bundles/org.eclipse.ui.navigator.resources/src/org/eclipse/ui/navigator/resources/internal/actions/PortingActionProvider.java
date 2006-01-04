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
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonActionProviderConfig;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.WizardActionGroup;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorPlugin;

/**
 * Adds actions for Import/Export wizards. The group is smart, in that it will
 * either add actions for Import and Export, or if there are context sensitive
 * options available (as defined by <b>org.eclipse.ui.navigator.commonWizard</b>),
 * then it will compound these options into a submenu with the appropriate lead
 * text ("Import" or "Export").
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
public class PortingActionProvider extends CommonActionProvider {

	private static final String COMMON_NAVIGATOR_IMPORT_MENU = "common.import.menu"; //$NON-NLS-1$

	private static final String COMMON_NAVIGATOR_EXPORT_MENU = "common.export.menu"; //$NON-NLS-1$	

	private ImportResourcesAction importAction;

	private ExportResourcesAction exportAction;

	private WizardActionGroup importWizardActionGroup;

	private WizardActionGroup exportWizardActionGroup;

	private boolean disposed = false;

	public void init(CommonActionProviderConfig aConfig) {

		Assert.isTrue(!disposed);
		
		IWorkbenchWindow window = (aConfig.getViewSite() != null) ? aConfig
				.getViewSite().getWorkbenchWindow() : null;
		importAction = new ImportResourcesAction(window);
		exportAction = new ExportResourcesAction(window);
		importWizardActionGroup = new WizardActionGroup(window, PlatformUI
				.getWorkbench().getImportWizardRegistry(), WizardActionGroup.TYPE_IMPORT);
		exportWizardActionGroup = new WizardActionGroup(window, PlatformUI
				.getWorkbench().getExportWizardRegistry(), WizardActionGroup.TYPE_EXPORT);

	}

	/**
	 * Extends the superclass implementation to dispose the subgroups.
	 */
	public void dispose() {
		importWizardActionGroup.dispose();
		exportWizardActionGroup.dispose();
		importAction = null;
		exportAction = null;
		disposed = true;
	}

	public void fillActionBars(IActionBars theActionBars) {
	
		Assert.isTrue(!disposed);
		
		theActionBars.setGlobalActionHandler(ActionFactory.IMPORT.getId(),
				importAction);
		theActionBars.setGlobalActionHandler(ActionFactory.EXPORT.getId(),
				exportAction);
	
	}

	public void fillContextMenu(IMenuManager aMenu) {
	
		Assert.isTrue(!disposed);
		
		ISelection selection = getContext().getSelection();
		if (selection.isEmpty() || !(selection instanceof IStructuredSelection))
			addSimplePortingMenus(aMenu);
		else if (((IStructuredSelection) selection).size() > 1)
			addSimplePortingMenus(aMenu);
		else {
			addImportMenu(aMenu);
			addExportMenu(aMenu);
		}
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/full/"; //$NON-NLS-1$ 
		URL url = WorkbenchNavigatorPlugin.getDefault().find(
				new Path(iconPath + relativePath));
		if (url == null)
			return ImageDescriptor.getMissingImageDescriptor();
		return ImageDescriptor.createFromURL(url);
	}

	private void addSimplePortingMenus(IMenuManager aMenu) {
		aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, importAction);
		aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, exportAction);
	}

	private void addImportMenu(IMenuManager aMenu) {

		importWizardActionGroup.setContext(getContext());
		if (importWizardActionGroup.getWizardActionIds().length == 0) {
			aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, importAction);
			return;
		}

		IMenuManager submenu = new MenuManager(
				WorkbenchNavigatorMessages.ImportResourcesMenu_text,
				COMMON_NAVIGATOR_IMPORT_MENU);
		importWizardActionGroup.fillContextMenu(submenu);

		submenu.add(new Separator(ICommonMenuConstants.GROUP_ADDITIONS));
		submenu.add(new Separator());
		submenu.add(importAction);
		aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, submenu);
	}

	private void addExportMenu(IMenuManager aMenu) {

		exportWizardActionGroup.setContext(getContext());
		if (importWizardActionGroup.getWizardActionIds().length == 0) {
			aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, exportAction);
			return;
		}
		IMenuManager submenu = new MenuManager(
				WorkbenchNavigatorMessages.ExportResourcesMenu_text,
				COMMON_NAVIGATOR_EXPORT_MENU);
		exportWizardActionGroup.fillContextMenu(submenu);

		submenu.add(new Separator(ICommonMenuConstants.GROUP_ADDITIONS));
		submenu.add(new Separator());
		submenu.add(exportAction);
		aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, submenu);
	}

}
