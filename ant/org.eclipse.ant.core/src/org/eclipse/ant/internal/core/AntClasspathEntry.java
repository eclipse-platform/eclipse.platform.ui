/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.IAntClasspathEntry;
import org.eclipse.core.internal.variables.StringVariableManager;
import org.eclipse.core.runtime.CoreException;

public class AntClasspathEntry implements IAntClasspathEntry {

	private String entryString;
	private boolean eclipseRequired= true;
	private URL url= null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.core.IAntClasspathEntry#getLabel()
	 */
	public String getLabel() {
		
		return entryString;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.core.IAntClasspathEntry#getEntryURL()
	 */
	public URL getEntryURL() {
		if (url != null) {
			return url;
		}
		try {
			String expanded = StringVariableManager.getDefault().performStringSubstitution(entryString);
			return new URL("file:" + expanded); //$NON-NLS-1$
		} catch (CoreException e) {
			AntCorePlugin.log(e);
		} catch (MalformedURLException e) {
			AntCorePlugin.log(e);
		}
		return null;
	}

	public AntClasspathEntry(String entryString) {
		this.entryString= entryString;
	}
	
	public AntClasspathEntry(URL url) {
		this.url= url;
		this.entryString= new File(url.getPath()).getAbsolutePath();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IAntClasspathEntry) {
			IAntClasspathEntry other= (IAntClasspathEntry)obj;
			return entryString.equals(other.getLabel());
		}
		return false;
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return entryString.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getLabel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.core.IAntClasspathEntry#isEclipseRuntimeRequired()
	 */
	public boolean isEclipseRuntimeRequired() {
		return eclipseRequired;
	}
	
	public void setEclipseRuntimeRequired(boolean eclipseRequired) {
		this.eclipseRequired = eclipseRequired;
	}
}