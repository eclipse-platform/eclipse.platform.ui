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

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.framework.stats.BundleStats;
import org.eclipse.osgi.framework.stats.StatsManager;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for the ActivePluginsView
 *
 */
public class ActivePluginsViewLabelProvider extends LabelProvider implements ITableLabelProvider {

	public void addListener(ILabelProviderListener listener) {
		// do nothing
	}

	public void dispose() {
		// do nothing
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// do nothing
	}

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof IPluginDescriptor))
			return "not a plugin"; //$NON-NLS-1$
		BundleStats plugin = StatsManager.getDefault().getPlugin(((IPluginDescriptor) element).getUniqueIdentifier());
		if (plugin == null)
			return "no info for plugin"; //$NON-NLS-1$
		VMClassloaderInfo pluginInfo = VMClassloaderInfo.getClassloader(plugin.getPluginId());
		switch (columnIndex) {
			case 0 : /* plugin id */
				return plugin.getPluginId() + (plugin.isStartupPlugin() ? "*" : ""); //$NON-NLS-1$ //$NON-NLS-2$
			case 1 : /* class load count */
				return "" + plugin.getClassLoadCount(); //$NON-NLS-1$
			case 2 : /* Total Mem Alloc */
				return "" + (pluginInfo.getAllocRAM() + pluginInfo.getAllocROM()); //$NON-NLS-1$
			case 3 : /* Total Mem Used */
				return "" + (pluginInfo.getUsedRAM() + pluginInfo.getUsedROM()); //$NON-NLS-1$
			case 4 : /* startup time */
				return "" + (plugin.getStartupTime()); //$NON-NLS-1$
			case 5 : /* activation order */
				return "" + plugin.getActivationOrder(); //$NON-NLS-1$
			case 6 : /* activation time */
				return "" + (int) (plugin.getTimestamp()); //$NON-NLS-1$
			case 7 : /* class load time*/
				return "" + (int) (plugin.getClassLoadTime()); //$NON-NLS-1$
			case 8 : /* startup method time */
				return "" + (int) (plugin.getStartupMethodTime()); //$NON-NLS-1$
			case 9 : /* RAM alloc */
				return "" + pluginInfo.getAllocRAM(); //$NON-NLS-1$
			case 10 : /* RAM used */
				return "" + pluginInfo.getUsedRAM(); //$NON-NLS-1$
			case 11 : /* ROM alloc */
				return "" + pluginInfo.getAllocROM(); //$NON-NLS-1$
			case 12 : /* ROM used */
				return "" + pluginInfo.getUsedROM(); //$NON-NLS-1$
		}
		return null;
	}

}