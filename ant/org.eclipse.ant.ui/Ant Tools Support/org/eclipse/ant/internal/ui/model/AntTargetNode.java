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

import java.util.Enumeration;
import java.util.Map;

import org.apache.tools.ant.Target;
import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;

public class AntTargetNode extends AntElementNode {

	private Target fTarget= null;
	
	public AntTargetNode(Target target) {
		super("target"); //$NON-NLS-1$
		fTarget= target;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#getDisplayName()
	 */
	public String getLabel() {
		String targetName= fTarget.getName();
		if (targetName == null) {
			targetName= "target"; //$NON-NLS-1$
			setProblemSeverity(AntModelProblem.SEVERITY_ERROR);
		}
		
		StringBuffer displayName= new StringBuffer(targetName);
		if (isDefaultTarget()) {
			displayName.append(AntModelMessages.getString("AntTargetNode.2")); //$NON-NLS-1$
		}
		if (isExternal()) {
			appendEntityName(displayName);
		}
		
		return displayName.toString();
	}
	
	public Target getTarget() {
		return fTarget;
	}
	
	public boolean isDefaultTarget() {
		String targetName= fTarget.getName();
		if (targetName == null) {
			return false;
		}
		return targetName.equals(fTarget.getProject().getDefaultTarget());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#getBaseImageDescriptor()
	 */
	protected ImageDescriptor getBaseImageDescriptor() {
		ImageDescriptor base= null;
		if (isDefaultTarget()) {
			base = AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_DEFAULT_TARGET);
		} else if (getTarget().getDescription() == null) {
			base = AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_TARGET_INTERNAL);
		} else {
			base = AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_TARGET);
		}
		return base;
	}
	/**
	 * @param target The Target to set.
	 */
	public void setTarget(Target target) {
		fTarget = target;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.model.AntElementNode#reset()
	 */
	public void reset() {
		super.reset();
		 Map currentTargets = fTarget.getProject().getTargets();
		 if (currentTargets.get(fTarget.getName()) != null) {
		 	currentTargets.remove(fTarget.getName());
		 }
	}

	/**
	 * Returns the name of a missing dependency or <code>null</code> if all
	 * dependencies exist in the project.
	 */
	public String checkDependencies() {
		Enumeration dependencies= fTarget.getDependencies();
		while (dependencies.hasMoreElements()) {
			String dependency = (String) dependencies.nextElement();
			 if (fTarget.getProject().getTargets().get(dependency) == null) {
			 	return dependency;
			 }
		}
		return null;
	}
	
	public boolean collapseProjection() {
		IPreferenceStore store= AntUIPlugin.getDefault().getPreferenceStore();		
		if (store.getBoolean(AntEditorPreferenceConstants.EDITOR_FOLDING_TARGETS)) {
			return true;
		}
		return false;
	}
}