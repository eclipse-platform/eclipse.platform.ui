package org.eclipse.update.internal.ui.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.URL;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.model.IFeatureAdapter;
import org.eclipse.update.internal.ui.model.ISiteAdapter;
import org.eclipse.update.internal.ui.model.ModelObject;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.model.*;
import java.util.*;

public class SearchResultSite
	extends ModelObject
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

	public ISite getSite() {
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