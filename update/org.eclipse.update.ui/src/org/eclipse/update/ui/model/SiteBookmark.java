package org.eclipse.update.ui.model;

import java.net.URL;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class SiteBookmark extends ModelObject {
	private String name;
	private URL url;
	private ISite site;
	
	public static final String P_NAME="p_name";
	public static final String P_URL="p_url";
	
	public SiteBookmark() {
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
}