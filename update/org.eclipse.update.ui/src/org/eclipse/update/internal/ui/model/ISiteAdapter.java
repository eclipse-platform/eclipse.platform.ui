package org.eclipse.update.internal.ui.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.ISite;
import java.net.URL;

public interface ISiteAdapter {
	
	public String getLabel();
	public URL getURL();
	public ISite getSite(IProgressMonitor monitor);

}

