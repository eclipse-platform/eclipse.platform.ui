/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.model;

import org.apache.tools.ant.Project;
import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;


public class AntProjectNode extends AntElementNode {

	protected AntModelProject fProject;
	protected IAntModel fModel;
	protected String fLabel;
	
	public AntProjectNode(AntModelProject project, IAntModel antModel) {
		super("project"); //$NON-NLS-1$
		fProject= project;
		fModel= antModel;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.AntElementNode#getLabel()
	 */
	public String getLabel() {
	    if (fLabel == null) {
	        if (fProject != null) {
	            fLabel= fProject.getName();
	        } else {
	            fLabel= AntModelMessages.getString("AntProjectNode.0"); //$NON-NLS-1$
	        }
	        if (fLabel == null || fLabel.length() == 0) {
	            fLabel= "project"; //$NON-NLS-1$
	        }
	    }
		return fLabel;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#getBaseImageDescriptor()
	 */
	protected ImageDescriptor getBaseImageDescriptor() {
		return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_PROJECT);
	}

	/**
	 * Returns the Ant project associated with this project node.
	 * @return the Ant project
	 */
	public Project getProject() {
		return fProject;
	}
	
	protected IAntModel getAntModel() {
		return fModel;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#reset()
	 */
	public void reset() {
		super.reset();
		fProject.reset();
		setProblemSeverity(AntModelProblem.NO_PROBLEM);
		setProblemMessage(null);
        fOffset= -1;
        fLength= -1;
	}
	
	public String getDescription() {
		return fProject.getDescription();
	}

	public String getBuildFileName() {
		LocationProvider locationProvider= getAntModel().getLocationProvider();
		if (locationProvider != null) {
		    IFile file= locationProvider.getFile();
		    if (file != null) {
		        return file.getFullPath().toOSString();
		    }
		}
		return null;
	}
	
	public String getDefaultTargetName() {
		return fProject.getDefaultTarget();
	}
	
	/**
	 * @param node the property node that is currently being configured
	 */
	public void setCurrentConfiguringProperty(AntPropertyNode node) {
		AntModelProject project= (AntModelProject) getProject();
		project.setCurrentConfiguringProperty(node);
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.AntElementNode#getProjectNode()
     */
    public AntProjectNode getProjectNode() {
        return this;
    }
}
