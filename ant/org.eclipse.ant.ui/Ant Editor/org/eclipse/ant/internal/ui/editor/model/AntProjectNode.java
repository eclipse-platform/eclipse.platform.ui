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

import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.Project;
import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.ant.internal.ui.editor.outline.XMLProblem;
import org.eclipse.ant.internal.ui.model.AntUIImages;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;


public class AntProjectNode extends AntElementNode {

	private Project fProject;
	private AntModel fModel;
	private Map fNameToDefiningNodeMap;
	
	public AntProjectNode(Project project, AntModel antModel) {
		super("project"); //$NON-NLS-1$
		fProject= project;
		fModel= antModel;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#getDisplayName()
	 */
	public String getLabel() {
		String projectName= fProject.getName();
		if (projectName == null || projectName.length() == 0) {
			projectName= "project"; //$NON-NLS-1$
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
	
	protected AntModel getAntModel() {
		return fModel;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#reset()
	 */
	public void reset() {
		super.reset();
		fProject.getTargets().clear();
		fProject.setDefault(null);
		fProject.setDescription(null);
		fProject.setName(""); //$NON-NLS-1$
		fNameToDefiningNodeMap= null;
		setProblemSeverity(XMLProblem.NO_PROBLEM);
	}
	
	public void addDefiningTaskNode(AntDefiningTaskNode node) {
		if (fNameToDefiningNodeMap == null) {
			fNameToDefiningNodeMap= new HashMap();
		}
		fNameToDefiningNodeMap.put(node.getLabel(), node);
	}
	
	public AntDefiningTaskNode getDefininingTaskNode(String nodeName) {
		if (fNameToDefiningNodeMap != null) {
			return (AntDefiningTaskNode)fNameToDefiningNodeMap.get(nodeName);
		}
		return null;
	}
}
