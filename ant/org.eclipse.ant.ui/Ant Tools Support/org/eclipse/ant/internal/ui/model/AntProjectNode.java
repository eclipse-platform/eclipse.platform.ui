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

import org.apache.tools.ant.Project;
import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;


public class AntProjectNode extends AntElementNode {

	protected AntModelProject fProject;
	protected IAntModel fModel;
	private Map fNameToDefiningNodeMap;
	
	public AntProjectNode(AntModelProject project, IAntModel antModel) {
		super("project"); //$NON-NLS-1$
		fProject= project;
		fModel= antModel;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#getDisplayName()
	 */
	public String getLabel() {
	    String projectName= "project"; //$NON-NLS-1$
	    if (fProject != null) {
	        projectName= fProject.getName();
	        if (projectName == null || projectName.length() == 0) {
	            projectName= "project"; //$NON-NLS-1$
	        }
	    }
		return projectName;
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
		if (fNameToDefiningNodeMap != null && getAntModel() instanceof AntModel) {
			((AntModel)getAntModel()).setNamesOfOldDefiningNodes(fNameToDefiningNodeMap.keySet());
		}
		fNameToDefiningNodeMap= null;
		setProblemSeverity(AntModelProblem.NO_PROBLEM);
		setProblemMessage(null);
	}
	
	public void addDefiningTaskNode(AntDefiningTaskNode node) {
		if (fNameToDefiningNodeMap == null) {
			fNameToDefiningNodeMap= new HashMap();
		}
		String label= node.getLabel();
		if (label.equalsIgnoreCase("macrodef") //$NON-NLS-1$
        		|| label.equalsIgnoreCase("presetdef") //$NON-NLS-1$
				|| label.equalsIgnoreCase("typedef") //$NON-NLS-1$
				|| label.equalsIgnoreCase("taskdef")) { //$NON-NLS-1$
			//only add user defined names
			return;
		}
		fNameToDefiningNodeMap.put(node.getLabel(), node);
	}
	
	public AntDefiningTaskNode getDefininingTaskNode(String nodeName) {
		if (fNameToDefiningNodeMap != null) {
			return (AntDefiningTaskNode)fNameToDefiningNodeMap.get(nodeName);
		}
		return null;
	}
	
	public String getDescription() {
		return fProject.getDescription();
	}

	public String getBuildFileName() {
		LocationProvider locationProvider= getAntModel().getLocationProvider();
		if (locationProvider != null) {
			return locationProvider.getFile().getFullPath().toOSString();
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
