package org.eclipse.update.ui.internal.model;

import java.net.URL;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.model.*;
import java.util.*;

public class SiteBookmark extends ModelObject implements IWorkbenchAdapter {
	private String name;
	private URL url;
	private ISite site;
	private Vector catalog;
	
	public static final String P_NAME="p_name";
	public static final String P_URL="p_url";
	
	public SiteBookmark() {
	}
	
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class)) {
			return this;
		}
		return super.getAdapter(adapter);
	}
	
	public SiteBookmark(String name, URL url) {
		this.name = name;
		this.url = url;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return getName();
	}
	
	public void setName(String name) {
		this.name = name;
		notifyObjectChanged(P_NAME);
	}
	
	public URL getURL() {
		return url;
	}

	public void setURL(URL url) {
		this.url = url;
		notifyObjectChanged(P_URL);
	}
	
	public ISite getSite() {
		return site;
	}
	
	public boolean isSiteConnected() {
		return site!=null;
	}
	
	public void connect() throws CoreException {
		site = SiteManager.getSite(url);
		createCatalog();
	}
	
	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		return null;
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
		return getName();
	}

	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object arg0) {
		return null;
	}
	
	private void createCatalog() {
		catalog = new Vector();
		// Add all the categories
		ICategory [] categories = site.getCategories();
		for (int i=0; i<categories.length; i++) {
			ICategory category = categories[i];
			addCategoryToCatalog(category);
		}
		// Add features to categories
		IFeature [] features = site.getFeatures();
		for (int i=0; i<features.length; i++) {
			IFeature feature = features[i];
			addFeatureToCatalog(feature);
		}
	}

	public Object [] getCatalog() {
		if (catalog.size()>0) return catalog.toArray();
		return site.getFeatures();
	}
	private void addCategoryToCatalog(ICategory category) {
		String name = category.getName();
		int loc = name.indexOf('/');
		if (loc == -1) {
			// first level
			catalog.add(new SiteCategory(name, category));
		}
		else {
			IPath path = new Path(name);
			path = path.removeLastSegments(1);
			SiteCategory parentCategory = findCategory(path, catalog.toArray());
		}
	}
	private void addFeatureToCatalog(IFeature feature) {
	}
	
	private SiteCategory findCategory(IPath path, Object [] children) {
		for (int i=0; i<children.length; i++) {
			Object child = children[i];
			if (child instanceof SiteCategory) {
				SiteCategory sc = (SiteCategory)child;
				if (sc.getName().equals(path.segment(0))) {
				   if (path.segmentCount()==1) return sc;
					else {
						path = path.removeFirstSegments(1);
						return findCategory(path, sc.getChildren());
					}
				}
			}
		}
		return null;
	}
}