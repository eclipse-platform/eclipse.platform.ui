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

package org.eclipse.ant.internal.ui.editor.outline;

/*
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;

/**
 * Derived from the original Ant Project class
 * This class allows property values to be written multiple times.
 * This facilitates incremental parsing of the Ant build file
 * It also attempts to ensure that we clean up after ourselves and allows
 * more manipulation of properties resulting from incremental parsing
 */
public class AntModelProject extends Project {
	
	private Hashtable fBaseProperties;
	private Hashtable fCurrentProperties= new Hashtable();
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#setNewProperty(java.lang.String, java.lang.String)
	 */
	public void setNewProperty(String name, String value) {
		
		if (fCurrentProperties.get(name) != null) {
			return;
		} 
		//always property values to be over-written for this parse session
		//there is currently no way to remove properties from the Apache Ant project
		//the project resets it properties for each parse...see reset()
		fCurrentProperties.put(name, value);
		super.setProperty(name, value);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#fireBuildFinished(java.lang.Throwable)
	 */
	public void fireBuildFinished(Throwable exception) {
		super.fireBuildFinished(exception);
		Enumeration e= getBuildListeners().elements();
		while (e.hasMoreElements()) {
			BuildListener listener = (BuildListener) e.nextElement();
			removeBuildListener(listener);
		}
	}
	
	public void reset() {
		getTargets().clear();
		setDefault(null);
		setDescription(null);
		setName(""); //$NON-NLS-1$
		//reset the properties to the initial set
		fCurrentProperties= new Hashtable();
        Enumeration e = fBaseProperties.keys();
        while (e.hasMoreElements()) {
            Object name = e.nextElement();
            Object value = fBaseProperties.get(name);
            fCurrentProperties.put(name, value);
        }	  
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#getProperty(java.lang.String)
	 */
	public String getProperty(String name) {
		//override as we cannot remove properties from the Apache Ant project
		return (String)fCurrentProperties.get(name);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#getProperties()
	 */
	public Hashtable getProperties() {
		//override as we cannot remove properties from the Apache Ant project
		return fCurrentProperties;
	}
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#init()
	 */
	public void init() throws BuildException {
		super.init();
		fBaseProperties= super.getProperties();
		fCurrentProperties= super.getProperties();
	}
}