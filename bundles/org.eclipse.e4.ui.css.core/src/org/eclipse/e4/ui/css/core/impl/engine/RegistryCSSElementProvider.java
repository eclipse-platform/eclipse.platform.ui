/*******************************************************************************
 * Copyright (c) 2014, 2021 Manumitting Technologies Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Element;

/**
 * A simple {@link IElementProvider} that is configured by an extension point.
 */
public class RegistryCSSElementProvider implements IElementProvider {
	private static final String ELEMENT_PROVIDER_EXTPOINT = "org.eclipse.e4.ui.css.core.elementProvider";

	final private IExtensionRegistry registry;

	private Map<Class<?>, IElementProvider> providerCache = Collections
			.synchronizedMap(new WeakHashMap<>());

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
		if (providerCache.containsKey(o.getClass())) {
			return null;
		}
		for (Class<?> type : computeElementTypeLookup(o.getClass())) {
			String typeName = type.getName();
			for (IConfigurationElement ce : registry.getConfigurationElementsFor(ELEMENT_PROVIDER_EXTPOINT)) {
				if ("provider".equals(ce.getName())) {
					for (IConfigurationElement ce2 : ce.getChildren()) {
						if (typeName.equals(ce2.getAttribute("class"))) {
							try {
								provider = (IElementProvider) ce.createExecutableExtension("class");
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
		providerCache.put(o.getClass(), null);
		return null;
	}

	private List<Class<?>> computeElementTypeLookup(Class<?> clazz) {
		ArrayList<Class<?>> results = new ArrayList<>();
		LinkedList<Class<?>> todo = new LinkedList<>();
		Set<Class<?>> seen = new HashSet<>();
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
