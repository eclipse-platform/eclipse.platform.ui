/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.runtime;

import java.util.ArrayList;
import org.eclipse.core.runtime.*;
import org.eclipse.core.tools.IFlattable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.framework.stats.BundleStats;
import org.eclipse.osgi.framework.stats.StatsManager;

/**
 * Content provider for the ActivePluginsView
 */
public class ActivePluginsViewContentProvider implements ITreeContentProvider, IFlattable {
	private boolean flat;

	public void setFlat(boolean mode) {
		flat = mode;
	}

	public void dispose() {
		// do nothing
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}

	public Object[] getChildren(Object element) {
		if (flat || !(element instanceof IPluginDescriptor))
			return new Object[0];
		BundleStats plugin = StatsManager.getDefault().getPlugin(((IPluginDescriptor) element).getUniqueIdentifier());
		if (plugin == null)
			return null;
		ArrayList stats = plugin.getPluginsActivated();
		Object[] result = new Object[stats.size()];
		IPluginRegistry registry = Platform.getPluginRegistry();
		for (int i = 0; i < stats.size(); i++)
			result[i] = registry.getPluginDescriptor(((BundleStats) stats.get(i)).getPluginId());
		return result;
	}

	public Object getParent(Object element) {
		if (flat || !(element instanceof IPluginDescriptor))
			return null;
		BundleStats plugin = StatsManager.getDefault().getPlugin(((IPluginDescriptor) element).getUniqueIdentifier());
		if (plugin == null)
			return null;
		return Platform.getPluginRegistry().getPluginDescriptor(plugin.getActivatedBy().getPluginId());
	}

	public boolean hasChildren(Object element) {
		if (flat || !(element instanceof IPluginDescriptor))
			return false;
		BundleStats plugin = StatsManager.getDefault().getPlugin(((IPluginDescriptor) element).getUniqueIdentifier());
		return plugin == null ? false : plugin.getPluginsActivated().size() > 0;
	}

	public Object[] getElements(Object inputElement) {
		if (!StatsManager.MONITOR_ACTIVATION || inputElement != BundleStats.class)
			return null;

		BundleStats[] activePlugins = StatsManager.getDefault().getPlugins();
		IPluginRegistry registry = Platform.getPluginRegistry();
		ArrayList result = new ArrayList(activePlugins.length);
		for (int i = 0; i < activePlugins.length; i++) {
			if (flat || activePlugins[i].getActivatedBy() == null)
				result.add(registry.getPluginDescriptor(activePlugins[i].getPluginId()));
		}
		return result.toArray(new IPluginDescriptor[result.size()]);
	}

}