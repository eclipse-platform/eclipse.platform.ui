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

package org.eclipse.ant.internal.ui.editor.model;

import org.apache.tools.ant.Task;
import org.eclipse.ant.internal.ui.model.AntUIImages;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.xml.sax.Attributes;

public class AntPropertyNode extends AntTaskNode {
	
	private String fValue= null;
	
	public AntPropertyNode(Task task, Attributes attributes) {
		super(task);
		 String label = attributes.getValue(IAntEditorConstants.ATTR_NAME);
         if(label == null) {
         	label = attributes.getValue(IAntEditorConstants.ATTR_FILE);
         	if(label != null) {
         		label=  "file="+label; //$NON-NLS-1$
         	} else {	
         		label =  attributes.getValue(IAntEditorConstants.ATTR_RESOURCE);
         		if (label != null) {
         			label= "resource="+label; //$NON-NLS-1$
         		} else {
         			label = attributes.getValue(IAntEditorConstants.ATTR_ENVIRONMENT);
         			if(label != null) {
         				label= "environment=" + label; //$NON-NLS-1$
         			}
         		}
         	}
         } else {
         	fValue= attributes.getValue(IAntEditorConstants.ATTR_VALUE);
         }
         setLabel(label);
         //set the property in the project
         task.maybeConfigure();
         task.execute();
	}
	
	public String getValue() {
		return fValue;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#getBaseImageDescriptor()
	 */
	protected ImageDescriptor getBaseImageDescriptor() {
		return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_PROPERTY);
	}
}