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

import java.util.*;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tools.Policy;
import org.eclipse.core.tools.SpyView;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class PluginListView extends SpyView implements IStructuredContentProvider {

	// cache the plug-in list
	private Object[] plugins = null;

	/** The id by which this view is known in the plug-in registry */
	public static final String VIEW_ID = PluginListView.class.getName();

	/**
	 * Class which provides the text labels for the view.
	 */
	class PluginListLabelProvider extends LabelProvider implements ITableLabelProvider {
		/**
		 * @see ITableLabelProvider#getColumnImage(Object, int)
		 */
		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}

		/**
		 * @see ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			return element == null ? Policy.bind("depend.badPluginId") : ((IPluginDescriptor) element).getUniqueIdentifier(); //$NON-NLS-1$
		}
	}

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object arg0) {
		if (plugins == null) {
			// before caching the array of descriptors, sort them.
			// we have to use a comparator here because plug-in
			// descriptors cannot be compared against each other
			// in a tree set.
			Comparator comparator = new Comparator() {
				public int compare(Object obj1, Object obj2) {
					String id1 = ((IPluginDescriptor) obj1).getUniqueIdentifier();
					String id2 = ((IPluginDescriptor) obj2).getUniqueIdentifier();
					return id1.compareTo(id2);
				}
			};
			Set set = new TreeSet(comparator);
			IPluginDescriptor[] descriptors = Platform.getPluginRegistry().getPluginDescriptors();
			for (int i = 0; i < descriptors.length; i++)
				set.add(descriptors[i]);
			plugins = set.toArray();
		}
		return plugins;
	}

	/**
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {
		plugins = null;
	}

	/**
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// do nothing
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		// Create viewer.
		ListViewer viewer = new ListViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.setContentProvider(this);
		viewer.setLabelProvider(new PluginListLabelProvider());
		viewer.setInput(""); //$NON-NLS-1$
		getSite().setSelectionProvider(viewer);
	}
}