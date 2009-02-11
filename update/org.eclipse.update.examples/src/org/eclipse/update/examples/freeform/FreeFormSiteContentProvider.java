/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.examples.freeform;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.update.core.SiteContentProvider;

public class FreeFormSiteContentProvider extends SiteContentProvider {

	/**
	 * Constructor for SiteContentProvider.
	 */
	public FreeFormSiteContentProvider(URL url) {
		super(url);
	}
	
	/*
	 * @see ISiteContentProvider#getArchiveReference(String)
	 */
	public URL getArchiveReference(String id) throws CoreException {
		try {
			return new URL(getURL(),id);
		} catch(MalformedURLException e) {
			throw newCoreException("Unable to return archive URL",e);
		}
	}
	
	private CoreException newCoreException(String s, Throwable e) throws CoreException {
		return new CoreException(new Status(IStatus.ERROR,"org.eclipse.update.examples",0,s,e));
	}

}
