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
	private static final String PROPERTY_HANDLERS_EXTENSION_POINT = "org.eclipse.e4.ui.css.swt.property.handler";

	private IExtensionRegistry registry;
	private boolean hasDeprecatedProperties = false; // mild optimization

	private Map<String, Map<String, ICSSPropertyHandler>> propertyHandlerMap;

	public RegistryCSSPropertyHandlerProvider(IExtensionRegistry registry) {
		this.registry = registry;

		initialize();
	}

	public void initialize() {
		Map<String, Map<String, ICSSPropertyHandler>> handlersMap = new HashMap<String, Map<String, ICSSPropertyHandler>>();

		IExtensionPoint extPoint = registry
				.getExtensionPoint(PROPERTY_HANDLERS_EXTENSION_POINT);
		for (IExtension e : extPoint.getExtensions()) {
			for (IConfigurationElement ce : e.getConfigurationElements()) {
				if (ce.getName().equals("handler")) {
					String name = ce.getAttribute("composite");
					String adapter = ce.getAttribute("adapter");
					// if (className.equals(adapter)) {
					IConfigurationElement[] children = ce.getChildren();
					String[] names = new String[children.length];
					boolean[] deprecated = new boolean[children.length];
					for (int i = 0; i < children.length; i++) {
						if (children[i].getName().equals("property-name")) {
							names[i] = children[i].getAttribute("name");
							deprecated[i] = Boolean.valueOf(children[i]
									.getAttribute("deprecated"));
							if (deprecated[i]) {
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
							Object t = ce.createExecutableExtension("handler");
							if (t instanceof ICSSPropertyHandler) {
								for (int i = 0; i < names.length; i++) {
									adaptersMap.put(names[i],
													deprecated[i] ? new DeprecatedPropertyHandlerWrapper(
															(ICSSPropertyHandler) t)
															: (ICSSPropertyHandler) t);
								}
							} else {
								System.err
										.println("invalid property handler for "
												+ name + "");
							}
						}
					} catch (CoreException e1) {
					}
				}
			}
		}
		propertyHandlerMap = handlersMap;
	}

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.dom.properties.providers.
	 * AbstractCSSPropertyHandlerProvider
	 * #getDefaultCSSStyleDeclaration(org.eclipse
	 * .e4.ui.css.core.engine.CSSEngine,
	 * org.eclipse.e4.ui.css.core.dom.CSSStylableElement,
	 * org.w3c.dom.css.CSSStyleDeclaration)
	 */
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

	private static class DeprecatedPropertyHandlerWrapper implements
			ICSSPropertyHandler {
		private ICSSPropertyHandler delegate;
		private Set<String> logged = new HashSet<String>();

		DeprecatedPropertyHandlerWrapper(ICSSPropertyHandler handler) {
			delegate = handler;
		}

		public boolean applyCSSProperty(Object element, String property,
				CSSValue value, String pseudo, CSSEngine engine)
				throws Exception {
			logIfNecessary(property);
			return delegate.applyCSSProperty(element, property, value, pseudo,
					engine);
		}

		public String retrieveCSSProperty(Object element, String property,
				String pseudo, CSSEngine engine) throws Exception {
			logIfNecessary(property);
			return delegate.retrieveCSSProperty(element, property, pseudo,
					engine);
		}

		private void logIfNecessary(String property) {
			if (!logged.contains(property)) {
				logged.add(property);
				RuntimeLog.log(new Status(IStatus.ERROR,
						"org.eclipse.e4.ui.css.core",
						"CSS property has been deprecated: " + property));
			}
		}

	}
}