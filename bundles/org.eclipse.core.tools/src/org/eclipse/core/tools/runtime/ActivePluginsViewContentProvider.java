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
		if (flat || !(element instanceof BundleStats))
			return new Object[0];
		if (element == null)
			return null;
		ArrayList stats = ((BundleStats) element).getBundlesActivated();
		return stats.toArray(new Object[stats.size()]);
	}

	public Object getParent(Object element) {
		if (flat || !(element instanceof BundleStats))
			return null;
		if (element == null)
			return null;
		return ((BundleStats) element).getActivatedBy().getSymbolicName();
	}

	public boolean hasChildren(Object element) {
		if (flat || !(element instanceof BundleStats))
			return false;
		return element == null ? false : ((BundleStats) element).getBundlesActivated().size() > 0;
	}

	public Object[] getElements(Object inputElement) {
		if (!StatsManager.MONITOR_ACTIVATION || inputElement != BundleStats.class)
			return null;

		BundleStats[] active = StatsManager.getDefault().getBundles();
		ArrayList result = new ArrayList(active.length);
		for (int i = 0; i < active.length; i++) {
			if (flat || active[i].getActivatedBy() == null)
				result.add(active[i]);
		}
		return result.toArray(new BundleStats[result.size()]);
	}

}