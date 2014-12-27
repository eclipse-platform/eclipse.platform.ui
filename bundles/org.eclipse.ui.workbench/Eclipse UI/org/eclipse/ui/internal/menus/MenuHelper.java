/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 180308
 *******************************************************************************/
package org.eclipse.ui.internal.menus;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.internal.workbench.ContributionsAnalyzer;
import org.eclipse.e4.ui.internal.workbench.RenderedElementUtil;
import org.eclipse.e4.ui.internal.workbench.swt.Policy;
import org.eclipse.e4.ui.internal.workbench.swt.WorkbenchSWTActivator;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MExpression;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.renderers.swt.DirectContributionItem;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.ActionDescriptor;
import org.eclipse.ui.internal.OpenPreferencesAction;
import org.eclipse.ui.internal.PluginAction;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.handlers.ActionDelegateHandlerProxy;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class MenuHelper {

	public static void trace(String msg, Throwable error) {
		WorkbenchSWTActivator.trace(Policy.MENUS, msg, error);
	}

	public static final String MAIN_MENU_ID = ActionSet.MAIN_MENU;
	private static Field urlField;

	/**
	 * The private 'location' field that is defined in the FileImageDescriptor.
	 * 
	 * @see #getLocation(ImageDescriptor)
	 */
	private static Field locationField;

	/**
	 * The private 'name' field that is defined in the FileImageDescriptor.
	 * 
	 * @see #getName(ImageDescriptor)
	 */
	private static Field nameField;

	public static String getActionSetCommandId(IConfigurationElement element) {
		String id = MenuHelper.getDefinitionId(element);
		if (id != null) {
			return id;
		}
		id = MenuHelper.getId(element);
		String actionSetId = null;
		Object obj = element.getParent();
		while (obj instanceof IConfigurationElement && actionSetId == null) {
			IConfigurationElement parent = (IConfigurationElement) obj;
			String parentName = parent.getName();
			if (parentName.equals(IWorkbenchRegistryConstants.TAG_ACTION_SET)
					|| parentName.equals(IWorkbenchRegistryConstants.TAG_VIEW_CONTRIBUTION)
					|| parentName.equals(IWorkbenchRegistryConstants.TAG_EDITOR_CONTRIBUTION)) {
				actionSetId = MenuHelper.getId(parent);
			}
			obj = parent.getParent();
		}
		return IWorkbenchRegistryConstants.AUTOGENERATED_PREFIX + actionSetId + '/' + id;
	}

	public static String getImageUrl(ImageDescriptor imageDescriptor) {
		return getIconURI(imageDescriptor, null);
	}

	private static URL getUrl(Class<?> idc, ImageDescriptor imageDescriptor) {
		try {
			if (urlField == null) {
				urlField = idc.getDeclaredField("url"); //$NON-NLS-1$
				urlField.setAccessible(true);
			}
			return (URL) urlField.get(imageDescriptor);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static Class<?> getLocation(ImageDescriptor imageDescriptor) {
		try {
			if (locationField == null) {
				locationField = imageDescriptor.getClass().getDeclaredField("location"); //$NON-NLS-1$
				locationField.setAccessible(true);
			}
			return (Class<?>) locationField.get(imageDescriptor);
		} catch (SecurityException e) {
			WorkbenchPlugin.log(e);
		} catch (NoSuchFieldException e) {
			WorkbenchPlugin.log(e);
		} catch (IllegalAccessException e) {
			WorkbenchPlugin.log(e);
		}
		return null;
	}

	private static String getName(ImageDescriptor imageDescriptor) {
		try {
			if (nameField == null) {
				nameField = imageDescriptor.getClass().getDeclaredField("name"); //$NON-NLS-1$
				nameField.setAccessible(true);
			}
			return (String) nameField.get(imageDescriptor);
		} catch (SecurityException e) {
			WorkbenchPlugin.log(e);
		} catch (NoSuchFieldException e) {
			WorkbenchPlugin.log(e);
		} catch (IllegalAccessException e) {
			WorkbenchPlugin.log(e);
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

	static MExpression getVisibleWhen(final IConfigurationElement commandAddition) {
		try {
			IConfigurationElement[] visibleConfig = commandAddition
					.getChildren(IWorkbenchRegistryConstants.TAG_VISIBLE_WHEN);
			if (visibleConfig.length > 0 && visibleConfig.length < 2) {
				IConfigurationElement[] visibleChild = visibleConfig[0].getChildren();
				if (visibleChild.length == 0) {
					String checkEnabled = visibleConfig[0]
							.getAttribute(IWorkbenchRegistryConstants.ATT_CHECK_ENABLED);
					if (Boolean.parseBoolean(checkEnabled)) {
						final String commandId = getCommandId(commandAddition);
						if (commandId == null) {
							return null;
						}

						Expression visWhen = new Expression() {
							@Override
							public EvaluationResult evaluate(IEvaluationContext context) {
								EHandlerService service = getFromContext(context,
										EHandlerService.class);
								ICommandService commandService = getFromContext(context,
										ICommandService.class);
								if (service == null || commandService == null) {
									WorkbenchPlugin
											.log("Could not retrieve EHandlerService or ICommandService from context evaluation context for" //$NON-NLS-1$
													+ commandId);
									return EvaluationResult.FALSE;
								}
								Command c = commandService.getCommand(commandId);
								ParameterizedCommand generateCommand = ParameterizedCommand
										.generateCommand(c, Collections.EMPTY_MAP);
								return EvaluationResult
										.valueOf(service.canExecute(generateCommand));
							}
						};
						MCoreExpression exp = UiFactoryImpl.eINSTANCE.createCoreExpression();
						exp.setCoreExpressionId("programmatic.value"); //$NON-NLS-1$
						exp.setCoreExpression(visWhen);
						return exp;
					}
				} else if (visibleChild.length > 0) {
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
	 * Do a type-safe extraction of an object from the evalation context
	 * 
	 * @param context
	 *            the evaluation context
	 * @param expectedType
	 *            the expected type
	 * @return an object of the expected type or <code>null</code>
	 * @throws NullPointerException
	 *             if either argument is <code>null</code>
	 */
	protected static <T> T getFromContext(IEvaluationContext context, Class<T> expectedType) {
		if (context == null || expectedType == null) {
			throw new NullPointerException();
		}
		final Object rawValue = context.getVariable(expectedType.getName());
		return (expectedType.isInstance(rawValue)) ? expectedType.cast(rawValue) : null;
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

	static String getPath(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_PATH);
	}

	static String getMenuBarPath(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_MENUBAR_PATH);
	}

	static String getToolBarPath(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_TOOLBAR_PATH);
	}

	static String getMnemonic(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_MNEMONIC);
	}

	static String getTooltip(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_TOOLTIP);
	}

	public static String getIconURI(IConfigurationElement element, String attr) {
		String iconPath = element.getAttribute(attr);
		if (iconPath == null) {
			return null;
		}

		// If iconPath doesn't specify a scheme, then try to transform to a URL
		// RFC 3986: scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
		// This allows using data:, http:, or other custom URL schemes
		if (!iconPath.matches("\\p{Alpha}[\\p{Alnum}+.-]*:.*")) { //$NON-NLS-1$
			// First attempt to resolve in ISharedImages (e.g. "IMG_OBJ_FOLDER")
			// as per bug 391232 & AbstractUIPlugin.imageDescriptorFromPlugin().
			ImageDescriptor d = WorkbenchPlugin.getDefault().getSharedImages()
					.getImageDescriptor(iconPath);
			if (d != null) {
				return getImageUrl(d);
			}
			String extendingPluginId = element.getDeclaringExtension().getContributor().getName();
			iconPath = "platform:/plugin/" + extendingPluginId + "/" + iconPath; //$NON-NLS-1$//$NON-NLS-2$
		}
		URL url = null;
		try {
			url = FileLocator.find(new URL(iconPath));
		} catch (MalformedURLException e) {
			/* IGNORE */
		}
		return url == null ? iconPath : rewriteDurableURL(url.toString());
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

	public static ItemType getStyle(IConfigurationElement element) {
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
			trace("Failed to get style for " + IWorkbenchRegistryConstants.STYLE_PULLDOWN, null); //$NON-NLS-1$
			// return CommandContributionItem.STYLE_PULLDOWN;
		}
		return ItemType.PUSH;
	}

	public static boolean hasPulldownStyle(IConfigurationElement element) {
		String style = element.getAttribute(IWorkbenchRegistryConstants.ATT_STYLE);
		return IWorkbenchRegistryConstants.STYLE_PULLDOWN.equals(style);
	}

	public static boolean getRetarget(IConfigurationElement element) {
		String r = element.getAttribute(IWorkbenchRegistryConstants.ATT_RETARGET);
		return Boolean.valueOf(r);
	}

	public static String getDefinitionId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_DEFINITION_ID);
	}

	public static Map<String, String> getParameters(IConfigurationElement element) {
		HashMap<String, String> map = new HashMap<String, String>();
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

	public static MMenu createMenuAddition(IConfigurationElement menuAddition) {
		MMenu element = MenuFactoryImpl.eINSTANCE.createMenu();
		String id = MenuHelper.getId(menuAddition);
		element.setElementId(id);
		String text = MenuHelper.getLabel(menuAddition);
		String mnemonic = MenuHelper.getMnemonic(menuAddition);
		if (text != null && mnemonic != null) {
			int idx = text.indexOf(mnemonic);
			if (idx != -1) {
				text = text.substring(0, idx) + '&' + text.substring(idx);
			}
		}
		element.setVisibleWhen(getVisibleWhen(menuAddition));
		element.setIconURI(MenuHelper
				.getIconURI(menuAddition, IWorkbenchRegistryConstants.ATT_ICON));
		element.setLabel(Util.safeString(text));

		return element;
	}

	public static MMenuElement createLegacyMenuActionAdditions(MApplication app,
			final IConfigurationElement element) {
		final String id = MenuHelper.getId(element);
		String text = MenuHelper.getLabel(element);
		String mnemonic = MenuHelper.getMnemonic(element);
		if (text != null && mnemonic != null) {
			int idx = text.indexOf(mnemonic);
			if (idx != -1) {
				text = text.substring(0, idx) + '&' + text.substring(idx);
			}
		}
		String iconUri = MenuHelper.getIconURI(element, IWorkbenchRegistryConstants.ATT_ICON);
		final String cmdId = MenuHelper.getActionSetCommandId(element);

		MCommand cmd = ContributionsAnalyzer.getCommandById(app, cmdId);
		if (cmd == null) {
			ECommandService commandService = app.getContext().get(ECommandService.class);
			Command command = commandService.getCommand(cmdId);
			if (command == null) {
				ICommandService ics = app.getContext().get(ICommandService.class);
				command = commandService.defineCommand(cmdId, text, null, ics.getCategory(null),
						null);
			}
			cmd = CommandsFactoryImpl.eINSTANCE.createCommand();
			cmd.setCommandName(text);
			cmd.setElementId(cmdId);
			app.getCommands().add(cmd);
		}

		String style = element.getAttribute(IWorkbenchRegistryConstants.ATT_STYLE);
		String pulldown = element.getAttribute("pulldown"); //$NON-NLS-1$
		if (IWorkbenchRegistryConstants.STYLE_PULLDOWN.equals(style)
				|| (pulldown != null && pulldown.equals("true"))) { //$NON-NLS-1$
			MMenuItem item = RenderedElementUtil.createRenderedMenuItem();
			item.setLabel(text);
			if (iconUri != null) {
				item.setIconURI(iconUri);
			}
			IContextFunction generator = new ContextFunction() {
				@Override
				public Object compute(IEclipseContext context, String contextKey) {
					IWorkbenchWindow window = context.get(IWorkbenchWindow.class);
					if (window == null) {
						return null;
					}
					ActionDescriptor desc = new ActionDescriptor(element,
							ActionDescriptor.T_WORKBENCH_PULLDOWN, window);
					final PluginAction action = desc.getAction();
					return new ActionContributionItem(action) {
						@Override
						public void dispose() {
							super.dispose();
							action.disposeDelegate();
						}
					};
				}
			};
			RenderedElementUtil.setContributionManager(item, generator);
			return item;
		}

		ItemType type = ItemType.PUSH;
		if (IWorkbenchRegistryConstants.STYLE_TOGGLE.equals(style)) {
			type = ItemType.CHECK;
		} else if (IWorkbenchRegistryConstants.STYLE_RADIO.equals(style)) {
			type = ItemType.RADIO;
		}
		MHandledMenuItem item = MenuFactoryImpl.eINSTANCE.createHandledMenuItem();
		item.setElementId(id);
		item.setLabel(text);
		item.setType(type);
		item.setCommand(cmd);
		if (iconUri != null) {
			item.setIconURI(iconUri);
		}
		return item;
	}

	public static String getDescription(IConfigurationElement configElement) {
		return configElement.getAttribute(IWorkbenchRegistryConstants.TAG_DESCRIPTION);
	}

	public static MToolBarElement createLegacyToolBarActionAdditions(MApplication app,
			final IConfigurationElement element) {
		String cmdId = MenuHelper.getActionSetCommandId(element);
		final String id = MenuHelper.getId(element);
		String text = MenuHelper.getLabel(element);
		String mnemonic = MenuHelper.getMnemonic(element);
		if (text != null && mnemonic != null) {
			int idx = text.indexOf(mnemonic);
			if (idx != -1) {
				text = text.substring(0, idx) + '&' + text.substring(idx);
			}
		}
		String iconUri = MenuHelper.getIconURI(element, IWorkbenchRegistryConstants.ATT_ICON);
		String disabledIconUri = MenuHelper.getIconURI(element,
				IWorkbenchRegistryConstants.ATT_DISABLEDICON);
		MCommand cmd = ContributionsAnalyzer.getCommandById(app, cmdId);
		if (cmd == null) {
			ECommandService commandService = app.getContext().get(ECommandService.class);
			Command command = commandService.getCommand(cmdId);
			if (command == null) {
				ICommandService ics = app.getContext().get(
						ICommandService.class);
				command = commandService.defineCommand(cmdId, text, null, ics.getCategory(null),
						null);
			}
			cmd = CommandsFactoryImpl.eINSTANCE.createCommand();
			cmd.setCommandName(text);
			cmd.setElementId(cmdId);
			app.getCommands().add(cmd);
		}
		final MHandledToolItem item = MenuFactoryImpl.eINSTANCE.createHandledToolItem();

		String style = element.getAttribute(IWorkbenchRegistryConstants.ATT_STYLE);
		String pulldown = element.getAttribute("pulldown"); //$NON-NLS-1$
		if (IWorkbenchRegistryConstants.STYLE_TOGGLE.equals(style)) {
			item.setType(ItemType.CHECK);
			IContextFunction generator = createToggleFunction(element);
			if (generator != null) {
				item.getTransientData().put(ItemType.CHECK.toString(), generator);
			}
		} else if (IWorkbenchRegistryConstants.STYLE_RADIO.equals(style)) {
			item.setType(ItemType.RADIO);
		} else {
			item.setType(ItemType.PUSH);
		}

		if (IWorkbenchRegistryConstants.STYLE_PULLDOWN.equals(style)
				|| (pulldown != null && pulldown.equals("true"))) { //$NON-NLS-1$
			MMenu menu = RenderedElementUtil.createRenderedMenu();
			ECommandService cs = app.getContext().get(ECommandService.class);
			final ParameterizedCommand parmCmd = cs.createCommand(cmdId, null);
			IContextFunction generator = new ContextFunction() {
				@Override
				public Object compute(IEclipseContext context, String contextKey) {
					return new IMenuCreator() {
						private ActionDelegateHandlerProxy handlerProxy;

						private ActionDelegateHandlerProxy getProxy() {
							if (handlerProxy == null) {
								handlerProxy = new ActionDelegateHandlerProxy(element,
										IWorkbenchRegistryConstants.ATT_CLASS, id, parmCmd,
										PlatformUI
										.getWorkbench().getActiveWorkbenchWindow(), null, null,
 null);
							}
							return handlerProxy;
						}

						private IWorkbenchWindowPulldownDelegate getDelegate() {
							getProxy();
							if (handlerProxy == null) {
								return null;
							}
							if (handlerProxy.getDelegate() == null) {
								handlerProxy.loadDelegate();

								ISelectionService service = PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow().getSelectionService();
								IActionDelegate delegate = handlerProxy.getDelegate();
								delegate.selectionChanged(handlerProxy.getAction(),
										service.getSelection());
							}
							return (IWorkbenchWindowPulldownDelegate) handlerProxy.getDelegate();
						}

						@Override
						public Menu getMenu(Menu parent) {
							IWorkbenchWindowPulldownDelegate2 delegate = (IWorkbenchWindowPulldownDelegate2) getDelegate();
							if (delegate == null) {
								return null;
							}
							return delegate.getMenu(parent);
						}

						@Override
						public Menu getMenu(Control parent) {
							return getDelegate() == null ? null : getDelegate().getMenu(parent);
						}

						@Override
						public void dispose() {
							if (handlerProxy != null) {
								handlerProxy.dispose();
								handlerProxy = null;
							}
						}
					};
				}
			};
			RenderedElementUtil.setContributionManager(menu, generator);
			item.setMenu(menu);
		}
		
		item.setElementId(id);
		item.setCommand(cmd);
		if (iconUri == null) {
			item.setLabel(text);
		} else {
			item.setIconURI(iconUri);
		}
		if (disabledIconUri != null) {
			setDisabledIconURI(item, disabledIconUri);
		}
		String tooltip = getTooltip(element);
		// if no tooltip defined, use the textual label as the tooltip
		item.setTooltip(tooltip == null ? text : tooltip);
		return item;
	}

	private static int getType(String name) {
		if (name.equals(IWorkbenchRegistryConstants.TAG_ACTION_SET)) {
			return ActionDescriptor.T_WORKBENCH;
		} else if (name.equals(IWorkbenchRegistryConstants.TAG_VIEW_CONTRIBUTION)) {
			return ActionDescriptor.T_VIEW;
		} else if (name.equals(IWorkbenchRegistryConstants.TAG_EDITOR_CONTRIBUTION)) {
			return ActionDescriptor.T_EDITOR;
		}
		return -1;
	}

	private static IContextFunction createToggleFunction(final IConfigurationElement element) {
		Object ice = element.getParent();
		if (!(ice instanceof IConfigurationElement)) {
			return null;
		}

		// identify the type of contribution that this is
		IConfigurationElement parent = (IConfigurationElement) ice;
		final int type = getType(parent.getName());
		if (type == -1) {
			// unknown, don't create a toggling function
			return null;
		}

		IContextFunction generator = new ContextFunction() {
			private ActionDescriptor getDescriptor(IEclipseContext context) {
				switch (type) {
				case ActionDescriptor.T_WORKBENCH:
					IWorkbenchWindow window = context.get(IWorkbenchWindow.class);
					return window == null ? null : new ActionDescriptor(element, type, window);
				case ActionDescriptor.T_EDITOR:
					return new ActionDescriptor(element, type, null);
				case ActionDescriptor.T_VIEW:
					MPart part = context.get(MPart.class);
					if (part != null) {
						Object object = part.getObject();
						if (object instanceof CompatibilityPart) {
							return new ActionDescriptor(element, type,
									((CompatibilityPart) object).getPart());
						}
					}
					return null;
				default:
					return null;
				}
			}

			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				final MHandledItem model = context.get(MHandledItem.class);
				if (model == null) {
					return null;
				}
				ActionDescriptor desc = getDescriptor(context);
				final IAction action = desc.getAction();
				final IPropertyChangeListener propListener = new IPropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent event) {
						if (IAction.CHECKED.equals(event.getProperty())) {
							boolean checked = false;
							if (event.getNewValue() instanceof Boolean) {
								checked = ((Boolean) event.getNewValue()).booleanValue();
							}
							model.setSelected(checked);
						}
					}
				};
				action.addPropertyChangeListener(propListener);
				Runnable obj = new Runnable() {
					@Override
					@Execute
					public void run() {
						action.removePropertyChangeListener(propListener);
					}
				};
				model.setSelected(action.isChecked());
				return obj;
			}
		};
		return generator;
	}

	public static MMenu createMenu(MenuManager manager) {
		MMenu subMenu = MenuFactoryImpl.eINSTANCE.createMenu();
		subMenu.setLabel(manager.getMenuText());
		subMenu.setElementId(manager.getId());
		return subMenu;
	}

	public static MMenuItem createItem(MApplication application, CommandContributionItem cci) {
		if (cci.getCommand() == null) {
			return null;
		}
		String id = cci.getCommand().getId();
		for (MCommand command : application.getCommands()) {
			if (id.equals(command.getElementId())) {
				CommandContributionItemParameter data = cci.getData();
				MHandledMenuItem menuItem = MenuFactoryImpl.eINSTANCE.createHandledMenuItem();
				menuItem.setCommand(command);
				menuItem.setContributorURI(command.getContributorURI());
				if (data.label != null) {
					menuItem.setLabel(data.label);
				} else {
					menuItem.setLabel(command.getCommandName());
				}
				if (data.mnemonic != null) {
					menuItem.setMnemonics(data.mnemonic);
				}
				if (data.icon != null) {
					menuItem.setIconURI(getIconURI(data.icon, application.getContext()));
				} else {
					menuItem.setIconURI(getIconURI(id, application.getContext(),
							ICommandImageService.TYPE_DEFAULT));
				}
				String itemId = cci.getId();
				menuItem.setElementId(itemId == null ? id : itemId);
				return menuItem;
			}
		}
		return null;
	}

	public static MToolItem createToolItem(MApplication application, CommandContributionItem cci) {
		String id = cci.getCommand().getId();
		for (MCommand command : application.getCommands()) {
			if (id.equals(command.getElementId())) {
				CommandContributionItemParameter data = cci.getData();
				MHandledToolItem toolItem = MenuFactoryImpl.eINSTANCE.createHandledToolItem();
				toolItem.setCommand(command);
				toolItem.setContributorURI(command.getContributorURI());
				toolItem.setVisible(cci.isVisible());

				String iconURI = null;
				String disabledIconURI = null;

				toolItem.setType(ItemType.PUSH);
				if (data.style == CommandContributionItem.STYLE_CHECK)
					toolItem.setType(ItemType.CHECK);
				else if (data.style == CommandContributionItem.STYLE_RADIO)
					toolItem.setType(ItemType.RADIO);

				if (data.icon != null) {
					iconURI = getIconURI(data.icon, application.getContext());
				}
				if (iconURI == null) {
					iconURI = getIconURI(id, application.getContext(),
							ICommandImageService.TYPE_DEFAULT);
				}
				if (iconURI == null) {
					toolItem.setLabel(command.getCommandName());
				} else {
					toolItem.setIconURI(iconURI);
				}

				if (data.disabledIcon != null) {
					disabledIconURI = getIconURI(data.disabledIcon, application.getContext());
				}

				if (disabledIconURI == null) {
					disabledIconURI = getIconURI(id, application.getContext(),
							ICommandImageService.TYPE_DISABLED);
				}

				if (disabledIconURI != null) {
					setDisabledIconURI(toolItem, disabledIconURI);
				}

				if (data.tooltip != null) {
					toolItem.setTooltip(data.tooltip);
				} else if (data.label != null) {
					toolItem.setTooltip(data.label);
				} else {
					toolItem.setTooltip(command.getDescription());
				}

				String itemId = cci.getId();
				toolItem.setElementId(itemId == null ? id : itemId);
				return toolItem;
			}
		}
		return null;
	}

	public static MToolItem createToolItem(MApplication application, ActionContributionItem item) {
		final IAction action = item.getAction();
		String id = action.getActionDefinitionId();
		if (id != null) {
			for (MCommand command : application.getCommands()) {
				if (id.equals(command.getElementId())) {
					MHandledToolItem toolItem = MenuFactoryImpl.eINSTANCE.createHandledToolItem();
					toolItem.setCommand(command);
					toolItem.setContributorURI(command.getContributorURI());
					toolItem.setVisible(item.isVisible());

					String iconURI = getIconURI(action.getImageDescriptor(),
							application.getContext());
					if (iconURI == null) {
						iconURI = getIconURI(id, application.getContext(),
								ICommandImageService.TYPE_DEFAULT);
						if (iconURI == null) {
							toolItem.setLabel(command.getCommandName());
						} else {
							toolItem.setIconURI(iconURI);
						}
					} else {
						toolItem.setIconURI(iconURI);
					}
					if (action.getToolTipText() != null) {
						toolItem.setTooltip(action.getToolTipText());
					}

					String disabledIconURI = getIconURI(action.getDisabledImageDescriptor(),
							application.getContext());
					if (disabledIconURI == null)
						disabledIconURI = getIconURI(id, application.getContext(),
								ICommandImageService.TYPE_DEFAULT);
					if (disabledIconURI != null)
						setDisabledIconURI(toolItem, disabledIconURI);

					switch (action.getStyle()) {
					case IAction.AS_CHECK_BOX:
						toolItem.setType(ItemType.CHECK);
						toolItem.setSelected(action.isChecked());
						break;
					case IAction.AS_RADIO_BUTTON:
						toolItem.setType(ItemType.RADIO);
						toolItem.setSelected(action.isChecked());
						break;
					default:
						toolItem.setType(ItemType.PUSH);
						break;
					}
					String itemId = item.getId();
					toolItem.setElementId(itemId == null ? id : itemId);
					return toolItem;
				}
			}
		} else {
			final MDirectToolItem toolItem = MenuFactoryImpl.eINSTANCE.createDirectToolItem();
			String itemId = item.getId();
			toolItem.setElementId(itemId);
			toolItem.setVisible(item.isVisible());
			String iconURI = getIconURI(action.getImageDescriptor(), application.getContext());
			if (iconURI == null) {
				if (itemId == null) {
					if (action.getText() != null) {
						toolItem.setLabel(action.getText());
					}
				} else {
					iconURI = getIconURI(itemId, application.getContext(),
							ICommandImageService.TYPE_DEFAULT);
					if (iconURI == null) {
						if (action.getText() != null) {
							toolItem.setLabel(action.getText());
						}
					} else {
						toolItem.setIconURI(iconURI);
					}
				}
			} else {
				toolItem.setIconURI(iconURI);
			}
			if (action.getToolTipText() != null) {
				toolItem.setTooltip(action.getToolTipText());
			}

			switch (action.getStyle()) {
			case IAction.AS_CHECK_BOX:
				toolItem.setType(ItemType.CHECK);
				toolItem.setSelected(action.isChecked());
				break;
			case IAction.AS_RADIO_BUTTON:
				toolItem.setType(ItemType.RADIO);
				toolItem.setSelected(action.isChecked());
				break;
			default:
				toolItem.setType(ItemType.PUSH);
				break;
			}
			toolItem.setContributionURI("bundleclass://org.eclipse.ui.workbench/programmic.contribution"); //$NON-NLS-1$
			toolItem.setObject(new DirectProxy(action));
			toolItem.setEnabled(action.isEnabled());

			final IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					String property = event.getProperty();
					if (property.equals(IAction.ENABLED)) {
						toolItem.setEnabled(action.isEnabled());
					} else if (property.equals(IAction.CHECKED)) {
						toolItem.setSelected(action.isChecked());
					} else if (property.equals(IAction.TEXT)) {
						toolItem.setLabel(action.getText());
					} else if (property.equals(IAction.TOOL_TIP_TEXT)) {
						toolItem.setLabel(action.getToolTipText());
					}
				}
			};
			// property listener is removed in
			// DirectContributionItem#handleWidgetDispose()
			action.addPropertyChangeListener(propertyListener);
			toolItem.getTransientData().put(DirectContributionItem.DISPOSABLE, new Runnable() {
						@Override
						public void run() {
							action.removePropertyChangeListener(propertyListener);
						}
					});
			return toolItem;
		}
		return null;
	}

	public static MMenuItem createItem(MApplication application, ActionContributionItem item) {
		IAction action = item.getAction();
		String id = action.getActionDefinitionId();
		if (action instanceof OpenPreferencesAction) {
			for (MCommand command : application.getCommands()) {
				if (IWorkbenchCommandConstants.WINDOW_PREFERENCES.equals(command.getElementId())) {
					MHandledMenuItem menuItem = MenuFactoryImpl.eINSTANCE.createHandledMenuItem();
					menuItem.setCommand(command);
					menuItem.setLabel(command.getCommandName());
					menuItem.setIconURI(getIconURI(action.getImageDescriptor(),
							application.getContext()));

					// extract the mnemonic definition
					String text = action.getText();
					int index = text.indexOf('&');
					if (index != -1 && index != text.length() - 1) {
						menuItem.setMnemonics(text.substring(index + 1, index + 2));
					}

					switch (action.getStyle()) {
					case IAction.AS_CHECK_BOX:
						menuItem.setType(ItemType.CHECK);
						menuItem.setSelected(action.isChecked());
						break;
					case IAction.AS_RADIO_BUTTON:
						menuItem.setType(ItemType.RADIO);
						menuItem.setSelected(action.isChecked());
						break;
					default:
						menuItem.setType(ItemType.PUSH);
						break;
					}

					String itemId = item.getId();
					menuItem.setElementId(itemId == null ? id : itemId);
					return menuItem;
				}
			}
		} else if (id != null) {

			for (MCommand command : application.getCommands()) {
				if (id.equals(command.getElementId())) {
					MHandledMenuItem menuItem = MenuFactoryImpl.eINSTANCE.createHandledMenuItem();
					menuItem.setCommand(command);
					if (action.getText() != null) {
						menuItem.setLabel(action.getText());
					} else {
						menuItem.setLabel(command.getCommandName());
					}
					menuItem.setIconURI(getIconURI(action.getImageDescriptor(),
							application.getContext()));

					switch (action.getStyle()) {
					case IAction.AS_CHECK_BOX:
						menuItem.setType(ItemType.CHECK);
						menuItem.setSelected(action.isChecked());
						break;
					case IAction.AS_RADIO_BUTTON:
						menuItem.setType(ItemType.RADIO);
						menuItem.setSelected(action.isChecked());
						break;
					default:
						menuItem.setType(ItemType.PUSH);
						break;
					}

					String itemId = item.getId();
					menuItem.setElementId(itemId == null ? id : itemId);
					return menuItem;
				}
			}
		} else {
			MDirectMenuItem menuItem = MenuFactoryImpl.eINSTANCE.createDirectMenuItem();
			if (action.getText() != null) {
				menuItem.setLabel(action.getText());
			}
			String itemId = item.getId();
			menuItem.setElementId(itemId == null ? id : itemId);
			menuItem.setIconURI(getIconURI(action.getImageDescriptor(), application.getContext()));
			switch (action.getStyle()) {
			case IAction.AS_CHECK_BOX:
				menuItem.setType(ItemType.CHECK);
				menuItem.setSelected(action.isChecked());
				break;
			case IAction.AS_RADIO_BUTTON:
				menuItem.setType(ItemType.RADIO);
				menuItem.setSelected(action.isChecked());
				break;
			default:
				menuItem.setType(ItemType.PUSH);
				break;
			}
			menuItem.setContributionURI("bundleclass://org.eclipse.ui.workbench/programmic.contribution"); //$NON-NLS-1$
			menuItem.setObject(new DirectProxy(action));
			return menuItem;
		}
		return null;
	}

	static class DirectProxy {
		private IAction action;

		public DirectProxy(IAction action) {
			this.action = action;
		}

		@CanExecute
		public boolean canExecute() {
			return action.isEnabled();
		}

		@Execute
		public void execute() {
			action.run();
		}
	}

	public static String getIconURI(ImageDescriptor descriptor, IEclipseContext context) {
		if (descriptor == null) {
			return null;
		}

		// Attempt to retrieve URIs from the descriptor and convert into a more
		// durable form in case it's to be persisted
		if (descriptor.getClass().toString().endsWith("URLImageDescriptor")) { //$NON-NLS-1$
			URL url = getUrl(descriptor.getClass(), descriptor);
			return rewriteDurableURL(url.toExternalForm());
		} else if (descriptor.getClass().toString().endsWith("FileImageDescriptor")) { //$NON-NLS-1$
			Class<?> sourceClass = getLocation(descriptor);
			if (sourceClass == null) {
				return null;
			}

			String path = getName(descriptor);
			if (path == null) {
				return null;
			}

			Bundle bundle = FrameworkUtil.getBundle(sourceClass);
			// get the fully qualified class name
			String parentPath = sourceClass.getName();
			// remove the class's name
			parentPath = parentPath.substring(0, parentPath.lastIndexOf('.'));
			// swap '.' with '/' so that it becomes a path
			parentPath = parentPath.replace('.', '/');

			// construct the URL
			URL url = FileLocator.find(bundle, new Path(parentPath).append(path), null);
			return url == null ? null : rewriteDurableURL(url.toString());
		} else {
			if (descriptor instanceof IAdaptable) {
				Object o = ((IAdaptable) descriptor).getAdapter(URL.class);
				if (o != null) {
					return rewriteDurableURL(o.toString());
				}
				o = ((IAdaptable) descriptor).getAdapter(URI.class);
				if (o != null) {
					return rewriteDurableURL(o.toString());
				}
			} else if (context != null) {
				IAdapterManager adapter = context.get(IAdapterManager.class);
				if (adapter != null) {
					Object o = adapter.getAdapter(descriptor, URL.class);
					if (o != null) {
						return rewriteDurableURL(o.toString());
					}
					o = adapter.getAdapter(descriptor, URI.class);
					if (o != null) {
						return rewriteDurableURL(o.toString());
					}
				}
			}
		}
		return null;
	}

	/**
	 * Rewrite certain types of URLs to more durable forms, as these URLs may
	 * may be persisted in the model.
	 * 
	 * @param url
	 *            the url
	 * @return the rewritten URL
	 */
	private static String rewriteDurableURL(String url) {
		// Rewrite bundleentry and bundleresource entries as they are
		// invalidated on -clean or a bundle remove, . These Platform URIs are
		// of the form:
		// bundleentry://<bundle-id>.XXX/path/to/file
		// bundleresource://<bundle-id>.XXX/path/to/file
		if (!url.startsWith("bundleentry:") && !url.startsWith("bundleresource:")) { //$NON-NLS-1$ //$NON-NLS-2$
			return url;
		}

		BundleContext ctxt = FrameworkUtil.getBundle(WorkbenchWindow.class).getBundleContext();
		try {
			URI uri = new URI(url);
			String host = uri.getHost();
			String bundleId = host.substring(0, host.indexOf('.'));
			Bundle bundle = ctxt.getBundle(Long.parseLong(bundleId));
			StringBuilder builder = new StringBuilder("platform:/plugin/"); //$NON-NLS-1$
			builder.append(bundle.getSymbolicName());
			builder.append(uri.getPath());
			return builder.toString();
		} catch (URISyntaxException e) {
			return url;
		}
	}

	private static String getIconURI(String commandId, IEclipseContext workbench, int type) {
		if (commandId == null) {
			return null;
		}

		ICommandImageService imageService = workbench.get(ICommandImageService.class);
		ImageDescriptor descriptor = imageService.getImageDescriptor(commandId, type);
		return getIconURI(descriptor, workbench);
	}

	/**
	 * @param item
	 * @param disabledIconURI
	 */
	public static void setDisabledIconURI(MToolItem item, String disabledIconURI) {
		item.getTransientData().put(IPresentationEngine.DISABLED_ICON_IMAGE_KEY, disabledIconURI);
	}
}
