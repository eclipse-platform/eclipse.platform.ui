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

package org.eclipse.ant.internal.ui.preferences;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.ant.core.IAntClasspathEntry;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;

public class ClasspathEntry extends AbstractClasspathEntry {

	private URL url= null;
	private String variableString= null;
	private IAntClasspathEntry entry= null;
	
	public ClasspathEntry(Object o, IClasspathEntry parent) {
		this.parent= parent;
		if (o instanceof URL) {
			url= (URL)o;
		} else if (o instanceof String) {
			variableString= (String)o;
		} else if (o instanceof IAntClasspathEntry) {
			entry= (IAntClasspathEntry)o;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IAntClasspathEntry) {
			IAntClasspathEntry other= (IAntClasspathEntry)obj;
			return other.getLabel().equals(getLabel());
		}
		return false;
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getLabel().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (entry != null) {
			return entry.getLabel();
		}
		if (getURL() != null) {
			return getURL().getFile();
		} 

		return getVariableString();
	}
	
	protected URL getURL() {
		return url;
	}
	
	protected String getVariableString() {
		return variableString;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.core.IAntClasspathEntry#getLabel()
	 */
	public String getLabel() {
		if (entry == null) {
			return toString();
		}
		return entry.getLabel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.core.IAntClasspathEntry#getEntryURL()
	 */
	public URL getEntryURL() {
		if (entry != null) {
			return entry.getEntryURL();
		}
		if (url != null) {
			return url;
		} 
			
		try {
			String expanded = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(variableString);
			return new URL("file:" + expanded); //$NON-NLS-1$
		} catch (CoreException e) {
			AntUIPlugin.log(e);
		} catch (MalformedURLException e) {
			AntUIPlugin.log(e);
		}
		return null;
	}
}