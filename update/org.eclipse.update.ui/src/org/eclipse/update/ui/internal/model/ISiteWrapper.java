package org.eclipse.update.ui.internal.model;

import org.eclipse.update.core.ISite;
import java.net.URL;

public interface ISiteWrapper {
	
	public String getLabel();
	public URL getURL();
	public ISite getSite();

}

