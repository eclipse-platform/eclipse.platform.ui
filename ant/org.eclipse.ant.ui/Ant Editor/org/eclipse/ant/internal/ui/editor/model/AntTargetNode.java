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

import org.apache.tools.ant.Target;

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
			setIsErrorNode(true);
		}
		
		if (isDefaultTarget()) {
			targetName= targetName + AntModelMessages.getString("AntTargetNode.2"); //$NON-NLS-1$
		}
		return targetName;
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
}
