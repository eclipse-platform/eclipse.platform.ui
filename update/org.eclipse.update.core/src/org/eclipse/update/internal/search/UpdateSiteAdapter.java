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
package org.eclipse.update.internal.search;

import java.net.*;

import org.eclipse.update.search.*;

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
/*
	public ISite getSite(IProgressMonitor monitor) {
		try {
			return SiteManager.getSite(getURL(), monitor);
		} catch (CoreException e) {
			return null;
		}
	}
*/
}