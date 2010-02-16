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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.provisional.presentations.IActionBarPresentationFactory;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @since 3.3
 * 
 */
public class MenuAdditionCacheEntry extends AbstractMenuAdditionCacheEntry {
	
	/**
	 * Maps an IConfigurationElement to its parsed Expression
	 */
	private HashMap visWhenMap = new HashMap();

	/**
	 * The menu service on which to generate all subcaches.
	 */
	private IMenuService menuService;

	/**
	 * List of caches created while processing this one. Used to clean up
	 * stale cache entries during removal
	 */
	private List subCaches;

	private boolean hasAdditions = false;

	private Boolean contributeToAllPopups = null;

	public MenuAdditionCacheEntry(IMenuService menuService,
			IConfigurationElement element, String location, String namespace) {
		super(location, namespace, element);
		this.menuService = menuService;
		findAdditions();
		generateSubCaches();
	}

	/**
	 * 
	 */
	private void generateSubCaches() {
		IConfigurationElement[] items = getConfigElement().getChildren();
		for (int i = 0; i < items.length; i++) {
			String itemType = items[i].getName();
			if (IWorkbenchRegistryConstants.TAG_MENU.equals(itemType)
					|| IWorkbenchRegistryConstants.TAG_TOOLBAR.equals(itemType)) {
				// Menus and toolbars are special...we have to add any sub menu
				// items into their own cache
				// If the locationURI is null then this should be a sub menu
				// addition..create the 'root' URI

				String location = new MenuLocationURI(getLocation())
						.getScheme()
						+ ":" + MenuAdditionCacheEntry.getId(items[i]); //$NON-NLS-1$

				// -ALL- contibuted menus must have an id so create one
				// if necessary
				MenuAdditionCacheEntry subMenuEntry = new MenuAdditionCacheEntry(
						menuService, items[i], location, getNamespace());
				menuService.addContributionFactory(subMenuEntry);
				
				if (subCaches == null)
					subCaches = new ArrayList();
				
				subCaches.add(subMenuEntry);
			}
		}
	}

	private Expression getVisibleWhenForItem(IContributionItem item, IConfigurationElement configElement) {
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
	public void createContributionItems(IServiceLocator serviceLocator,
			IContributionRoot additions) {
		IActionBarPresentationFactory actionBarPresentationFactory = null;

		IWorkbenchLocationService wls = (IWorkbenchLocationService) serviceLocator
		.getService(IWorkbenchLocationService.class);
		WorkbenchWindow window = (WorkbenchWindow) wls.getWorkbenchWindow();
		if (window != null) {
			actionBarPresentationFactory = window
					.getActionBarPresentationFactory();
		}

		IConfigurationElement[] items = getConfigElement().getChildren();
		for (int i = 0; i < items.length; i++) {
			String itemType = items[i].getName();
			IContributionItem newItem = null;

			if (IWorkbenchRegistryConstants.TAG_COMMAND.equals(itemType)) {
				newItem = createCommandAdditionContribution(serviceLocator,
						items[i]);
			} else if (IWorkbenchRegistryConstants.TAG_DYNAMIC.equals(itemType)) {
				newItem = createDynamicAdditionContribution(serviceLocator,
						items[i]);
			} else if (IWorkbenchRegistryConstants.TAG_CONTROL.equals(itemType)) {
				newItem = createControlAdditionContribution(serviceLocator,
						items[i]);
			} else if (IWorkbenchRegistryConstants.TAG_SEPARATOR
					.equals(itemType)) {
				newItem = createSeparatorAdditionContribution(items[i]);
			} else if (IWorkbenchRegistryConstants.TAG_MENU.equals(itemType)) {
				newItem = createMenuAdditionContribution(items[i]);
			} else if (IWorkbenchRegistryConstants.TAG_TOOLBAR.equals(itemType)) {
				newItem = createToolBarAdditionContribution(window,
						actionBarPresentationFactory, items[i]);
			}
			
			if (newItem instanceof InternalControlContribution) {
				((InternalControlContribution) newItem).setWorkbenchWindow(window);
			}

			// Cache the relationship between the ICI and the
			// registry element used to back it
			if (newItem != null) {
				additions.addContributionItem(newItem,
						getVisibleWhenForItem(newItem, items[i]));
			}
		}
	}

	/**
	 * @param window
	 * @param configurationElement
	 * @return the toolbar contribution item
	 */
	private IContributionItem createToolBarAdditionContribution(WorkbenchWindow window,
			IActionBarPresentationFactory actionBarPresentationFactory,
			IConfigurationElement configurationElement) {
		if (!inToolbar()) {
			return null;
		}
		String id = getId(configurationElement);
		String label = getLabel(configurationElement);
		if (label != null && label.length() > 0) {
			window.putToolbarLabel(id, label);
		}
		if (actionBarPresentationFactory != null) {
			return actionBarPresentationFactory.createToolBarContributionItem(
					actionBarPresentationFactory.createToolBarManager(),
					id);
		}
		return new ToolBarContributionItem(new ToolBarManager(),
				id);
	}

	/**
	 * @return <code>true</code> if this is a toolbar contribution
	 */
	private boolean inToolbar() {
		return getLocation().startsWith("toolbar"); //$NON-NLS-1$
	}

	/**
	 * @param configurationElement
	 * @return the menu manager
	 */
	private IContributionItem createMenuAdditionContribution(
			final IConfigurationElement menuAddition) {
		// Is this for a menu or a ToolBar ? We can't create
		// a menu directly under a Toolbar; we have to add an
		// item of style 'pulldown'
		if (inToolbar()) {
			return null;
		}

		String text = getLabel(menuAddition);
		String mnemonic = getMnemonic(menuAddition);
		if (text != null && mnemonic != null) {
			int idx = text.indexOf(mnemonic);
			if (idx != -1) {
				text = text.substring(0, idx) + '&' + text.substring(idx);
			}
		}
		MenuManager menuManager = new MenuManager(text, getIconDescriptor(menuAddition), getId(menuAddition));
		menuManager.setActionDefinitionId(getCommandId(menuAddition));
		return menuManager;
	}

	/**
	 * @param configurationElement
	 * @return
	 */
	private IContributionItem createSeparatorAdditionContribution(
			final IConfigurationElement sepAddition) {
		if (isSeparatorVisible(sepAddition)) {
			return new Separator(getName(sepAddition));
		}
		return new GroupMarker(getName(sepAddition));
	}

	/**
	 * @return
	 */
	private IContributionItem createDynamicAdditionContribution(
			final IServiceLocator locator,
			final IConfigurationElement dynamicAddition) {
		
		return new DynamicMenuContributionItem(getId(dynamicAddition), locator,
				dynamicAddition);
		
	}

	private IContributionItem createControlAdditionContribution(
			final IServiceLocator locator,
			final IConfigurationElement widgetAddition) {

		if (inToolbar()) {
			return new DynamicToolBarContributionItem(getId(widgetAddition), locator, widgetAddition);
		}
		
		return null;
		
	}

	private IContributionItem createCommandAdditionContribution(
			IServiceLocator locator, final IConfigurationElement commandAddition) {
		CommandContributionItemParameter parm = new CommandContributionItemParameter(
				locator, getId(commandAddition), getCommandId(commandAddition),
				getParameters(commandAddition),
				getIconDescriptor(commandAddition),
				getDisabledIconDescriptor(commandAddition),
				getHoverIconDescriptor(commandAddition),
				getLabel(commandAddition), getMnemonic(commandAddition),
				getTooltip(commandAddition), getStyle(commandAddition),
				getHelpContextId(commandAddition),
				getVisibleEnabled(commandAddition));
		if (inToolbar()) {
			parm.iconStyle = ICommandImageService.IMAGE_STYLE_TOOLBAR;
		}
		parm.mode = getMode(commandAddition);
		return new CommandContributionItem(parm);
	}

	/**
	 * @param element the configuration element
	 * @return <code>true</code> if the checkEnabled is <code>true</code>.
	 */
	static boolean getVisibleEnabled(IConfigurationElement element) {
		IConfigurationElement[] children = element
				.getChildren(IWorkbenchRegistryConstants.TAG_VISIBLE_WHEN);
		String checkEnabled = null;
		
		if (children.length>0) {
			checkEnabled = children[0]
				.getAttribute(IWorkbenchRegistryConstants.ATT_CHECK_ENABLED);
		}
		
		return checkEnabled != null && checkEnabled.equalsIgnoreCase("true"); //$NON-NLS-1$
	}

	/*
	 * Support Utilities
	 */
	public static String getId(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);

		// For sub-menu management -all- items must be id'd so enforce this
		// here (we could optimize by checking the 'name' of the config
		// element == "menu"
		if (id == null || id.length() == 0) {
			id = getCommandId(element);
		}
		if (id == null || id.length() == 0) {
			id = element.toString();
		}

		return id;
	}

	static String getName(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
	}
	
	static int getMode(IConfigurationElement element) {
		if ("FORCE_TEXT".equals(element.getAttribute(IWorkbenchRegistryConstants.ATT_MODE))) { //$NON-NLS-1$
			return CommandContributionItem.MODE_FORCE_TEXT;
		}
		return 0;
	}

	static String getLabel(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_LABEL);
	}

	static String getMnemonic(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_MNEMONIC);
	}

	static String getTooltip(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_TOOLTIP);
	}

	static String getIconPath(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_ICON);
	}

	static String getDisabledIconPath(IConfigurationElement element) {
		return element
				.getAttribute(IWorkbenchRegistryConstants.ATT_DISABLEDICON);
	}

	static String getHoverIconPath(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_HOVERICON);
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

	static ImageDescriptor getDisabledIconDescriptor(
			IConfigurationElement element) {
		String extendingPluginId = element.getDeclaringExtension()
				.getContributor().getName();

		String iconPath = getDisabledIconPath(element);
		if (iconPath != null) {
			return AbstractUIPlugin.imageDescriptorFromPlugin(
					extendingPluginId, iconPath);
		}
		return null;
	}

	static ImageDescriptor getHoverIconDescriptor(IConfigurationElement element) {
		String extendingPluginId = element.getDeclaringExtension()
				.getContributor().getName();

		String iconPath = getHoverIconPath(element);
		if (iconPath != null) {
			return AbstractUIPlugin.imageDescriptorFromPlugin(
					extendingPluginId, iconPath);
		}
		return null;
	}

	static String getHelpContextId(IConfigurationElement element) {
		return element
				.getAttribute(IWorkbenchRegistryConstants.ATT_HELP_CONTEXT_ID);
	}

	public static boolean isSeparatorVisible(IConfigurationElement element) {
		String val = element
				.getAttribute(IWorkbenchRegistryConstants.ATT_VISIBLE);
		return Boolean.valueOf(val).booleanValue();
	}

	public static String getClassSpec(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
	}

	public static String getCommandId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_COMMAND_ID);
	}

	private int getStyle(IConfigurationElement element) {
		String style = element
				.getAttribute(IWorkbenchRegistryConstants.ATT_STYLE);
		if (style == null || style.length() == 0) {
			return CommandContributionItem.STYLE_PUSH;
		}
		if (IWorkbenchRegistryConstants.STYLE_TOGGLE.equals(style)) {
			return CommandContributionItem.STYLE_CHECK;
		}
		if (IWorkbenchRegistryConstants.STYLE_RADIO.equals(style)) {
			return CommandContributionItem.STYLE_RADIO;
		}
		if (IWorkbenchRegistryConstants.STYLE_PULLDOWN.equals(style)) {
			return CommandContributionItem.STYLE_PULLDOWN;
		}
		return CommandContributionItem.STYLE_PUSH;
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

	/**
	 * @return Returns the subCaches.
	 */
	public List getSubCaches() {
		return subCaches;
	}
	
	private void findAdditions() {
		IConfigurationElement[] items = getConfigElement().getChildren();
		boolean done = false;
		for (int i = 0; i < items.length && !done; i++) {
			String itemType = items[i].getName();
			if (IWorkbenchRegistryConstants.TAG_SEPARATOR
			.equals(itemType)) {
				if (IWorkbenchActionConstants.MB_ADDITIONS.equals(getName(items[i]))) {
					hasAdditions  = true;
					done = true;
				}
			}
		}
	}
	
	public boolean hasAdditions() {
		return hasAdditions;
	}

	/**
	 * 
	 * Returns the value of the allPopups attribute
	 * 
	 * @return <code>true</code> if specified and the value equals to true,
	 *         <code>false</code> otherwise
	 * 
	 */
	public boolean contributeToAllPopups() {
		if (contributeToAllPopups == null) {
			String allPopups = getConfigElement().getAttribute("allPopups"); //$NON-NLS-1$
			if (allPopups == null || Boolean.valueOf(allPopups).booleanValue())
				contributeToAllPopups = Boolean.TRUE;
			else
				contributeToAllPopups = Boolean.FALSE;
		}
		return contributeToAllPopups.booleanValue();
	}
}
