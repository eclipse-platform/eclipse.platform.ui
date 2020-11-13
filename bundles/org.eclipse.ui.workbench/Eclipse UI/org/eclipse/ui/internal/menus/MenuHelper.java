/*******************************************************************************
 * Copyright (c) 2010, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 180308, 472654
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
import java.util.function.Supplier;
import java.util.regex.Pattern;
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
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.Policy;
import org.eclipse.e4.ui.internal.workbench.swt.WorkbenchSWTActivator;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MExpression;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class MenuHelper {

	public static void trace(String msg, Throwable error) {
		WorkbenchSWTActivator.trace(Policy.DEBUG_MENUS_FLAG, msg, error);
	}

	private static final Pattern SCHEME_PATTERN = Pattern.compile("\\p{Alpha}[\\p{Alnum}+.-]*:.*"); //$NON-NLS-1$
	private static Field urlField;
	private static Field urlSupplierField;

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

	public static String getImageUrl(ImageDescriptor imageDescriptor) {
		return getIconURI(imageDescriptor, null);
	}

	private static String getUrl(Class<? extends ImageDescriptor> idc, ImageDescriptor imageDescriptor) {
		try {
			if (urlField == null) {
				urlField = idc.getDeclaredField("url"); //$NON-NLS-1$
				urlField.setAccessible(true);
			}
			Object value = urlField.get(imageDescriptor);
			if (value != null) {
				return value.toString();
			}
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			WorkbenchPlugin.log(e);
		}
		return null;
	}

	private static String getUrlSupplier(Class<? extends ImageDescriptor> idc, ImageDescriptor imageDescriptor) {
		try {
			if (urlSupplierField == null) {
				urlSupplierField = idc.getDeclaredField("supplier"); //$NON-NLS-1$
				urlSupplierField.setAccessible(true);
			}
			Object value = urlSupplierField.get(imageDescriptor);
			if (value != null && value instanceof Supplier) {
				@SuppressWarnings("unchecked")
				Supplier<URL> supplier = (Supplier<URL>) value;
				URL url = supplier.get();
				return url == null ? null : url.toString();
			}
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			WorkbenchPlugin.log(e);
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
		} catch (SecurityException | NoSuchFieldException | IllegalAccessException e) {
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
		} catch (SecurityException | NoSuchFieldException | IllegalAccessException e) {
			WorkbenchPlugin.log(e);
		}
		return null;
	}

	static MExpression getVisibleWhen(final IConfigurationElement commandAddition) {
		try {
			IConfigurationElement[] visibleConfig = commandAddition
					.getChildren(IWorkbenchRegistryConstants.TAG_VISIBLE_WHEN);
			if (visibleConfig.length > 0 && visibleConfig.length < 2) {
				IConfigurationElement[] visibleChild = visibleConfig[0].getChildren();
				if (visibleChild.length == 0) {
					String checkEnabled = visibleConfig[0].getAttribute(IWorkbenchRegistryConstants.ATT_CHECK_ENABLED);
					if (Boolean.parseBoolean(checkEnabled)) {
						final String commandId = getCommandId(commandAddition);
						if (commandId == null) {
							return null;
						}

						Expression visWhen = new Expression() {
							@Override
							public EvaluationResult evaluate(IEvaluationContext context) {
								EHandlerService service = getFromContext(context, EHandlerService.class);
								ICommandService commandService = getFromContext(context, ICommandService.class);
								if (service == null || commandService == null) {
									WorkbenchPlugin.log(
											"Could not retrieve EHandlerService or ICommandService from context evaluation context for" //$NON-NLS-1$
													+ commandId);
									return EvaluationResult.FALSE;
								}
								Command c = commandService.getCommand(commandId);
								ParameterizedCommand generateCommand = ParameterizedCommand.generateCommand(c,
										Collections.EMPTY_MAP);
								return EvaluationResult.valueOf(service.canExecute(generateCommand));
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
		} catch (InvalidRegistryObjectException | CoreException e) {
			// visWhenMap.put(configElement, null);
			WorkbenchPlugin.log(e);
		}
		return null;
	}

	/**
	 * Do a type-safe extraction of an object from the evalation context
	 *
	 * @param context      the evaluation context
	 * @param expectedType the expected type
	 * @return an object of the expected type or <code>null</code>
	 * @throws NullPointerException if either argument is <code>null</code>
	 */
	protected static <T> T getFromContext(IEvaluationContext context, Class<T> expectedType) {
		if (context == null || expectedType == null) {
			throw new NullPointerException();
		}
		final Object rawValue = context.getVariable(expectedType.getName());
		return (expectedType.isInstance(rawValue)) ? expectedType.cast(rawValue) : null;
	}

	/**
	 * Returns id attribute of the element or unique string computed from the
	 * element registry handle
	 *
	 * @param element non null
	 * @return non null id
	 */
	public static String getId(IConfigurationElement element) {
		String id = element.getAttribute(IWorkbenchRegistryConstants.ATT_ID);

		// For sub-menu management -all- items must be id'd so enforce this
		// here (we could optimize by checking the 'name' of the config
		// element == "menu"
		if (id == null || id.isEmpty()) {
			id = getCommandId(element);
		}
		if (id == null || id.isEmpty()) {
			id = getConfigurationHandleId(element);
		}
		return id;
	}

	/**
	 * @return unique string computed from the element registry handle
	 */
	private static String getConfigurationHandleId(IConfigurationElement element) {
		return String.valueOf(element.getHandleId());
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

	public static String getIconURI(IConfigurationElement element, String attr) {
		String iconPath = element.getAttribute(attr);
		if (iconPath == null) {
			return null;
		}

		// If iconPath doesn't specify a scheme, then try to transform to a URL
		// RFC 3986: scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
		// This allows using data:, http:, or other custom URL schemes
		if (!SCHEME_PATTERN.matcher(iconPath).matches()) {
			// First attempt to resolve in ISharedImages (e.g. "IMG_OBJ_FOLDER")
			// as per bug 391232 & AbstractUIPlugin.imageDescriptorFromPlugin().
			ImageDescriptor d = WorkbenchPlugin.getDefault().getSharedImages().getImageDescriptor(iconPath);
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
		return Boolean.parseBoolean(val);
	}

	public static String getCommandId(IConfigurationElement element) {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_COMMAND_ID);
	}

	public static ItemType getStyle(IConfigurationElement element) {
		String style = element.getAttribute(IWorkbenchRegistryConstants.ATT_STYLE);
		if (style == null || style.isEmpty()) {
			return ItemType.PUSH;
		}
		if (IWorkbenchRegistryConstants.STYLE_TOGGLE.equals(style)) {
			return ItemType.CHECK;
		}
		if (IWorkbenchRegistryConstants.STYLE_RADIO.equals(style)) {
			return ItemType.RADIO;
		}
		if (IWorkbenchRegistryConstants.STYLE_PULLDOWN.equals(style)) {
			if (Policy.DEBUG_MENUS) {
				trace("Failed to get style for " + IWorkbenchRegistryConstants.STYLE_PULLDOWN, null); //$NON-NLS-1$
				// return CommandContributionItem.STYLE_PULLDOWN;
			}
		}
		return ItemType.PUSH;
	}

	public static boolean hasPulldownStyle(IConfigurationElement element) {
		String style = element.getAttribute(IWorkbenchRegistryConstants.ATT_STYLE);
		return IWorkbenchRegistryConstants.STYLE_PULLDOWN.equals(style);
	}

	public static Map<String, String> getParameters(IConfigurationElement element) {
		HashMap<String, String> map = new HashMap<>();
		IConfigurationElement[] parameters = element.getChildren(IWorkbenchRegistryConstants.TAG_PARAMETER);
		for (IConfigurationElement parameter : parameters) {
			String name = parameter.getAttribute(IWorkbenchRegistryConstants.ATT_NAME);
			String value = parameter.getAttribute(IWorkbenchRegistryConstants.ATT_VALUE);
			if (name != null && value != null) {
				map.put(name, value);
			}
		}
		return map;
	}

	public static MMenu createMenuAddition(IConfigurationElement menuAddition) {
		MMenu element = MenuFactoryImpl.eINSTANCE.createMenu();
		element.getPersistedState().put(IWorkbench.PERSIST_STATE, Boolean.FALSE.toString());
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
		element.setIconURI(MenuHelper.getIconURI(menuAddition, IWorkbenchRegistryConstants.ATT_ICON));
		element.setLabel(Util.safeString(text));

		return element;
	}

	public static MMenu createMenu(MenuManager manager) {
		MMenu subMenu = MenuFactoryImpl.eINSTANCE.createMenu();
		subMenu.setLabel(manager.getMenuText());
		subMenu.setElementId(manager.getId());
		return subMenu;
	}

	public static MHandledToolItem createToolItem(MApplication application, CommandContributionItem cci) {
		MCommand command = getMCommand(application, cci);
		if (command != null) {
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
				iconURI = getIconURI(command.getElementId(), application.getContext(),
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
				disabledIconURI = getIconURI(command.getElementId(), application.getContext(),
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
			toolItem.setElementId(itemId == null ? command.getElementId() : itemId);
			return toolItem;
		}
		return null;
	}

	public static MCommand getMCommand(MApplication application, CommandContributionItem contribution) {
		ParameterizedCommand command = contribution.getCommand();
		if (command != null) {
			for (MCommand mcommand : application.getCommands()) {
				if (mcommand.getElementId().equals(command.getId())) {
					return mcommand;
				}
			}
		}
		return null;
	}

	public static String getIconURI(ImageDescriptor descriptor, IEclipseContext context) {
		if (descriptor == null) {
			return null;
		}

		// Attempt to retrieve URIs from the descriptor and convert into a more
		// durable form in case it's to be persisted
		if (descriptor.getClass().toString().endsWith("URLImageDescriptor")) { //$NON-NLS-1$
			String url = getUrl(descriptor.getClass(), descriptor);
			return rewriteDurableURL(url);
		} else if (descriptor.getClass().toString().endsWith("DeferredImageDescriptor")) { //$NON-NLS-1$
			String url = getUrlSupplier(descriptor.getClass(), descriptor);
			return rewriteDurableURL(url);
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
		} else if (descriptor instanceof IAdaptable) {
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
		return null;
	}

	/**
	 * Rewrite certain types of URLs to more durable forms, as these URLs may may be
	 * persisted in the model.
	 *
	 * @param url the url
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
