package org.eclipse.update.ui.model;

import java.net.URL;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.*;

public class SiteBookmark {
	private String name;
	private URL url;
	private ISite site;
	
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
	}
	
	public URL getURL() {
		return url;
	}

	public void setURL(URL url) {
		this.url = url;
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