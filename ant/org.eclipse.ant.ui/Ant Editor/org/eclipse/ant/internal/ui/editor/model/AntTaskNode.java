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


public class AntTaskNode extends AntElementNode {

	private Task fTask= null;
	private String fLabel= null;
	
	public AntTaskNode(Task task) {
		super(task.getTaskName());
		fTask= task;
	}	
	
	public AntTaskNode(Task task, String label) {
		super(task.getTaskName());
		fTask= task;
		fLabel= label;
	}	
	
	public String getLabel() {
		if (fLabel != null) {
			return fLabel;
		}
		return fTask.getTaskName();
	}
	
	public void setLabel(String label) {
		fLabel= label;
	}
	
	public Task getTask() {
		return fTask;
	}
	
	public void setTask(Task task) {
		fTask= task;
	}
	
	protected ImageDescriptor getBaseImageDescriptor() {
		if("macrodef".equalsIgnoreCase(getName()) //$NON-NLS-1$
				|| "presetdef".equalsIgnoreCase(getName())) {  //$NON-NLS-1$
			return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_MACRODEF);
		}
		
		if("import".equalsIgnoreCase(getName())) { //$NON-NLS-1$
			return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_IMPORT);
		}
		
		return super.getBaseImageDescriptor();
	}
}