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

package org.eclipse.ant.internal.ui.preferences;

import java.io.File;
import java.net.URL;

public class ClasspathEntry extends AbstractClasspathEntry {

	private URL url= null;
	private String variableString= null;
	
	public ClasspathEntry(Object o, IClasspathEntry parent) {
		this.parent= parent;
		if (o instanceof URL) {
			url= (URL)o;
		} else if (o instanceof String) {
			variableString= (String)o;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ClasspathEntry) {
			ClasspathEntry other= (ClasspathEntry)obj;
			if (getURL() != null && other.getURL() != null) {
				File file= new File(getURL().getFile());
				File otherFile= new File(other.getURL().getFile());
				return otherFile.equals(file);
			} else if (getVariableString() != null && other.getVariableString() != null) {
				return getVariableString().equals(other.getVariableString());
			}
		}
		return false;
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (getURL() != null) {
			return getURL().hashCode();
		} else {
			return getVariableString().hashCode();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (getURL() != null) {
			return getURL().getFile();
		} else {
			return getVariableString();
		}
	}
	
	protected URL getURL() {
		return url;
	}
	
	protected String getVariableString() {
		return variableString;
	}
}