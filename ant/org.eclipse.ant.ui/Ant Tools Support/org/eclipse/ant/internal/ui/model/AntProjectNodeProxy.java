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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.ant.internal.ui.AntUtil;
import org.eclipse.core.runtime.IPath;


public class AntProjectNodeProxy extends AntProjectNode {
	
	private String fBuildFileName;
	private String fDefaultTargetName;

	/**
	 * Creates a new project node with the given name and the given build file
	 * name.
	 * 
	 * @param name the project's name or <code>null</code> if the project's
	 * name is not known. If this value is <code>null</code>, the file will be
	 * parsed the first time a value is requested that requires it.
	 * @param buildFileName
	 */
	public AntProjectNodeProxy(String name, String buildFileName) {
		super(null, null);
		fName= name;
		fBuildFileName= buildFileName;
	}
	
	/**
	 * Creates a new project node on the given build file.
	 */
	public AntProjectNodeProxy(String buildFileName) {
		this(null, buildFileName);
	}
	
	public void parseBuildFile() {
		
		AntTargetNode[] nodes = null;
		IPath buildFilePath= AntUtil.getFile(getBuildFileName()).getLocation();
		if (buildFilePath == null) {
			//setErrorMessage(AntViewElementsMessages.getString("ProjectNode.Build_file_not_found_1")); //$NON-NLS-1$
			return;
		}
		nodes = AntUtil.getTargets(buildFilePath.toString());
		
		if (nodes.length < 1) {
			//setErrorMessage(AntViewElementsMessages.getString("ProjectNode.No_targets")); //$NON-NLS-1$
			return;
		}
		
		AntProjectNode projectNode = nodes[0].getProjectNode();
		fChildNodes= Arrays.asList(nodes);
		fModel= projectNode.getAntModel();
		fProject= (AntModelProject)projectNode.getProject();
		if (projectNode.isErrorNode()) {
			//setErrorMessage(projectNode.getLabel());
			return;
		}
		String projectName = projectNode.getName();
		if (projectName == null) {
			projectName= AntModelMessages.getString("AntProjectNodeProxy.0");  //$NON-NLS-1$
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.AntProjectNode#getDescription()
	 */
	public String getDescription() {
		if (fProject != null) {
			return super.getDescription();
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.AntElementNode#getLabel()
	 */
	public String getLabel() {
	    if (fName != null) {
	        return fName;
	    }
		if (fProject == null) {
			parseBuildFile();
		}
		return super.getLabel();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.AntElementNode#getChildNodes()
	 */
	public List getChildNodes() {
		if (fProject == null) {
			parseBuildFile();
		}
		List children= super.getChildNodes();
		if (children == null) {
		    return Collections.EMPTY_LIST;
		}
		return children;
	}
	
	public String getBuildFileName() {
		return fBuildFileName;
	}

	public void setDefaultTargetName(String defaultTarget) {
		fDefaultTargetName= defaultTarget;
		
	}
	
	public String getDefaultTargetName() {
		if (fProject == null) {
			return fDefaultTargetName;
		}
		return super.getDefaultTargetName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.model.AntElementNode#hasChildren()
	 */
	public boolean hasChildren() {
		return true;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.AntElementNode#addChildNode(org.eclipse.ant.internal.ui.model.AntElementNode)
     */
    public void addChildNode(AntElementNode childElement) {
    	if (childElement instanceof AntTargetNode) {
    		super.addChildNode(childElement);
    	}
    }
    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.AntElementNode#dispose()
     */
    public void dispose() {
        if (fProject != null) {
            super.dispose();
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.AntElementNode#isErrorNode()
     */
    public boolean isErrorNode() {
        if (fProject == null) {
            return super.isErrorNode();
        } 
        return fModel.getProjectNode().isErrorNode();
    }
    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.AntElementNode#getProblemMessage()
     */
    public String getProblemMessage() {
       if (fProject == null) {
           return null;
       }
        return fModel.getProjectNode().getProblemMessage();
    }
    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.AntElementNode#isWarningNode()
     */
    public boolean isWarningNode() {
        if (fProject == null) {
            return super.isWarningNode();
        } 
        return fModel.getProjectNode().isWarningNode();
    }
}