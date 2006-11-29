/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @since 3.3
 * 
 */
public class MenuAdditionCacheEntry {
	private IConfigurationElement additionElement;
	private MenuLocationURI uri = null;
	private IMenuService menuSvc = null;

	// Caches

	/**
	 * Maps an IContributionItem to its corresponding IConfigurationElement
	 */
	Map iciToConfigElementMap = new HashMap();

	/**
	 * Maps an IConfigurationElement to its parsed Expression
	 */
	private HashMap visWhenMap = new HashMap();

	public MenuAdditionCacheEntry(IConfigurationElement element,
			IMenuService service) {
		this.additionElement = element;

		String locationURI = additionElement
				.getAttribute(IWorkbenchRegistryConstants.TAG_LOCATION_URI);

		// If the locationURI is null then this should be a sub menu
		// addition..create the 'root' URI
		if (locationURI == null) {
			locationURI = "menu:" + getId(element); //$NON-NLS-1$
		}
		uri = new MenuLocationURI(locationURI);

		menuSvc = service;
	}

	public Expression getVisibleWhenForItem(IContributionItem item) {
		IConfigurationElement configElement = (IConfigurationElement) iciToConfigElementMap
				.get(item);
		if (configElement == null)
			return null;

		if (!visWhenMap.containsKey(configElement)) {
			// Not parsed yet
			try {
				IConfigurationElement[] visibleConfig = configElement
						.getChildren(IWorkbenchRegistryConstants.TAG_VISIBLE_WHEN);
				if (visibleConfig.length > 0 && visibleConfig.length < 2) {
					IConfigurationElement[] visibleChild = visibleConfig[0]
							.getChildren();
					if (visibleChild.length > 0) {
						Expression visWhen = ExpressionConverter.getDefault()
								.perform(visibleChild[0]);
						visWhenMap.put(configElement, visWhen);
					}
				}
			} catch (InvalidRegistryObjectException e) {
				visWhenMap.put(configElement, null);
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				visWhenMap.put(configElement, null);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return (Expression) visWhenMap.get(configElement);
	}

	/**
	 * Populate the list
	 * 
	 * @param additions
	 */
	public void getContributionItems(List additions) {
		additions.clear();

		IConfigurationElement[] items = additionElement.getChildren();
		for (int i = 0; i < items.length; i++) {
			String itemType = items[i].getName();
			IContributionItem newItem = null;

			if (IWorkbenchRegistryConstants.TAG_ITEM.equals(itemType)) {
				if (isDynamic(items[i])) {

				} else
					newItem = createItemAdditionContribution(items[i]);
			} else if (IWorkbenchRegistryConstants.TAG_WIDGET.equals(itemType)) {
				newItem = createWidgetAdditionContribution(items[i]);
			} else if (IWorkbenchRegistryConstants.TAG_SEPARATOR
					.equals(itemType)) {
				newItem = createSeparatorAdditionContribution(items[i]);
			} else if (IWorkbenchRegistryConstants.TAG_MENU.equals(itemType)) {
				newItem = createMenuAdditionContribution(items[i]);

				// Menus are special...we have to add any sub menu
				// items into their own cache

				// -ALL- contibuted menus must have an id so create one
				// if necessary
				String menuId = getId(items[i]);
				MenuLocationURI uri = new MenuLocationURI("menu:" + menuId); //$NON-NLS-1$
				List subMenuCache = menuSvc.getAdditionsForURI(uri);
				MenuAdditionCacheEntry subMenuEntry = new MenuAdditionCacheEntry(
						items[i], menuSvc);

				// Add the 'original' definition to the start of the list
				subMenuCache.add(0, subMenuEntry);
			}

			// Cache the relationship between the ICI and the
			// registry element used to back it
			if (newItem != null) {
				additions.add(newItem);
				iciToConfigElementMap.put(newItem, items[i]);
			}
		}
	}

	/**
	 * @param configurationElement
	 * @return the menu manager
	 */
	private IContributionItem createMenuAdditionContribution(
			final IConfigurationElement menuAddition) {
		return new MenuManager(getLabel(menuAddition), getId(menuAddition));
	}

	/**
	 * @param configurationElement
	 * @return
	 */
	private IContributionItem createSeparatorAdditionContribution(
			final IConfigurationElement sepAddition) {
		return new SeparatorContributionItem(getId(sepAddition), sepAddition);
	}

	/**
	 * @return
	 */
	private IContributionItem createWidgetAdditionContribution(
			final IConfigurationElement widgetAddition) {
		return new WidgetContributionItem(getId(widgetAddition), widgetAddition);
	}

	/**
	 * @param configurationElement
	 * @return
	 */
	private IContributionItem createItemAdditionContribution(
			final IConfigurationElement itemAddition) {
		return new CommandContributionItem(getId(itemAddition), itemAddition);
	}

	/**
	 * @return Returns the uri.
	 */
	public MenuLocationURI getUri() {
		return uri;
	}

	/*
	 * Support Utilities
	 */
	private String getId(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);

		// For sub-menu management -all- items must be id'd so enforce this
		// here (we could optimize by checking the 'name' of the config
		// element == "menu"
		if (id == null || id.length() == 0)
			id = element.toString();

		return id;
	}

	static String getLabel(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
	}

	static String getTooltip(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_TOOLTIP);
	}

	static String getIconPath(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
	}

	static ImageDescriptor getIconDescriptor(IConfigurationElement element) {
		String extendingPluginId = element.getDeclaringExtension()
				.getContributor().getName();

		String iconPath = getIconPath(element);
		if (iconPath != null) {
			return AbstractUIPlugin.imageDescriptorFromPlugin(
					extendingPluginId, iconPath);
		}
		return null;
	}

	public static AbstractWorkbenchWidget getWidget(
			IConfigurationElement element) {
		return loadWidget(element);
	}

	/**
	 * @param element
	 * @return
	 */
	private static AbstractWorkbenchWidget loadWidget(
			IConfigurationElement element) {
		AbstractWorkbenchWidget widget = null;
		try {
			widget = (AbstractWorkbenchWidget) element
					.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return widget;
	}

	public static boolean isSeparatorVisible(IConfigurationElement element) {
		String val = element
				.getAttribute(IWorkbenchRegistryConstants.ATT_VISIBLE);
		return Boolean.valueOf(val).booleanValue();
	}

	public static String getClassSpec(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
	}

	public static boolean isDynamic(IConfigurationElement element) {
		return getClassSpec(element) != null
				&& getClassSpec(element).length() > 0;
	}
}
