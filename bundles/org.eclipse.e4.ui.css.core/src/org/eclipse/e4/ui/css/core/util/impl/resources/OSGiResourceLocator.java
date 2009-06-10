package org.eclipse.e4.ui.css.core.util.impl.resources;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.ui.css.core.util.resources.IResourceLocator;

public class OSGiResourceLocator implements IResourceLocator {
	
	private String startLocation;

	public OSGiResourceLocator(String start) {
		startLocation = start;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.util.resources.IURIResolver#resolve(java.lang.String)
	 */
	public String resolve(String uri) {
		return uri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.util.resources.IResourceLocator#getInputStream(java.lang.String)
	 */
	public InputStream getInputStream(String uri) throws Exception {
		return FileLocator.resolve(
					new URL(startLocation + uri)
				).openStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.util.resources.IResourceLocator#getReader(java.lang.String)
	 */
	public Reader getReader(String uri) throws Exception {
		return new InputStreamReader(getInputStream(uri));
	}


}
