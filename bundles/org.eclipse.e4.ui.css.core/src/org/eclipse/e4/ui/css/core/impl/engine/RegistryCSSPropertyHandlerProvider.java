/*******************************************************************************
 * Copyright (c) 2012, 2015 Brian de Alwis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis - cobbled together from other sources
 *     Angelo Zerr <angelo.zerr@gmail.com> - likely source of initial implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.core.internal.runtime.RuntimeLog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.dom.properties.providers.AbstractCSSPropertyHandlerProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

public class RegistryCSSPropertyHandlerProvider extends
		AbstractCSSPropertyHandlerProvider {
	private static final String ATTR_COMPOSITE = "composite";
	private static final String ATTR_ADAPTER = "adapter";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_PROPERTY_NAME = "property-name";
	private static final String ATTR_HANDLER = "handler";
	private static final String ATTR_DEPRECATED = "deprecated";

	private static final String PROPERTY_HANDLERS_EXTPOINT = "org.eclipse.e4.ui.css.core.propertyHandler";

	/* the handlers extension point was originally in .swt */
	private static final String DEPRECATED_PROPERTY_HANDLERS_EXTPOINT = "org.eclipse.e4.ui.css.swt.property.handler";

	private IExtensionRegistry registry;
	private boolean hasDeprecatedProperties = false; // mild optimization for
														// getCSSProperties()

	private Map<String, Map<String, ICSSPropertyHandler>> propertyHandlerMap = new HashMap<String, Map<String, ICSSPropertyHandler>>();;

	public RegistryCSSPropertyHandlerProvider(IExtensionRegistry registry) {
		this.registry = registry;
		if (configure(DEPRECATED_PROPERTY_HANDLERS_EXTPOINT)) {
			System.err.println("Extension point "
					+ DEPRECATED_PROPERTY_HANDLERS_EXTPOINT
					+ " is deprecated; use " + PROPERTY_HANDLERS_EXTPOINT);
		}
		configure(PROPERTY_HANDLERS_EXTPOINT);
	}

	public RegistryCSSPropertyHandlerProvider(IExtensionRegistry registry,
			String extensionPointId) {
		this.registry = registry;
		// FIXME: should install a registry listener to make this dynamic
		configure(extensionPointId);
	}

	/** @return true if some extensions were found */
	protected boolean configure(String extensionPointId) {
		IExtensionPoint extPoint = registry.getExtensionPoint(extensionPointId);
		if (extPoint == null) {
			return false;
		}
		IExtension[] extensions = extPoint.getExtensions();
		if (extensions.length == 0) {
			return false;
		}
		Map<String, Map<String, ICSSPropertyHandler>> handlersMap = new HashMap<String, Map<String, ICSSPropertyHandler>>();
		for (IExtension e : extensions) {
			for (IConfigurationElement ce : e.getConfigurationElements()) {
				if (ce.getName().equals(ATTR_HANDLER)) {
					// a single handler may implement a number of properties
					String name = ce.getAttribute(ATTR_COMPOSITE);
					String adapter = ce.getAttribute(ATTR_ADAPTER);
					// if (className.equals(adapter)) {
					IConfigurationElement[] children = ce.getChildren();
					String[] names = new String[children.length];
					String[] deprecated = new String[children.length];
					for (int i = 0; i < children.length; i++) {
						if (children[i].getName().equals(ATTR_PROPERTY_NAME)) {
							names[i] = children[i].getAttribute(ATTR_NAME);
							deprecated[i] = children[i]
									.getAttribute(ATTR_DEPRECATED);
							if (deprecated[i] != null) {
								hasDeprecatedProperties = true;
							}
						}

					}
					try {
						Map<String, ICSSPropertyHandler> adaptersMap = handlersMap
								.get(adapter);
						if (adaptersMap == null) {
							handlersMap
									.put(adapter,
											adaptersMap = new HashMap<String, ICSSPropertyHandler>());
						}
						if (!adaptersMap.containsKey(name)) {
							Object t = ce
									.createExecutableExtension(ATTR_HANDLER);
							if (t instanceof ICSSPropertyHandler) {
								for (int i = 0; i < names.length; i++) {
									adaptersMap
											.put(names[i],
													deprecated[i] == null ? (ICSSPropertyHandler) t
															: new DeprecatedPropertyHandlerWrapper(
																	(ICSSPropertyHandler) t,
																	deprecated[i]));
								}
							} else {
								logError("invalid property handler for " + name);
							}
						}
					} catch (CoreException e1) {
						logError("invalid property handler for " + name + ": "
								+ e1);
					}
				}
			}
		}
		propertyHandlerMap.putAll(handlersMap);
		return true;
	}

	@Override
	public Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
			String property) throws Exception {
		List<ICSSPropertyHandler> handlers = new ArrayList<ICSSPropertyHandler>();
		for (Map<String, ICSSPropertyHandler> perElement : propertyHandlerMap
				.values()) {
			ICSSPropertyHandler h = perElement.get(property);
			if (h != null) {
				handlers.add(h);
			}
		}
		return handlers;
	}

	@Override
	protected CSSStyleDeclaration getDefaultCSSStyleDeclaration(
			CSSEngine engine, CSSStylableElement stylableElement,
			CSSStyleDeclaration newStyle, String pseudoE) throws Exception {
		if (stylableElement.getDefaultStyleDeclaration(pseudoE) != null)
			return stylableElement.getDefaultStyleDeclaration(pseudoE);
		if (newStyle != null) {
			StringBuffer style = null;
			int length = newStyle.getLength();
			for (int i = 0; i < length; i++) {
				String propertyName = newStyle.item(i);
				String[] compositePropertiesNames = engine
						.getCSSCompositePropertiesNames(propertyName);
				if (compositePropertiesNames != null) {
					for (int j = 0; j < compositePropertiesNames.length; j++) {
						propertyName = compositePropertiesNames[j];
						String s = getCSSPropertyStyle(engine, stylableElement,
								propertyName, pseudoE);
						if (s != null) {
							if (style == null)
								style = new StringBuffer();
							style.append(s);
						}
					}
				} else {
					String s = getCSSPropertyStyle(engine, stylableElement,
							propertyName, pseudoE);
					if (s != null) {
						if (style == null)
							style = new StringBuffer();
						style.append(s);
					}
				}
			}
			if (style != null) {
				CSSStyleDeclaration defaultStyleDeclaration = engine
						.parseStyleDeclaration(style.toString());
				stylableElement.setDefaultStyleDeclaration(pseudoE,
						defaultStyleDeclaration);
				return defaultStyleDeclaration;
			}
		}
		return stylableElement.getDefaultStyleDeclaration(pseudoE);
	}

	@Override
	public Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
			Object element, String property) throws Exception {
		List<ICSSPropertyHandler> handlers = new ArrayList<ICSSPropertyHandler>();
		Class<?> clazz = element.getClass();
		while (clazz != Object.class) {
			if (propertyHandlerMap.containsKey(clazz.getName())) {
				ICSSPropertyHandler handler = propertyHandlerMap.get(
						clazz.getName()).get(property);
				if (handler != null) {
					handlers.add(handler);
				}
			}
			clazz = clazz.getSuperclass();
		}
		return handlers;
	}

	@Override
	public Collection<String> getCSSProperties(Object element) {
		// don't include deprecated elements
		Set<String> properties = new HashSet<String>();
		Class<?> clazz = element.getClass();
		while (clazz != Object.class) {
			Map<String, ICSSPropertyHandler> handlerMap = propertyHandlerMap
					.get(clazz.getName());
			if (handlerMap != null) {
				if (!hasDeprecatedProperties) {
					properties.addAll(handlerMap.keySet());
				} else {
					for (Entry<String, ICSSPropertyHandler> entry : handlerMap
							.entrySet()) {
						if (!(entry.getValue() instanceof DeprecatedPropertyHandlerWrapper)) {
							properties.add(entry.getKey());
						}
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		return properties;
	}

	protected void logError(String message) {
		// we log as an error to ensure it's shown
		RuntimeLog.log(new Status(IStatus.ERROR, "org.eclipse.e4.ui.css.core",
				message));
	}

	private class DeprecatedPropertyHandlerWrapper implements
			ICSSPropertyHandler {
		private ICSSPropertyHandler delegate;
		private String message;
		private Set<String> logged = new HashSet<String>();

		DeprecatedPropertyHandlerWrapper(ICSSPropertyHandler handler,
				String message) {
			delegate = handler;
			this.message = message;
		}

		@Override
		public boolean applyCSSProperty(Object element, String property,
				CSSValue value, String pseudo, CSSEngine engine)
				throws Exception {
			logIfNecessary(property);
			return delegate.applyCSSProperty(element, property, value, pseudo,
					engine);
		}

		@Override
		public String retrieveCSSProperty(Object element, String property,
				String pseudo, CSSEngine engine) throws Exception {
			logIfNecessary(property);
			return delegate.retrieveCSSProperty(element, property, pseudo,
					engine);
		}

		private void logIfNecessary(String property) {
			if (!logged.contains(property)) {
				logged.add(property);
				logError("CSS property '" + property
						+ "' has been deprecated: " + message);
			}
		}
	}
}