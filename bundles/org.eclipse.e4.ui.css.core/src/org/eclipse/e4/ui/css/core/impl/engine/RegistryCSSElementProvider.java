/*******************************************************************************
 * Copyright (c) 2014 Manumitting Technologies Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis (MTI) - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Element;

/**
 * A simple {@link IElementProvider} that is configured by an extension point.
 */
public class RegistryCSSElementProvider implements IElementProvider {
	/* the original extension point was misspelled */
	private static final String DEPRECATED_ELEMENT_PROVIDER_EXTPOINT = "org.eclipse.e4.u.css.core.elementProvider";
	private static final String ELEMENT_PROVIDER_EXTPOINT = "org.eclipse.e4.ui.css.core.elementProvider";

	final private IExtensionRegistry registry;;

	private String[] extpts = { ELEMENT_PROVIDER_EXTPOINT,
			DEPRECATED_ELEMENT_PROVIDER_EXTPOINT };

	private Map<Class<?>, IElementProvider> providerCache = Collections
			.synchronizedMap(new WeakHashMap<Class<?>, IElementProvider>());

	public RegistryCSSElementProvider() {
		this(RegistryFactory.getRegistry());
	}

	public RegistryCSSElementProvider(IExtensionRegistry registry) {
		// FIXME: add a registry listener to refresh caches; but would need to
		// add a dispose() to IElementProvider
		this.registry = registry;
	}

	@Override
	public Element getElement(Object o, CSSEngine engine) {
		if (o instanceof Element) {
			return (Element) o;
		}
		IElementProvider provider = providerCache.get(o.getClass());
		if (provider != null) {
			return provider.getElement(o, engine);
		}
		for (Class<?> type : computeElementTypeLookup(o.getClass())) {
			String typeName = type.getName();
			for (String extpt : extpts) {
				for (IConfigurationElement ce : registry
						.getConfigurationElementsFor(extpt)) {
					if ("provider".equals(ce.getName())) {
						for (IConfigurationElement ce2 : ce.getChildren()) {
							if (typeName.equals(ce2.getAttribute("class"))) {
								try {
									if (extpt
											.equals(DEPRECATED_ELEMENT_PROVIDER_EXTPOINT)) {
										System.err
										.println("Extension point "
												+ DEPRECATED_ELEMENT_PROVIDER_EXTPOINT
												+ " is deprecated; use "
												+ ELEMENT_PROVIDER_EXTPOINT);
									}
									provider = (IElementProvider) ce
											.createExecutableExtension("class");
									providerCache.put(o.getClass(), provider);
									return provider.getElement(o, engine);
								} catch (CoreException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	private List<Class<?>> computeElementTypeLookup(Class<?> clazz) {
		ArrayList<Class<?>> results = new ArrayList<Class<?>>();
		LinkedList<Class<?>> todo = new LinkedList<Class<?>>();
		Set<Class<?>> seen = new HashSet<Class<?>>();
		todo.add(clazz);
		while (!todo.isEmpty()) {
			Class<?> candidate = todo.removeFirst();
			if (!seen.contains(candidate)) {
				seen.add(candidate);
				results.add(candidate);
				Collections.addAll(todo, candidate.getInterfaces());
				if (candidate.getSuperclass() != null) {
					todo.add(candidate.getSuperclass());
				}
			}
		}
		return results;
	}

}
