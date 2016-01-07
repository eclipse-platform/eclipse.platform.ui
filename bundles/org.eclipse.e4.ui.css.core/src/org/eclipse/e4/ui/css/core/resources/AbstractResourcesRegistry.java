/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Abstract class resources registry which implement basic cache with Map.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 *
 */
public abstract class AbstractResourcesRegistry implements IResourcesRegistry {

	private Map<Object, Map<Object, Object>> allResourcesMap;

	@Override
	public Object getResource(Object type, Object key) {
		if (allResourcesMap == null) {
			return null;
		}
		Map<Object, Object> resourcesMap = allResourcesMap.get(type);
		if (resourcesMap == null) {
			return null;
		}
		return resourcesMap.get(key);
	}

	@Override
	public void registerResource(Object type, Object key, Object resource) {
		if (allResourcesMap == null) {
			allResourcesMap = new HashMap<>();
		}
		Map<Object, Object> resourcesMap = allResourcesMap.get(type);
		if (resourcesMap == null) {
			resourcesMap = new HashMap<>();
			allResourcesMap.put(type, resourcesMap);
		}
		resourcesMap.put(key, resource);
	}

	protected Map<Object, Object> getCacheByType(Object type) {
		if (allResourcesMap != null) {
			Map<Object, Object> resourcesMap = allResourcesMap.get(type);
			if (resourcesMap != null) {
				return resourcesMap;
			}
		}
		return Collections.emptyMap();
	}

	@Override
	public void unregisterResource(Object type, Object key) {
		if (allResourcesMap == null) {
			return;
		}
		Map<Object, Object> resourcesMap = allResourcesMap.get(type);
		if (resourcesMap == null) {
			return;
		}
		Object resource = resourcesMap.get(key);
		if (resource != null) {
			resourcesMap.remove(resource);
		}
	}

	public void unregisterResource(Object resource) {
		Object type = getResourceType(resource);
		if (type != null) {
			Map<Object, Object> resourcesMap = allResourcesMap.get(type);
			if (resourcesMap != null) {
				resourcesMap.remove(resource);
			}
		}
	}

	public Object getResourceType(Object resource) {
		return resource.getClass();
	}

	@Override
	public void dispose() {
		if (allResourcesMap == null) {
			return;
		}
		// Loop for all resources stored into cache
		Set<Entry<Object, Map<Object, Object>>> allResources = allResourcesMap.entrySet();
		for (Entry<Object, Map<Object, Object>> entry : allResources) {
			Object type = entry.getKey();
			Set<Entry<Object, Object>> resources = entry.getValue().entrySet();
			for (Entry<Object, Object> entry2 : resources) {
				// Dispose the current resource.
				disposeResource(type, entry2.getKey(), entry2.getValue());
			}
		}
		allResourcesMap = null;
	}

	public abstract void disposeResource(Object type, Object key, Object resource);
}
