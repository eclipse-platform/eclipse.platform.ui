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
import org.eclipse.ant.internal.ui.model.AntUIImages;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;

public class AntDefiningTaskNode extends AntTaskNode {
	
	public AntDefiningTaskNode(Task task, String label) {
		super(task, label);
	}
	
	protected ImageDescriptor getBaseImageDescriptor() {
		return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_MACRODEF);	
	}
	
	/**
	 * Execute the defining task.
	 */
	public boolean configure(boolean validateFully) {
		if (configured) {
			return false;
		}
		try {
			getTask().maybeConfigure();
			getTask().execute();
			configured= true;
			return false;
		} catch (BuildException be) {
			//TODO not sure if we want to report these
			//a user could have taskdefs etc that depend on runtime supplied properties
			//getAntModel().handleBuildException(be, this);
		}
		return false;
	}
}
