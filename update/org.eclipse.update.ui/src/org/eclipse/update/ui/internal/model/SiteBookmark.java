package org.eclipse.update.ui.internal.model;

import java.net.URL;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.model.*;

public class SiteBookmark extends ModelObject implements IWorkbenchAdapter {
	private String name;
	private URL url;
	private ISite site;
	
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
	
	public void connect(IProgressMonitor monitor) throws CoreException {
	}
	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object arg0) {
		return null;
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object arg0) {
		return null;
	}

	/**
	 * @see IWorkbenchAdapter#getLabel(Object)
	 */
	public String getLabel(Object arg0) {
		return getName();
	}

	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object arg0) {
		return null;
	}

}