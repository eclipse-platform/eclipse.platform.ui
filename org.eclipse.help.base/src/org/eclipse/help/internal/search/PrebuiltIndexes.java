/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.help.internal.base.HelpBasePlugin;

/**
 * Plugins with prebuilt search indexes.
 * 
 */
public class PrebuiltIndexes {
	private String locale;

	/**
	 * Set of PluginIndex
	 */
	private Set set = new HashSet();

	PrebuiltIndexes(String locale) {
		super();
		this.locale = locale;
	}

	void add(String plugin, String path) {
		set.add(new PluginIndex(plugin, path, locale));
	}

	/**
	 * Removes Plugin indexes with no index
	 */
	private void trim() {
		List indexes = new ArrayList(set);
		for (int i = 0; i < indexes.size();) {
			PluginIndex index = (PluginIndex) indexes.get(i);
			if (index.getPaths().size() == 0) {
				HelpBasePlugin.logError("Help index missing for for plugin " //$NON-NLS-1$
						+ index.getPluginId() + ".", null); //$NON-NLS-1$
				set.remove(index);
			}
			i++;
		}
	}

	public PluginIndex[] getIndexes() {
		trim();
		return (PluginIndex[]) set.toArray(new PluginIndex[set.size()]);
	}
}
