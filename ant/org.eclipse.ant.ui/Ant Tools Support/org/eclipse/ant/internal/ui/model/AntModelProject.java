/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * Portions Copyright  2000-2004 The Apache Software Foundation
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Apache Software License v2.0 which 
 * accompanies this distribution and is available at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Contributors:
 *     IBM Corporation - derived implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.model;

import java.io.File;
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
	private AntPropertyNode fCurrentConfiguringPropertyNode;
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#setNewProperty(java.lang.String, java.lang.String)
	 */
	public void setNewProperty(String name, String value) {
		
		if (fCurrentProperties.get(name) != null) {
			return;
		} 
		//allows property values to be over-written for this parse session
		//there is currently no way to remove properties from the Apache Ant project
		//the project resets it properties for each parse...see reset()
		fCurrentProperties.put(name, value);
		if (fCurrentConfiguringPropertyNode != null) {
			fCurrentConfiguringPropertyNode.addProperty(name, value);
		}
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
		fCurrentProperties= new Hashtable(fBaseProperties);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#getProperty(java.lang.String)
	 */
	public String getProperty(String name) {
		//override as we cannot remove properties from the Apache Ant project
		String result= (String)fCurrentProperties.get(name);
		if (result == null) {
			result= getUserProperty(name);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#getProperties()
	 */
	public Hashtable getProperties() {
		//override as we cannot remove properties from the Apache Ant project
		Hashtable allProps= new Hashtable(fCurrentProperties);
		allProps.putAll(getUserProperties());
		allProps.put("basedir", getBaseDir().getPath()); //$NON-NLS-1$
		return allProps;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#init()
	 */
	public void init() throws BuildException {
		super.init();
		fBaseProperties= super.getProperties();
		fCurrentProperties= super.getProperties();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Project#setBaseDir(java.io.File)
	 */
	public void setBaseDir(File baseDir) throws BuildException {
		super.setBaseDir(baseDir);
		fCurrentProperties.put("basedir", getBaseDir().getPath()); //$NON-NLS-1$
	}

	/**
	 * @param node the property node that is currently being configured
	 */
	public void setCurrentConfiguringProperty(AntPropertyNode node) {
		fCurrentConfiguringPropertyNode= node;
	}
}