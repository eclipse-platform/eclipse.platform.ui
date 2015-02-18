/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.model;

import java.util.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.model.PluginModel;

/**
 * A map specialized to manage plugin models (either for plugins or fragments).
 */
public class PluginMap {
	private Map map;
	private boolean preserveOrder;
	private int size;
	private boolean replaceDuplicates;

	public PluginMap(Map pluginModels) {
		this(pluginModels, true, false);
	}

	public PluginMap(Map pluginModels, boolean preserveOrder, boolean replaceDuplicates) {
		this.map = pluginModels;
		this.preserveOrder = preserveOrder;
		this.replaceDuplicates = replaceDuplicates;
	}

	public void add(PluginModel pluginModel) {

		String key = pluginModel.getId();
		List verList = (List) map.get(key);

		// create new index entry if one does not exist for plugin
		if (verList == null) {
			verList = new LinkedList();
			map.put(key, verList);
		}

		int i = 0;
		// insert plugin into list maintaining version order
		if (preserveOrder)
			for (; i < verList.size(); i++) {
				PluginModel element = (PluginModel) verList.get(i);
				if (getVersionIdentifier(pluginModel).equals(getVersionIdentifier(element))) {
					if (replaceDuplicates)
						verList.set(i, pluginModel);
					return; // ignore duplicates
				}
				if (getVersionIdentifier(pluginModel).isGreaterThan(getVersionIdentifier(element)))
					break;
			}
		verList.add(i, pluginModel);
		size++;
	}

	public PluginModel get(String id, String version) {
		List versions = (List) map.get(id);
		if (versions == null || versions.isEmpty())
			return null;
		if (version == null)
			// Just return the first one in the list (random)			
			return (PluginModel) versions.get(0);
		int versionCount = versions.size();
		for (int i = 0; i < versionCount; i++) {
			PluginModel pluginModel = (PluginModel) versions.get(i);
			if (pluginModel.getVersion().equals(version))
				return pluginModel;
		}
		return null;
	}

	public List getVersions(String id) {
		return (List) map.get(id);
	}

	public PluginModel getAny(String id) {
		List versions = (List) map.get(id);
		if (versions == null || versions.isEmpty())
			return null;
		return (PluginModel) versions.get(0);
	}

	private PluginVersionIdentifier getVersionIdentifier(PluginModel model) {
		if (PluginVersionIdentifier.validateVersion(model.getVersion()).getSeverity() != IStatus.OK)
			return new PluginVersionIdentifier("0.0.0"); //$NON-NLS-1$
		return new PluginVersionIdentifier(model.getVersion());
	}

	public int size() {
		return size;
	}

	public void markReadOnly() {
		for (Iterator it = map.values().iterator(); it.hasNext();) {
			List list = (List) it.next();
			int count = list.size();
			for (int i = 0; i < count; i++)
				((PluginModel) list.get(i)).markReadOnly();
		}
	}

	public PluginModel remove(String pluginId, String version) {
		List versions = (List) map.get(pluginId);
		if (versions == null)
			return null;
		for (Iterator iter = versions.iterator(); iter.hasNext();) {
			PluginModel pluginModel = (PluginModel) iter.next();
			if (pluginModel.getId().equals(pluginId) && pluginModel.getVersion().equals(version)) {
				if (versions.size() == 1)
					map.remove(pluginId);
				else
					iter.remove();
				size--;
				return pluginModel;
			}
		}
		return null;
	}

	public void removeVersions(String pluginId) {
		List versions = (List) map.remove(pluginId);
		if (versions != null)
			size -= versions.size();
	}

	public void copyToArray(Object[] array) {
		int index = 0;
		for (Iterator mapIter = map.values().iterator(); mapIter.hasNext();) {
			List versions = (List) mapIter.next();
			for (Iterator listIiter = versions.iterator(); listIiter.hasNext();)
				array[index++] = listIiter.next();
		}
	}
}
