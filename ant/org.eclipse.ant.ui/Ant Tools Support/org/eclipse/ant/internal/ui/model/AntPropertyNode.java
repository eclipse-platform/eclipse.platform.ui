/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.xml.sax.Attributes;

public class AntPropertyNode extends AntTaskNode {
	
	private String fValue= null;
	
	/*
	 * The set of properties defined by this node
	 * name-> value mapping
	 */
	private Map fProperties= null;
	
	public AntPropertyNode(Task task, Attributes attributes) {
		super(task);
		 String label = attributes.getValue(IAntModelConstants.ATTR_NAME);
         if(label == null) {
			label = attributes.getValue(IAntModelConstants.ATTR_FILE);
         	if(label != null) {
         		label=  "file="+label; //$NON-NLS-1$
         	} else {	
         		label =  attributes.getValue(IAntModelConstants.ATTR_RESOURCE);
         		if (label != null) {
         			label= "resource="+label; //$NON-NLS-1$
         		} else {
         			label = attributes.getValue(IAntModelConstants.ATTR_ENVIRONMENT);
         			if(label != null) {
         				label= "environment=" + label; //$NON-NLS-1$
         			}
         		}
         	}
         } else {
         	fValue= attributes.getValue(IAntModelConstants.ATTR_VALUE);
         }
         setLabel(label);
	}
	
	public String getValue() {
		return fValue;
	}
	
	public String getProperty(String propertyName) {
		return (String)fProperties.get(propertyName);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#getBaseImageDescriptor()
	 */
	protected ImageDescriptor getBaseImageDescriptor() {
		return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_PROPERTY);
	}
	
	/**
	 * Sets the property in the project.
	 */
	public boolean configure(boolean validateFully) {
		if (configured) {
			return false;
		}
		try {
			getProjectNode().setCurrentConfiguringProperty(this);
			getTask().maybeConfigure();
			getTask().execute();
			configured= true;
		} catch (BuildException be) {
			handleBuildException(be, AntEditorPreferenceConstants.PROBLEM_PROPERTIES);
		} finally {
			getProjectNode().setCurrentConfiguringProperty(null);
		}
		return false;
	}

	/**
	 * @param name
	 * @param value
	 */
	public void addProperty(String name, String value) {
		if (fProperties == null) {
			fProperties= new HashMap(1);
		}
		fProperties.put(name, value);
	}
}