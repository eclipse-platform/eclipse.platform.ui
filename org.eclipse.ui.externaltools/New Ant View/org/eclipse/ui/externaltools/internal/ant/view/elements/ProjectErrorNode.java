/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.view.elements;

public class ProjectErrorNode extends ProjectNode {
	
	private TargetErrorNode errorNode;
	
	/**
	 * Creates a new project error node with the given error message, the given
	 * parent node, and the given build file name
	 * 
	 * @param error the error message
	 * @param parent the project's parent node, typically a
	 * <code>RootNode</code>
	 * @param buildFileName the project's build file name
	 */
	public ProjectErrorNode(String error, String buildFileName) {
		super(error, buildFileName);
		addTarget(new TargetErrorNode(error));
	}
	
	/**
	 * @see org.eclipse.ui.externaltools.internal.ant.view.elements.ProjectNode#addTarget(TargetNode)
	 */
	public void addTarget(TargetNode target) {
		target.setParent(this);
	}

}
