/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 221662 [Contributions] Extension point org.eclipse.ui.menus: sub menu contribution does not have icon even if specified
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.Map;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.swt.modeling.MenuServiceFilter;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.3
 * 
 */
public class MenuAdditionCacheEntry {

	private MApplication application;
	// private IEclipseContext appContext;
	private IConfigurationElement configElement;
	private MenuLocationURI location;
	private MMenuContribution menuContribution;

	private MToolBarContribution toolBarContribution;

	// private String namespaceIdentifier;

	public MenuAdditionCacheEntry(MApplication application, IEclipseContext appContext,
			IConfigurationElement configElement, String attribute, String namespaceIdentifier) {
		this.application = application;
		// this.appContext = appContext;
		this.configElement = configElement;
		this.location = new MenuLocationURI(attribute);
		// this.namespaceIdentifier = namespaceIdentifier;
	}

	/**
	 * @return <code>true</code> if this is a toolbar contribution
	 */
	private boolean inToolbar() {
		return location.getScheme().startsWith("toolbar"); //$NON-NLS-1$
	}

	/**
	 * @param configurationElement
	 * @return the menu manager
	 */
	private MMenu createMenuAddition(final IConfigurationElement menuAddition) {
		// Is this for a menu or a ToolBar ? We can't create
		// a menu directly under a Toolbar; we have to add an
		// item of style 'pulldown'
		if (inToolbar()) {
			return null;
		}

		return MenuHelper.createMenuAddition(menuAddition);
	}

	private MMenuElement createSeparatorAddition(final IConfigurationElement sepAddition) {
		String name = MenuHelper.getName(sepAddition);
		MMenuElement element = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
		element.setElementId(name);
		if (!MenuHelper.isSeparatorVisible(sepAddition)) {
			element.setVisible(false);
		}
		return element;
	}

	private MMenuElement createMenuCommandAddition(final IConfigurationElement commandAddition) {
		MHandledMenuItem item = MenuFactoryImpl.eINSTANCE.createHandledMenuItem();
		item.setElementId(MenuHelper.getId(commandAddition));
		item.setCommand(MenuHelper.getCommandById(application,
				MenuHelper.getCommandId(commandAddition)));
		Map parms = MenuHelper.getParameters(commandAddition);
		for (Object obj : parms.entrySet()) {
			Map.Entry e = (Map.Entry) obj;
			MParameter parm = CommandsFactoryImpl.eINSTANCE.createParameter();
			parm.setName(e.getKey().toString());
			parm.setValue(e.getValue().toString());
			item.getParameters().add(parm);
		}
		item.setIconURI(MenuHelper
				.getIconUrl(commandAddition, IWorkbenchRegistryConstants.ATT_ICON));
		item.setLabel(MenuHelper.getLabel(commandAddition));
		item.setMnemonics(MenuHelper.getMnemonic(commandAddition));
		item.setTooltip(MenuHelper.getTooltip(commandAddition));
		item.setType(MenuHelper.getStyle(commandAddition));
		item.setVisibleWhen(MenuHelper.getVisibleWhen(commandAddition));
		return item;
	}

	private MToolBarElement createToolBarCommandAddition(final IConfigurationElement commandAddition) {
		MHandledToolItem item = MenuFactoryImpl.eINSTANCE.createHandledToolItem();
		item.setElementId(MenuHelper.getId(commandAddition));
		item.setCommand(MenuHelper.getCommandById(application,
				MenuHelper.getCommandId(commandAddition)));
		Map parms = MenuHelper.getParameters(commandAddition);
		for (Object obj : parms.entrySet()) {
			Map.Entry e = (Map.Entry) obj;
			MParameter parm = CommandsFactoryImpl.eINSTANCE.createParameter();
			parm.setName(e.getKey().toString());
			parm.setValue(e.getValue().toString());
			item.getParameters().add(parm);
		}
		String iconUrl = MenuHelper.getIconUrl(commandAddition,
				IWorkbenchRegistryConstants.ATT_ICON);
		if (iconUrl == null) {
			item.setLabel(MenuHelper.getLabel(commandAddition));
		} else {
			item.setIconURI(iconUrl);
		}
		item.setTooltip(MenuHelper.getTooltip(commandAddition));
		item.setType(MenuHelper.getStyle(commandAddition));
		item.setVisibleWhen(MenuHelper.getVisibleWhen(commandAddition));
		return item;
	}

	private void addMenuChildren(final MElementContainer<MMenuElement> container,
			IConfigurationElement parent, String filter) {
		IConfigurationElement[] items = parent.getChildren();
		for (int i = 0; i < items.length; i++) {
			String itemType = items[i].getName();
			String id = MenuHelper.getId(items[i]);

			if (IWorkbenchRegistryConstants.TAG_COMMAND.equals(itemType)) {
				MMenuElement element = createMenuCommandAddition(items[i]);
				container.getChildren().add(element);
			} else if (IWorkbenchRegistryConstants.TAG_DYNAMIC.equals(itemType)) {
				E4Util.unsupported("Dynamic: " + id + " in " + location); //$NON-NLS-1$//$NON-NLS-2$
			} else if (IWorkbenchRegistryConstants.TAG_CONTROL.equals(itemType)) {
				E4Util.unsupported("Control: " + id + " in " + location); //$NON-NLS-1$//$NON-NLS-2$
			} else if (IWorkbenchRegistryConstants.TAG_SEPARATOR.equals(itemType)) {
				MMenuElement element = createSeparatorAddition(items[i]);
				container.getChildren().add(element);
			} else if (IWorkbenchRegistryConstants.TAG_MENU.equals(itemType)) {
				MMenu element = createMenuAddition(items[i]);
				element.getTags().add(filter);
				container.getChildren().add(element);
				addMenuChildren(element, items[i], filter);
				// newItem = createMenuAdditionContribution(items[i]);
			} else if (IWorkbenchRegistryConstants.TAG_TOOLBAR.equals(itemType)) {
				E4Util.unsupported("Toolbar: " + id + " in " + location); //$NON-NLS-1$//$NON-NLS-2$
			}

			// if (newItem instanceof InternalControlContribution) {
			// ((InternalControlContribution)
			// newItem).setWorkbenchWindow(window);
			// }

			// Cache the relationship between the ICI and the
			// registry element used to back it
			// if (newItem != null) {
			// additions.addContributionItem(newItem,
			// getVisibleWhenForItem(newItem, items[i]));
			// }
		}
	}

	private void addToolBarChildren(final MElementContainer<MToolBarElement> container,
			IConfigurationElement parent) {
		IConfigurationElement[] items = parent.getChildren();
		for (int i = 0; i < items.length; i++) {
			String itemType = items[i].getName();
			// String id = getId(items[i]);

			if (IWorkbenchRegistryConstants.TAG_COMMAND.equals(itemType)) {
				MToolBarElement element = createToolBarCommandAddition(items[i]);
				container.getChildren().add(element);
				// } else if
				// (IWorkbenchRegistryConstants.TAG_DYNAMIC.equals(itemType)) {
				//				E4Util.unsupported("Dynamic: " + id + " in " + location); //$NON-NLS-1$//$NON-NLS-2$
				// } else if
				// (IWorkbenchRegistryConstants.TAG_CONTROL.equals(itemType)) {
				//				E4Util.unsupported("Control: " + id + " in " + location); //$NON-NLS-1$//$NON-NLS-2$
				// } else if
				// (IWorkbenchRegistryConstants.TAG_SEPARATOR.equals(itemType))
				// {
				// MMenuElement element = createSeparatorAddition(items[i]);
				// container.getChildren().add(element);
				// } else if
				// (IWorkbenchRegistryConstants.TAG_MENU.equals(itemType)) {
				// MMenu element = createMenuAddition(items[i]);
				// element.getTags().add(filter);
				// container.getChildren().add(element);
				// addMenuChildren(element, items[i], filter);
				// // newItem = createMenuAdditionContribution(items[i]);
				// } else if
				// (IWorkbenchRegistryConstants.TAG_TOOLBAR.equals(itemType)) {
				//				E4Util.unsupported("Toolbar: " + id + " in " + location); //$NON-NLS-1$//$NON-NLS-2$
			}

			// if (newItem instanceof InternalControlContribution) {
			// ((InternalControlContribution)
			// newItem).setWorkbenchWindow(window);
			// }

			// Cache the relationship between the ICI and the
			// registry element used to back it
			// if (newItem != null) {
			// additions.addContributionItem(newItem,
			// getVisibleWhenForItem(newItem, items[i]));
			// }
		}
	}

	public void addToModel() {
		if (inToolbar()) {
			toolBarContribution = MenuFactoryImpl.eINSTANCE.createToolBarContribution();
			String idContrib = MenuHelper.getId(configElement);
			if (idContrib != null && idContrib.length() > 0) {
				toolBarContribution.setElementId(idContrib);
			}
			toolBarContribution.setParentId(location.getPath());
			String query = location.getQuery();
			if (query == null || query.length() == 0) {
				query = "after=additions"; //$NON-NLS-1$
			}
			toolBarContribution.setPositionInParent(query);
			addToolBarChildren(toolBarContribution, configElement);
			application.getToolBarContributions().add(toolBarContribution);

			return;
		}
		menuContribution = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		String idContrib = MenuHelper.getId(configElement);
		if (idContrib != null && idContrib.length() > 0) {
			menuContribution.setElementId(idContrib);
		}
		if ("org.eclipse.ui.popup.any".equals(location.getPath())) { //$NON-NLS-1$
			menuContribution.setParentID("popup"); //$NON-NLS-1$
		} else {
			menuContribution.setParentID(location.getPath());
		}
		String query = location.getQuery();
		if (query == null || query.length() == 0) {
			query = "after=additions"; //$NON-NLS-1$
		}
		menuContribution.setPositionInParent(query);
		menuContribution.getTags().add("scheme:" + location.getScheme()); //$NON-NLS-1$
		String filter = MenuServiceFilter.MC_MENU;
		if ("popup".equals(location.getScheme())) { //$NON-NLS-1$
			filter = MenuServiceFilter.MC_POPUP;
		}
		menuContribution.getTags().add(filter);
		menuContribution.setVisibleWhen(MenuHelper.getVisibleWhen(configElement));
		addMenuChildren(menuContribution, configElement, filter);
		application.getMenuContributions().add(menuContribution);
	}

	public void dispose() {
		application.getMenuContributions().remove(menuContribution);
		application.getToolBarContributions().remove(toolBarContribution);
	}
}
