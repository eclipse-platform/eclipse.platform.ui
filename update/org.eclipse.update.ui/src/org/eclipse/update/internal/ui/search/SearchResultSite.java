/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.search;

import java.net.URL;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.update.core.ISite;
import org.eclipse.update.internal.ui.model.*;

public class SearchResultSite
	extends UIModelObject
	implements IWorkbenchAdapter, ISiteAdapter {
	private ISite site;
	private Vector candidates;
	private String label;
	private SearchObject search;

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class)) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	public SearchResultSite(SearchObject search, String label, ISite site) {
		this.search = search;
		this.label = label;
		this.site = site;
		candidates = new Vector();
	}
	
	public SearchObject getSearch() {
		return search;
	}

	public ISite getSite(IProgressMonitor monitor) {
		return site;
	}

	public String getLabel() {
		return label;
	}

	public String toString() {
		return getLabel();
	}

	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		return candidates.toArray();
	}
	public int getChildCount() {
		return candidates.size();
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object obj) {
		return null;
	}

	/**
	 * @see IWorkbenchAdapter#getLabel(Object)
	 */
	public String getLabel(Object obj) {
		return getLabel();
	}

	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object arg0) {
		return null;
	}

	public void addCandidate(IFeatureAdapter candidate) {
		candidates.add(candidate);
	}
	/**
	 * @see ISiteAdapter#getURL()
	 */
	public URL getURL() {
		return site.getURL();
	}

}
