/*
 * Created on May 24, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.search;

import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UpdateSiteAdapter implements IUpdateSiteAdapter {
	private String label;
	private URL url;
	
	public UpdateSiteAdapter(String label, URL url) {
		this.label = label;
		this.url = url;
	}
	public URL getURL() {
		return url;
	}
	public String getLabel() {
		return label;
	}
	public ISite getSite(IProgressMonitor monitor) {
		try {
			return SiteManager.getSite(getURL(), monitor);
		} catch (CoreException e) {
			return null;
		}
	}
}