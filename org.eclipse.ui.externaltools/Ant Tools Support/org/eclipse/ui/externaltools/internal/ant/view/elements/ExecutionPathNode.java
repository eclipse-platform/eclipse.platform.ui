/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.view.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Ant node storing the targets that will be executed by a given target. Targets
 * should be added to this node in the order that they will be executed.
 */
public class ExecutionPathNode extends AntNode {
	
	private List targets= new ArrayList();
	
	/**
	 * Creates a new execution path node
	 */
	public ExecutionPathNode(TargetNode parent) {
		super(parent, AntViewElementsMessages.getString("ExecutionPathNode.Execution_Order_1")); //$NON-NLS-1$
	}
	
	/**
	 * Adds the given target to the list of targets to be executed. Targets
	 * should be added in the order in which they are to be executed.
	 * 
	 * @param targetName the target to add
	 */
	public void addTarget(String targetName) {
		targets.add(targetName);
	}
	
	/**
	 * Returns the list of targets in the order that they were added
	 * 
	 * @return String[] the targets in the order they were added
	 */
	public String[] getTargets() {
		if (targets.size() > 0) {
			return (String[]) targets.toArray(new String[targets.size()]);
		}
		return new String[] {AntViewElementsMessages.getString("ExecutionPathNode.<none>_2")}; //$NON-NLS-1$
	} 
}
