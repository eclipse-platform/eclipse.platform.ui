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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.navigator.ICommonActionProvider;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.navigator.internal.actions.CommonActionProvider;
import org.eclipse.ui.navigator.resources.internal.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main action group for the navigator. This contains a few actions and several subgroups.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 */
public class PortingActionExtension extends CommonActionProvider implements ICommonActionProvider {

	// private static final NavigatorContentDescriptorRegistry CONTENT_DESCRIPTOR_REGISTRY =
	// NavigatorContentDescriptorRegistry.getInstance();
	private static final CommonWizardRegistry COMMON_WIZARD_REGISTRY = CommonWizardRegistry.getInstance();
	private static final String TYPE_IMPORT = "import"; //$NON-NLS-1$
	private static final String TYPE_EXPORT = "export"; //$NON-NLS-1$
	// private ImportActionGroup importActionGroup;
	public static final String COMMON_NAVIGATOR_IMPORT_MENU = "common.import.menu"; //$NON-NLS-1$
	public static final String COMMON_NAVIGATOR_EXPORT_MENU = "common.export.menu"; //$NON-NLS-1$	

	private static final Separator GROUP_IMPORT_SEPARATOR = new Separator(ICommonMenuConstants.GROUP_PORT);

	private ImportResourcesAction importAction;
	private ExportResourcesAction exportAction;
	private ActionContext actionContext;
	private INavigatorContentService contentService;

	private WizardActionGroup importWizardActionGroup;
	private WizardActionGroup exportWizardActionGroup;

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/full/"; //$NON-NLS-1$
		try {
			AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
			URL installURL = plugin.getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + relativePath);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

 
	public void init(String anExtensionId, IViewPart aViewPart, INavigatorContentService aContentService, StructuredViewer aStructuredViewer) {
		contentService = aContentService;
		IWorkbenchWindow window = (aViewPart.getViewSite() != null) ? aViewPart.getViewSite().getWorkbenchWindow() : null;
		importAction = new ImportResourcesAction(window);
		exportAction = new ExportResourcesAction(window);
		importWizardActionGroup = new WizardActionGroup(window, WizardActionGroup.IMPORT_WIZARD);
		exportWizardActionGroup = new WizardActionGroup(window, WizardActionGroup.EXPORT_WIZARD);

	}

	/**
	 * Extends the superclass implementation to dispose the subgroups.
	 */
	public void dispose() {
		//dispose
	}

 
	public void setActionContext(ActionContext aContext) {
		actionContext = aContext;
	}

 
	public boolean fillActionBars(IActionBars theActionBars) {
		theActionBars.setGlobalActionHandler(ActionFactory.IMPORT.getId(), importAction);
		theActionBars.setGlobalActionHandler(ActionFactory.EXPORT.getId(), exportAction);
		return true;
	}

 
	public boolean fillContextMenu(IMenuManager aMenu) {

		aMenu.appendToGroup(ICommonMenuConstants.GROUP_REORGANIZE, GROUP_IMPORT_SEPARATOR);

		if (actionContext == null || actionContext.getSelection().isEmpty() || !(actionContext.getSelection() instanceof IStructuredSelection)) {
			addSimplePortingMenus(aMenu);
		} else {
			IStructuredSelection structuredSelection = (IStructuredSelection) actionContext.getSelection();
			if (structuredSelection.size() > 1)
				addSimplePortingMenus(aMenu);
			else
				/* structuredSelection.size() = 1 */
				addFocusedPortingMenus(aMenu, structuredSelection.getFirstElement());
		}
		return true;
	}

	private void addSimplePortingMenus(IMenuManager aMenu) {
		aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, importAction);
		aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, exportAction);
	}

	private void addFocusedPortingMenus(IMenuManager aMenu, Object anElement) {
		// TODO MDE Add customizations
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

		String[] wizardDescriptorIds = COMMON_WIZARD_REGISTRY.getEnabledCommonWizardDescriptorIds(anElement, TYPE_IMPORT);

		if (wizardDescriptorIds.length == 0) {
			aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, importAction);
			return;
		}

		IMenuManager submenu = new MenuManager(WorkbenchNavigatorMessages.ImportResourcesAction_text, COMMON_NAVIGATOR_IMPORT_MENU);  
		importWizardActionGroup.setWizardActionIds(wizardDescriptorIds);
		importWizardActionGroup.setContext(actionContext);
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
		String[] wizardDescriptorIds = COMMON_WIZARD_REGISTRY.getEnabledCommonWizardDescriptorIds(anElement, TYPE_EXPORT);
		if (wizardDescriptorIds.length == 0) {
			aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, exportAction);
			return;
		}
		IMenuManager submenu = new MenuManager(WorkbenchNavigatorMessages.ExportResourcesAction_text, COMMON_NAVIGATOR_EXPORT_MENU);  
		exportWizardActionGroup.setWizardActionIds(wizardDescriptorIds);
		exportWizardActionGroup.setContext(actionContext);
		exportWizardActionGroup.fillContextMenu(submenu);
		submenu.add(new Separator(ICommonMenuConstants.GROUP_ADDITIONS));
		submenu.add(new Separator());
		submenu.add(exportAction);
		aMenu.appendToGroup(ICommonMenuConstants.GROUP_PORT, submenu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}


}
