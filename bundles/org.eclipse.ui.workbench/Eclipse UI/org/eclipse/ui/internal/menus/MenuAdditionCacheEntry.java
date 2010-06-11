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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MExpression;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.swt.modeling.MenuServiceFilter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;

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

	// private String namespaceIdentifier;

	public MenuAdditionCacheEntry(MApplication application, IEclipseContext appContext,
			IConfigurationElement configElement, String attribute, String namespaceIdentifier) {
		this.application = application;
		// this.appContext = appContext;
		this.configElement = configElement;
		this.location = new MenuLocationURI(attribute);
		// this.namespaceIdentifier = namespaceIdentifier;
	}

	// private Expression getVisibleWhenForItem(IContributionItem item,
	// IConfigurationElement configElement) {
	// if (!visWhenMap.containsKey(configElement)) {
	// // Not parsed yet
	// try {
	// IConfigurationElement[] visibleConfig = configElement
	// .getChildren(IWorkbenchRegistryConstants.TAG_VISIBLE_WHEN);
	// if (visibleConfig.length > 0 && visibleConfig.length < 2) {
	// IConfigurationElement[] visibleChild = visibleConfig[0].getChildren();
	// if (visibleChild.length > 0) {
	// Expression visWhen = ExpressionConverter.getDefault().perform(
	// visibleChild[0]);
	// visWhenMap.put(configElement, visWhen);
	// }
	// }
	// } catch (InvalidRegistryObjectException e) {
	// visWhenMap.put(configElement, null);
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (CoreException e) {
	// visWhenMap.put(configElement, null);
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	//
	// return (Expression) visWhenMap.get(configElement);
	// }

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

		MMenu element = MenuFactoryImpl.eINSTANCE.createMenu();
		String id = getId(menuAddition);
		element.setElementId(id);
		String text = getLabel(menuAddition);
		String mnemonic = getMnemonic(menuAddition);
		if (text != null && mnemonic != null) {
			E4Util.unsupported("mnemonic processing in menus: " + id + ": " + text); //$NON-NLS-1$//$NON-NLS-2$
			int idx = text.indexOf(mnemonic);
			if (idx != -1) {
				text = text.substring(0, idx) + '&' + text.substring(idx);
			}
		}
		element.setIconURI(getIconUrl(menuAddition, IWorkbenchRegistryConstants.ATT_ICON));
		element.setLabel(Util.safeString(text));

		return element;
	}

	private MMenuElement createSeparatorAddition(final IConfigurationElement sepAddition) {
		String name = getName(sepAddition);
		MMenuElement element = MenuFactoryImpl.eINSTANCE.createMenuSeparator();
		element.setElementId(name);
		if (!isSeparatorVisible(sepAddition)) {
			element.setVisible(false);
		}
		return element;
	}

	private MCommand findCommand(String id) {
		for (MCommand cmd : application.getCommands()) {
			if (id.equals(cmd.getElementId())) {
				return cmd;
			}
		}
		return null;
	}

	private MMenuElement createCommandAddition(final IConfigurationElement commandAddition) {
		MHandledMenuItem item = MenuFactoryImpl.eINSTANCE.createHandledMenuItem();
		item.setElementId(getId(commandAddition));
		item.setCommand(findCommand(getCommandId(commandAddition)));
		Map parms = getParameters(commandAddition);
		for (Object obj : parms.entrySet()) {
			Map.Entry e = (Map.Entry) obj;
			MParameter parm = CommandsFactoryImpl.eINSTANCE.createParameter();
			parm.setName(e.getKey().toString());
			parm.setValue(e.getValue().toString());
			item.getParameters().add(parm);
		}
		item.setIconURI(getIconUrl(commandAddition, IWorkbenchRegistryConstants.ATT_ICON));
		item.setLabel(getLabel(commandAddition));
		item.setMnemonics(getMnemonic(commandAddition));
		item.setTooltip(getTooltip(commandAddition));
		item.setType(getStyle(commandAddition));
		item.setVisibleWhen(getVisibleWhen(commandAddition));
		return item;
	}

	/**
	 * @param commandAddition
	 * @return
	 */
	private MExpression getVisibleWhen(IConfigurationElement commandAddition) {
		try {
			IConfigurationElement[] visibleConfig = configElement
					.getChildren(IWorkbenchRegistryConstants.TAG_VISIBLE_WHEN);
			if (visibleConfig.length > 0 && visibleConfig.length < 2) {
				IConfigurationElement[] visibleChild = visibleConfig[0].getChildren();
				if (visibleChild.length > 0) {
					Expression visWhen = ExpressionConverter.getDefault().perform(visibleChild[0]);
					MCoreExpression exp = UiFactoryImpl.eINSTANCE.createCoreExpression();
					exp.setCoreExpressionId("programmatic.value"); //$NON-NLS-1$
					exp.setCoreExpression(visWhen);
					return exp;
					// visWhenMap.put(configElement, visWhen);
				}
			}
		} catch (InvalidRegistryObjectException e) {
			// visWhenMap.put(configElement, null);
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// visWhenMap.put(configElement, null);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param element
	 *            the configuration element
	 * @return <code>true</code> if the checkEnabled is <code>true</code>.
	 */
	static boolean getVisibleEnabled(IConfigurationElement element) {
		IConfigurationElement[] children = element
				.getChildren(IWorkbenchRegistryConstants.TAG_VISIBLE_WHEN);
		String checkEnabled = null;

		if (children.length > 0) {
			checkEnabled = children[0].getAttribute(IWorkbenchRegistryConstants.ATT_CHECK_ENABLED);
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
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_DISABLEDICON);
	}

	static String getHoverIconPath(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_HOVERICON);
	}

	static String getIconUrl(IConfigurationElement element, String attr) {
		String extendingPluginId = element.getDeclaringExtension().getContributor().getName();

		String iconPath = element.getAttribute(attr);
		if (iconPath == null) {
			return null;
		}
		if (!iconPath.startsWith("platform:")) { //$NON-NLS-1$
			iconPath = "platform:/plugin/" + extendingPluginId + "/" + iconPath; //$NON-NLS-1$//$NON-NLS-2$
		}
		URL url = null;
		try {
			url = FileLocator.find(new URL(iconPath));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url == null ? null : url.toString();
	}

	static ImageDescriptor getDisabledIconDescriptor(IConfigurationElement element) {
		String extendingPluginId = element.getDeclaringExtension().getContributor().getName();

		String iconPath = getDisabledIconPath(element);
		if (iconPath != null) {
			return AbstractUIPlugin.imageDescriptorFromPlugin(extendingPluginId, iconPath);
		}
		return null;
	}

	static ImageDescriptor getHoverIconDescriptor(IConfigurationElement element) {
		String extendingPluginId = element.getDeclaringExtension().getContributor().getName();

		String iconPath = getHoverIconPath(element);
		if (iconPath != null) {
			return AbstractUIPlugin.imageDescriptorFromPlugin(extendingPluginId, iconPath);
		}
		return null;
	}

	static String getHelpContextId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_HELP_CONTEXT_ID);
	}

	public static boolean isSeparatorVisible(IConfigurationElement element) {
		String val = element.getAttribute(IWorkbenchRegistryConstants.ATT_VISIBLE);
		return Boolean.valueOf(val).booleanValue();
	}

	public static String getClassSpec(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
	}

	public static String getCommandId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_COMMAND_ID);
	}

	private ItemType getStyle(IConfigurationElement element) {
		String style = element.getAttribute(IWorkbenchRegistryConstants.ATT_STYLE);
		if (style == null || style.length() == 0) {
			return ItemType.PUSH;
		}
		if (IWorkbenchRegistryConstants.STYLE_TOGGLE.equals(style)) {
			return ItemType.CHECK;
		}
		if (IWorkbenchRegistryConstants.STYLE_RADIO.equals(style)) {
			return ItemType.RADIO;
		}
		if (IWorkbenchRegistryConstants.STYLE_PULLDOWN.equals(style)) {
			E4Util.unsupported("Failed to get style for " + IWorkbenchRegistryConstants.STYLE_PULLDOWN); //$NON-NLS-1$
			// return CommandContributionItem.STYLE_PULLDOWN;
		}
		return ItemType.PUSH;
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
			String name = parameters[i].getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
			String value = parameters[i].getAttribute(IWorkbenchRegistryConstants.ATT_VALUE);
			if (name != null && value != null) {
				map.put(name, value);
			}
		}
		return map;
	}

	private void addChildren(final MElementContainer<MMenuElement> container,
			IConfigurationElement parent, String filter) {
		IConfigurationElement[] items = parent.getChildren();
		for (int i = 0; i < items.length; i++) {
			String itemType = items[i].getName();
			String id = getId(items[i]);

			if (IWorkbenchRegistryConstants.TAG_COMMAND.equals(itemType)) {
				MMenuElement element = createCommandAddition(items[i]);
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
				addChildren(element, items[i], filter);
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

	public void addToModel() {
		if (inToolbar()) {
			E4Util.unsupported("We don't support toolbar menu contributions yet " + location); //$NON-NLS-1$
			return;
		}
		menuContribution = MenuFactoryImpl.eINSTANCE.createMenuContribution();
		String idContrib = getId(configElement);
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
		addChildren(menuContribution, configElement, filter);
		application.getMenuContributions().add(menuContribution);
	}

	public void dispose() {
		application.getMenuContributions().remove(menuContribution);
	}
}
