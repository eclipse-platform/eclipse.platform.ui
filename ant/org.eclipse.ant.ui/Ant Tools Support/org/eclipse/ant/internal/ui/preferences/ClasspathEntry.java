/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.preferences;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.ant.core.IAntClasspathEntry;
import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;

public class ClasspathEntry extends AbstractClasspathEntry {

	private URL fUrl= null;
	private String fVariableString= null;
	private IAntClasspathEntry fEntry= null;
	
	public ClasspathEntry(Object o, IClasspathEntry parent) {
		fParent= parent;
		if (o instanceof URL) {
			fUrl= (URL)o;
		} else if (o instanceof String) {
			fVariableString= (String)o;
		} else if (o instanceof IAntClasspathEntry) {
			fEntry= (IAntClasspathEntry)o;
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
		if (fEntry != null) {
			return fEntry.getLabel();
		}
		if (getURL() != null) {
			return getURL().getFile();
		} 

		return getVariableString();
	}
	
	protected URL getURL() {
		return fUrl;
	}
	
	protected String getVariableString() {
		return fVariableString;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.core.IAntClasspathEntry#getLabel()
	 */
	public String getLabel() {
		if (fEntry == null) {
			return toString();
		}
		return fEntry.getLabel();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.core.IAntClasspathEntry#getEntryURL()
	 */
	public URL getEntryURL() {
		if (fEntry != null) {
			return fEntry.getEntryURL();
		}
		if (fUrl != null) {
			return fUrl;
		} 
			
		try {
			String expanded = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(fVariableString);
			return new URL(IAntCoreConstants.FILE_PROTOCOL + expanded);
		} catch (CoreException e) {
			AntUIPlugin.log(e);
		} catch (MalformedURLException e) {
			AntUIPlugin.log(e);
		}
		return null;
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ant.core.IAntClasspathEntry#isEclipseRuntimeRequired()
     */
    public boolean isEclipseRuntimeRequired() {
        if (fEntry == null) {
            return super.isEclipseRuntimeRequired();
        } 
        return fEntry.isEclipseRuntimeRequired();
    }
}
