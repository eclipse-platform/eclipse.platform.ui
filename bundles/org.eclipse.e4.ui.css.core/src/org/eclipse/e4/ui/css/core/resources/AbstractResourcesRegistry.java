/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.resources;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class resources registry which implement basic cache with Map.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public abstract class AbstractResourcesRegistry implements IResourcesRegistry {

	private Map allResourcesMap = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.resources.IResourcesRegistry#getResource(java.lang.Object,
	 *      java.lang.Object)
	 */
	public Object getResource(Object type, Object key) {
		if (allResourcesMap == null)
			return null;
		Map resourcesMap = (Map) allResourcesMap.get(type);
		if (resourcesMap == null)
			return null;
		return resourcesMap.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.resources.IResourcesRegistry#registerResource(java.lang.Object,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void registerResource(Object type, Object key, Object resource) {
		if (allResourcesMap == null)
			allResourcesMap = new HashMap();
		Map resourcesMap = (Map) allResourcesMap.get(type);
		if (resourcesMap == null) {
			resourcesMap = new HashMap();
			allResourcesMap.put(type, resourcesMap);
		}
		resourcesMap.put(key, resource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.resources.IResourcesRegistry#unregisterResource(java.lang.Object,
	 *      java.lang.Object)
	 */
	public void unregisterResource(Object type, Object key) {
		if (allResourcesMap == null)
			return;
		Map resourcesMap = (Map) allResourcesMap.get(type);
		if (resourcesMap == null)
			return;
		Object resource = resourcesMap.get(key);
		if (resource != null)
			resourcesMap.remove(resource);
	}

	public void unregisterResource(Object resource) {
		Object type = getResourceType(resource);
		if (type != null) {
			Map resourcesMap = (Map) allResourcesMap.get(type);
			if (resourcesMap != null) {
				resourcesMap.remove(resource);
			}
		}
	}
	
	public Object getResourceType(Object resource) {
		return resource.getClass();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.resources.IResourcesRegistry#dispose()
	 */
	public void dispose() {
		if (allResourcesMap == null)
			return;
		// Loop for all resources stored into cache
		Set allResources = allResourcesMap.entrySet();
		for (Iterator iterator = allResources.iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Object type = entry.getKey();
			Map resourcesMap = (Map) entry.getValue();
			Set resources = resourcesMap.entrySet();
			for (Iterator iterator2 = resources.iterator(); iterator2.hasNext();) {
				Map.Entry entry2 = (Map.Entry) iterator2.next();
				// Dispose the current resource.
				disposeResource(type, (String) entry2.getKey(), entry2
						.getValue());
			}
		}
		allResourcesMap = null;
	}

	public abstract void disposeResource(Object type, String key,
			Object resource);
}
