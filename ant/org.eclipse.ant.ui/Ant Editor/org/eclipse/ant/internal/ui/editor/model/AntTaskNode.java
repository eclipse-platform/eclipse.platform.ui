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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.ant.internal.ui.editor.outline.XMLProblem;
import org.eclipse.ant.internal.ui.model.AntUIImages;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;


public class AntTaskNode extends AntElementNode {

	private Task fTask= null;
	private String fLabel= null;
	private String fId= null;
	protected boolean configured= false;
	
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
		StringBuffer label= new StringBuffer();
		if (fLabel != null) {
			label.append(fLabel);
		} else if (fId != null) {
			label.append(fId);
		} else {
			label.append(fTask.getTaskName());
		}
		if (isExternal()) {
			appendEntityName(label);
		}
		return label.toString();
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
		if (fId != null) {
			return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_TYPE);
		}
		
		return super.getBaseImageDescriptor();
	}

	/**
	 * The reference id for this task
	 * @param id The reference id for this task
	 */
	public void setId(String id) {
		fId= id;
	}
	
	/**
	 * Returns the reference id for this task or <code>null</code>
	 * if it has no reference id.
	 * @return The reference id for this task
	 */
	public String getId() {
		return fId;
	}
	
	/**
	 * Configures the associated task if required.
	 * Allows subclasses to do specific configuration (such as executing the task) by
	 * calling <code>nodeSpecificConfigure</code>
	 * 
	 * @return whether the configuration of this node could have impact on other nodes
	 */
	public boolean configure(boolean validateFully) {
		if (!validateFully || (getParentNode() instanceof AntTaskNode)) {
			return false;
		}
		if (configured) {
			return false;
		}
		try {
			getTask().maybeConfigure();
			nodeSpecificConfigure();
			configured= true;
			return true;
		} catch (BuildException be) {
			getAntModel().handleBuildException(be, this);
		}
		return false;
	}

	protected void nodeSpecificConfigure() {
		//by default do nothing
	}

	protected void handleBuildException(BuildException be, String preferenceKey) {
		int severity= XMLProblem.getSeverity(preferenceKey);
		if (severity != XMLProblem.NO_PROBLEM) {
			getAntModel().handleBuildException(be, this, severity);
		}
	}
}