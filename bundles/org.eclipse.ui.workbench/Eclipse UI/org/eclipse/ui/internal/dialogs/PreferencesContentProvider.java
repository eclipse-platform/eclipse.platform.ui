/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.*;
import org.eclipse.core.runtime.preferences.*;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * The PreferencesContentProvider is the content provider for the
 * preferences export tree.
 */
public class PreferencesContentProvider implements ITreeContentProvider {

	private static Set scopes = new HashSet();

	static {
		scopes.add(InstanceScope.SCOPE);
		scopes.add(ConfigurationScope.SCOPE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		Object[] children = getChildren(parent);
		IEclipsePreferences[] preferences = new IEclipsePreferences[children.length];
		System.arraycopy(children, 0, preferences, 0, children.length);
		return preferences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object child) {
		if (child instanceof IEclipsePreferences)
			return ((IEclipsePreferences) child).parent();
		return null;
	}

	private boolean hasKeys(Preferences node) throws BackingStoreException {
		if (node.keys().length > 0)
			return true;
		String[] children = node.childrenNames();
		for (int i = 0; i < children.length; i++) {
			String child = children[i];
			if (hasKeys(node.node(child)))
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		ArrayList result = new ArrayList();
		if (parent instanceof IEclipsePreferences) {
			IEclipsePreferences node = (IEclipsePreferences) parent;

			try {
				if (node.parent() == null) {
					// handle the case where we are given the root of the tree
					String[] childrenNames = node.childrenNames();
					for (int i = 0; childrenNames != null && i < childrenNames.length; i++) {
						String child = childrenNames[i];
						if (!scopes.contains(child))
							continue;
						Preferences preferences = node.node(child);
						if (hasKeys(preferences))
							result.add(preferences);
					}
				} else if (node.parent().parent() == null) {
					// if we are on a second level
					String[] childrenNames = node.childrenNames();
					for (int i = 0; childrenNames != null && i < childrenNames.length; i++) {
						String child = childrenNames[i];
						Preferences preferences = node.node(child);
						if (hasKeys(preferences))
							result.add(preferences);
					}
				}

			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
		return result.toArray(new Object[result.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object parent) {
		IEclipsePreferences node = (IEclipsePreferences) parent;
		try {
			if (node.parent() == null && hasKeys(node))
				return true;
			if (node.parent().parent() == null && hasKeys(node))
				return true;
		} catch (BackingStoreException e) {
			// do nothing
		}
		return false;
	}
}