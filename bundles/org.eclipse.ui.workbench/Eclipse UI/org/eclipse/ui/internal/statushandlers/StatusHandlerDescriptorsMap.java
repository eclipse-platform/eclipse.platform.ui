/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.statushandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class supporting the prefix based status handling policy.
 * 
 * @since 3.3
 */
class StatusHandlerDescriptorsMap {

	private final String ASTERISK = "*"; //$NON-NLS-1$

	private HashMap map;

	/**
	 * Creates a new instance of the class
	 */
	public StatusHandlerDescriptorsMap() {
		map = new HashMap();
	}

	/**
	 * Adds a new handler descriptor to the prefix tree
	 * 
	 * @param handlerDescriptor
	 *            the handler descriptor to add
	 */
	public void addHandlerDescriptor(StatusHandlerDescriptor handlerDescriptor) {
		add(this.map, handlerDescriptor.getPrefix(), handlerDescriptor);
	}

	/*
	 * Recursively searches the tree for the best place for the handler
	 * descriptor
	 */
	private void add(Map map, String prefix,
			StatusHandlerDescriptor handlerDescriptor) {
		if (prefix == null) {
			if (map.get(ASTERISK) == null) {
				map.put(ASTERISK, new ArrayList());
			}

			((List) map.get(ASTERISK)).add(handlerDescriptor);
		} else {
			int delimIndex = prefix.indexOf("."); //$NON-NLS-1$

			String pre = null;
			String post = null;

			if (delimIndex != -1) {
				pre = prefix.substring(0, delimIndex);

				if (delimIndex < prefix.length() - 1) {
					post = prefix.substring(delimIndex + 1);
				}
			} else {
				pre = prefix;
			}

			if (map.get(pre) == null) {
				map.put(pre, new HashMap());
			}

			add((Map) map.get(pre), post, handlerDescriptor);
		}
	}

	public void clear() {
		map.clear();
	}

	/**
	 * Returns status handler descriptors whose prefixes are the most specific
	 * for given pluginId.
	 * 
	 * @param pluginId
	 * @return handler descriptors list
	 */
	public List getHandlerDescriptors(String pluginId) {
		return get(pluginId, this.map);
	}

	/*
	 * Recursively searches the prefix tree for the most specific handler
	 * descriptor for the given pluginId.
	 */
	private List get(String pluginId, Map map) {
		if (pluginId == null) {
			return getAsteriskList(map);
		}

		int delimIndex = pluginId.indexOf("."); //$NON-NLS-1$

		String pre = null;
		String post = null;

		if (delimIndex != -1) {
			pre = pluginId.substring(0, delimIndex);

			if (delimIndex < pluginId.length() - 1) {
				post = pluginId.substring(delimIndex + 1);
			}
		} else {
			pre = pluginId;
		}

		if (map.get(pre) == null) {
			return getAsteriskList(map);
		}

		return get(post, (Map) map.get(pre));
	}

	private List getAsteriskList(Map map) {
		Object list = map.get(ASTERISK);
		if (list != null) {
			return (List) list;
		}

		return null;
	}
}
