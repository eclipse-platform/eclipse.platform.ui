/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

 
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPresentationManager;
import org.eclipse.debug.ui.DefaultDebugViewContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for the debug view.
 */
public class LaunchViewContentProvider implements ITreeContentProvider {
	
	/**
	 * Map of custom content providers keyed by content provider id.
	 */
	private Map fContentProviders;

	/**
	 * Default content provider for models that do not supply their own.
	 */
	private ITreeContentProvider fDefaultContentProvider;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		return getContentProvider(parent).getChildren(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return getContentProvider(element).getParent(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return getContentProvider(element).hasChildren(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		if (fDefaultContentProvider != null) {
			fDefaultContentProvider.dispose();
		}
		if (fContentProviders != null) {
			Iterator iterator = fContentProviders.values().iterator();
			while (iterator.hasNext()) {
				ITreeContentProvider provider = (ITreeContentProvider) iterator.next();
				provider.dispose();
			}
			fContentProviders.clear();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
	/**
	 * Returns the content provider to use for the given object.
	 * 
	 * @param object object for which content is being queried
	 * @return the content provider to use for the given object
	 */
	private ITreeContentProvider getContentProvider(Object object) {
		LaunchConfigurationPresentationManager manager = LaunchConfigurationPresentationManager.getDefault();
		String id = manager.getDebugViewContentProvderId(object);
		ITreeContentProvider provider = null;
		if (id != null) {
			if (fContentProviders == null) {
				fContentProviders = new HashMap();
			}
			provider = (ITreeContentProvider) fContentProviders.get(id);
			if (provider == null) {
				provider = manager.newDebugViewContentProvider(id);
				if (provider != null) {
					fContentProviders.put(id, provider);
				}
			}
		}
		if (provider != null) {
			return provider;
		}
		return getDefaultContentProvider(); 
	}
	
	/**
	 * Returns the default content provider
	 * 
	 * @return default content provider
	 */
	private ITreeContentProvider getDefaultContentProvider() {
		if (fDefaultContentProvider == null) {
			fDefaultContentProvider = new DefaultDebugViewContentProvider();
		}
		return fDefaultContentProvider;
	}
}