/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.view.elements;

public class TargetErrorNode extends TargetNode {
	
	/**
	 * Creates a new target error node with the given parent and the given error
	 * message
	 * 
	 * @param parent the target's enclosing project
	 * @param error the target's error message
	 */
	public TargetErrorNode(String error) {
		super(error, error);
	}

}
