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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.AbstractWorkbenchTrimWidget;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @since 3.3
 * 
 */
public class MenuAdditionCacheEntry extends AbstractContributionFactory {
	private IConfigurationElement additionElement;

	// Caches

	/**
	 * Maps an IContributionItem to its corresponding IConfigurationElement
	 */
	Map iciToConfigElementMap = new HashMap();

	/**
	 * Maps an IConfigurationElement to its parsed Expression
	 */
	private HashMap visWhenMap = new HashMap();
	
	private HashMap dynamicFactories = new HashMap();

	private IMenuService menuService;

	public MenuAdditionCacheEntry(IConfigurationElement element,
			IMenuService service, String location) {
		super(location);
		this.additionElement = element;
		this.menuService = service;

		generateSubCaches();
	}

	/**
	 * 
	 */
	private void generateSubCaches() {
		IConfigurationElement[] items = additionElement.getChildren();
		for (int i = 0; i < items.length; i++) {
			String itemType = items[i].getName();
			if (IWorkbenchRegistryConstants.TAG_MENU.equals(itemType)) {
				// Menus are special...we have to add any sub menu
				// items into their own cache
				// If the locationURI is null then this should be a sub menu
				// addition..create the 'root' URI

				String location = new MenuLocationURI(getLocation())
						.getScheme()
						+ ":" + MenuAdditionCacheEntry.getId(items[i]); //$NON-NLS-1$

				// -ALL- contibuted menus must have an id so create one
				// if necessary
				MenuAdditionCacheEntry subMenuEntry = new MenuAdditionCacheEntry(
						items[i], menuService, location);
				menuService.addContributionFactory(subMenuEntry);
			}
		}
	}

	private Expression getVisibleWhenForItem(IContributionItem item) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.AbstractContributionFactory#createContributionItems(org.eclipse.ui.internal.menus.IMenuService,
	 *      java.util.List)
	 */
	public void createContributionItems(IMenuService menuService, List additions) {
		additions.clear();

		IConfigurationElement[] items = additionElement.getChildren();
		for (int i = 0; i < items.length; i++) {
			String itemType = items[i].getName();
			IContributionItem newItem = null;

			if (IWorkbenchRegistryConstants.TAG_ITEM.equals(itemType)) {
				if (isDynamic(items[i])) {

				} else
					newItem = createItemAdditionContribution(items[i]);
			} else if (IWorkbenchRegistryConstants.TAG_DYNAMIC_CONTRIBUTION.equals(itemType)) {
				// ** Special Case ** Load the defined class and populate with
				// any items it returns. We leave 'newItem' as null since we are
				// -replacing- this element with a set of items
				
				// Attempt the load a cache to avoid failing retries
				if (!dynamicFactories.containsKey(items[i])) {
					AbstractDynamicContribution newFactory = 
						(AbstractDynamicContribution) Util.safeLoadExecutableExtension(items[i],
								IWorkbenchRegistryConstants.ATT_CLASS,
								AbstractDynamicContribution.class);
					dynamicFactories.put(items[i], newFactory);					
				}

				// If we get null now it means that the load failed...
				AbstractDynamicContribution dynamicFactory = (AbstractDynamicContribution) dynamicFactories.get(items[i]);
				if (dynamicFactory != null) {
					List dynamicContributions = new ArrayList();
					dynamicFactory.createContributionItems(dynamicContributions);
					for (Iterator iterator = dynamicContributions.iterator(); iterator
							.hasNext();) {
						IContributionItem ici = (IContributionItem) iterator.next();
						additions.add(ici);
					}
				}
				
			} else if (IWorkbenchRegistryConstants.TAG_WIDGET.equals(itemType)) {
				newItem = createWidgetAdditionContribution(items[i]);
			} else if (IWorkbenchRegistryConstants.TAG_SEPARATOR
					.equals(itemType)) {
				newItem = createSeparatorAdditionContribution(items[i]);
			} else if (IWorkbenchRegistryConstants.TAG_MENU.equals(itemType)) {
				newItem = createMenuAdditionContribution(items[i]);

			}

			// Cache the relationship between the ICI and the
			// registry element used to back it
			if (newItem != null) {
				iciToConfigElementMap.put(newItem, items[i]);
				additions.add(newItem);
				Expression visibleWhen = getVisibleWhenForItem(newItem);
				if (visibleWhen != null) {
					menuService.registerVisibleWhen(newItem, visibleWhen);
				}
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
		return new CommandContributionItem(getId(itemAddition),
				getCommandId(itemAddition), getParameters(itemAddition),
				getIconDescriptor(itemAddition), getLabel(itemAddition),
				getTooltip(itemAddition));
		// return new CommandContributionItem(getId(itemAddition),
		// itemAddition);
	}

	/*
	 * Support Utilities
	 */
	public static String getId(IConfigurationElement element) {
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

	public static AbstractWorkbenchTrimWidget getWidget(
			IConfigurationElement element) {
		return loadWidget(element);
	}

	/**
	 * @param element
	 * @return
	 */
	private static AbstractWorkbenchTrimWidget loadWidget(
			IConfigurationElement element) {
		AbstractWorkbenchTrimWidget widget = null;
		try {
			widget = (AbstractWorkbenchTrimWidget) element
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

	/**
	 * @param itemAddition
	 * @return
	 */
	public static String getCommandId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_COMMAND_ID);
	}

	/**
	 * @param element
	 * @return A map of parameters names to parameter values. All Strings. The
	 *         map may be empty.
	 */
	public static Map getParameters(IConfigurationElement element) {
		HashMap map = new HashMap();
		IConfigurationElement[] parameters = element
				.getChildren(IWorkbenchRegistryConstants.TAG_PARAMETER);
		for (int i = 0; i < parameters.length; i++) {
			String name = parameters[i]
					.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
			String value = parameters[i]
					.getAttribute(IWorkbenchRegistryConstants.ATT_VALUE);
			if (name != null && value != null) {
				map.put(name, value);
			}
		}
		return map;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.menus.AbstractContributionFactory#releaseContributionItems(org.eclipse.ui.internal.menus.IMenuService,
	 *      java.util.List)
	 */
	public void releaseContributionItems(IMenuService menuService, List items) {
		// TODO Auto-generated method stub

	}
}
